package cn.xybbz.config.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import org.koin.mp.KoinPlatform
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.util.Base64

/**
 * Android 连接凭据存储工厂。
 */
@Singleton
actual class ConnectionCredentialStoreFactory actual constructor() {
    actual fun create(): ConnectionCredentialStore {
        val contextWrapper = KoinPlatform.getKoin().get<ContextWrapper>()
        return AndroidConnectionCredentialStore(contextWrapper)
    }
}

/**
 * Android Keystore 凭据存储实现。
 */
private class AndroidConnectionCredentialStore(
    private val contextWrapper: ContextWrapper,
) : ConnectionCredentialStore {
    override suspend fun currentStoreType(connectionConfig: ConnectionConfig?): CredentialStoreType {
        return CredentialStoreType.ANDROID_KEYSTORE
    }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            ensureSecretKey()
            true
        }.getOrDefault(false)
    }

    override suspend fun save(connectionConfig: ConnectionConfig, password: String): String = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, ensureSecretKey())
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv ?: ByteArray(0)
        val payload = Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        val key = connectionConfig.currentPassword.takeIf { it.isNotBlank() } ?: ConnectionCredentialStoreKey.build(connectionConfig)
        saveToPreferences(key, payload)
        return@withContext key
    }

    override suspend fun load(connectionConfig: ConnectionConfig): String? = withContext(Dispatchers.IO) {
        val payload = readFromPreferences(connectionConfig.currentPassword).orEmpty()
        if (payload.isBlank()) {
            return@withContext null
        }
        val segments = payload.split(":", limit = 2)
        if (segments.size != 2) {
            return@withContext null
        }
        val iv = Base64.decode(segments[0], Base64.NO_WRAP)
        val encrypted = Base64.decode(segments[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, ensureSecretKey(), javax.crypto.spec.GCMParameterSpec(128, iv))
        return@withContext cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    override suspend fun delete(connectionConfig: ConnectionConfig) = withContext(Dispatchers.IO) {
        saveToPreferences(connectionConfig.currentPassword, null)
    }

    private fun ensureSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun saveToPreferences(key: String, value: String?) {
        val preferences = contextWrapper.context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
        preferences.edit().apply {
            if (value == null) {
                remove(key)
            } else {
                putString(key, value)
            }
        }.apply()
    }

    private fun readFromPreferences(key: String): String? {
        val preferences = contextWrapper.context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
        return preferences.getString(key, null)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val MASTER_KEY_ALIAS = "cn.xybbz.connection.credential.master"
        const val PREFERENCES_NAME = "cn.xybbz.connection.credentials"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
