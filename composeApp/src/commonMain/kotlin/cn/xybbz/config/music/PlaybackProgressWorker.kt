package cn.xybbz.config.music

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * 播放进度上报会话。
 *
 * @property musicId 当前会话绑定的歌曲 ID。
 * @property playSessionId 当前会话绑定的服务端播放会话 ID。
 * @property generation 当前会话在 worker 内的递增代次。
 * @property epoch 当前会话启动时对应的停止纪元。
 */
internal data class PlaybackProgressSession(
    val musicId: String,
    val playSessionId: String,
    val generation: Long,
    val epoch: Long
)

/**
 * 一次不可变的播放进度上报快照。
 *
 * @property musicId 快照对应的歌曲 ID。
 * @property playSessionId 快照对应的服务端播放会话 ID。
 * @property positionMs 快照记录的毫秒进度。
 * @property generation 快照所属的会话代次。
 */
internal data class PlaybackProgressSnapshot(
    val musicId: String,
    val playSessionId: String,
    val positionMs: Long,
    val generation: Long
)

/**
 * 串行 worker 使用的内部命令。
 */
private sealed interface PlaybackProgressCommand {
    /**
     * 启动指定歌曲的周期上报会话。
     *
     * @property musicId 要启动上报的歌曲 ID。
     * @property playSessionId 服务端播放会话 ID。
     * @property epoch 命令创建时的停止纪元。
     */
    data class Start(
        val musicId: String,
        val playSessionId: String,
        val epoch: Long
    ) : PlaybackProgressCommand

    /**
     * 提交一条即时播放进度。
     *
     * @property musicId 即时进度对应的歌曲 ID。
     * @property positionMs 即时进度的毫秒值。
     * @property epoch 命令创建时的停止纪元。
     */
    data class ReportNow(
        val musicId: String,
        val positionMs: Long,
        val epoch: Long
    ) : PlaybackProgressCommand

    /**
     * 触发指定会话代次的周期上报。
     *
     * @property generation 定时器所属的会话代次。
     */
    data class Tick(
        val generation: Long
    ) : PlaybackProgressCommand

    /**
     * 停止当前会话并按需通知等待方。
     *
     * @property epoch 停止后生效的新纪元。
     * @property completion 等待停止屏障的可选完成信号。
     */
    data class Stop(
        val epoch: Long,
        val completion: CompletableDeferred<Unit>?
    ) : PlaybackProgressCommand

    /**
     * 标记一次远端请求已经结束。
     *
     * @property requestId 本次请求的唯一编号。
     * @property generation 本次请求所属的会话代次。
     */
    data class RequestCompleted(
        val requestId: Long,
        val generation: Long
    ) : PlaybackProgressCommand

    /**
     * 关闭 worker 并释放全部子任务。
     */
    data object Close : PlaybackProgressCommand
}

/**
 * 串行调度周期进度和即时进度的内部 worker。
 *
 * 所有可变会话状态只由命令处理协程访问，远端请求最多同时运行一个。
 */
@OptIn(ExperimentalAtomicApi::class)
internal class PlaybackProgressWorker(
    parentScope: CoroutineScope,
    private val reportIntervalMillis: Long,
    private val canContinue: (PlaybackProgressSession) -> Boolean,
    private val currentPosition: () -> Long,
    private val reportProgress: suspend (PlaybackProgressSnapshot) -> Unit,
    private val onReportFailure: (Throwable) -> Unit
) : AutoCloseable {

    /** worker 自己持有的父任务，关闭时统一取消全部子任务。 */
    private val workerJob = SupervisorJob(parentScope.coroutineContext[Job])

    /** 继承调用方调度器但使用独立父任务的 worker 作用域。 */
    private val workerScope = CoroutineScope(parentScope.coroutineContext + workerJob)

    /** 串行接收所有生命周期与上报命令的无界通道。 */
    private val commands = Channel<PlaybackProgressCommand>(Channel.UNLIMITED)

    /** 记录 worker 是否已经永久关闭。 */
    private val closed = AtomicBoolean(false)

    /** 每次停止时递增，用于让并发排队中的旧命令立即失效。 */
    private val commandEpoch = AtomicLong(0L)

    /** 当前正在运行的播放进度会话。 */
    private var activeSession: PlaybackProgressSession? = null

    /** 当前会话的十秒周期定时任务。 */
    private var tickerJob: Job? = null

    /** 当前唯一允许执行的远端上报任务。 */
    private var requestJob: Job? = null

    /** 远端请求执行期间收到的最新待上报快照。 */
    private var pendingSnapshot: PlaybackProgressSnapshot? = null

    /** worker 内递增的会话代次。 */
    private var nextGeneration = 0L

    /** worker 内递增的远端请求编号。 */
    private var nextRequestId = 0L

    /** 当前远端请求的编号，用于忽略已经失效的完成回调。 */
    private var activeRequestId: Long? = null

    /** 串行消费所有命令的唯一处理任务。 */
    private val processorJob = workerScope.launch {
        processCommands()
    }.also { job ->
        job.invokeOnCompletion {
            workerJob.cancel()
        }
    }

    init {
        require(reportIntervalMillis > 0L) { "播放进度上报间隔必须大于 0" }
    }

    /**
     * 幂等启动指定歌曲的周期上报会话。
     */
    fun start(musicId: String, playSessionId: String) {
        if (musicId.isBlank() || closed.load()) {
            return
        }
        commands.trySend(
            PlaybackProgressCommand.Start(
                musicId = musicId,
                playSessionId = playSessionId,
                epoch = commandEpoch.load()
            )
        )
    }

    /**
     * 提交当前活动会话的一条即时进度。
     */
    fun reportNow(musicId: String, positionMs: Long) {
        if (musicId.isBlank() || closed.load()) {
            return
        }
        commands.trySend(
            PlaybackProgressCommand.ReportNow(
                musicId = musicId,
                positionMs = positionMs,
                epoch = commandEpoch.load()
            )
        )
    }

    /**
     * 非阻塞地让当前会话及已经排队的旧命令失效。
     */
    fun stop() {
        if (closed.load()) {
            return
        }
        val stoppedEpoch = commandEpoch.fetchAndAdd(1L) + 1L
        commands.trySend(
            PlaybackProgressCommand.Stop(
                epoch = stoppedEpoch,
                completion = null
            )
        )
    }

    /**
     * 停止当前会话，并等待定时器、在途请求与待处理快照全部结束。
     */
    suspend fun stopAndJoin() {
        if (closed.load()) {
            workerJob.join()
            return
        }
        val completion = CompletableDeferred<Unit>()
        val stoppedEpoch = commandEpoch.fetchAndAdd(1L) + 1L
        val completionHandle = workerJob.invokeOnCompletion {
            completion.complete(Unit)
        }
        try {
            val result = commands.trySend(
                PlaybackProgressCommand.Stop(
                    epoch = stoppedEpoch,
                    completion = completion
                )
            )
            if (result.isSuccess) {
                completion.await()
            } else {
                workerJob.join()
            }
        } finally {
            completionHandle.dispose()
        }
    }

    /**
     * 串行处理 worker 收到的全部命令。
     */
    private suspend fun processCommands() {
        try {
            for (command in commands) {
                when (command) {
                    is PlaybackProgressCommand.Start -> handleStart(command)
                    is PlaybackProgressCommand.ReportNow -> handleReportNow(command)
                    is PlaybackProgressCommand.Tick -> handleTick(command)
                    is PlaybackProgressCommand.Stop -> handleStop(command)
                    is PlaybackProgressCommand.RequestCompleted -> handleRequestCompleted(command)
                    PlaybackProgressCommand.Close -> {
                        stopActiveSession()
                        return
                    }
                }
            }
        } finally {
            tickerJob?.cancel()
            requestJob?.cancel()
            tickerJob = null
            requestJob = null
            activeRequestId = null
            pendingSnapshot = null
            activeSession = null
        }
    }

    /**
     * 处理启动命令，并保证同一歌曲和会话不会重复创建定时器。
     */
    private suspend fun handleStart(command: PlaybackProgressCommand.Start) {
        if (command.epoch != commandEpoch.load() || closed.load()) {
            return
        }
        val currentSession = activeSession
        if (
            currentSession?.musicId == command.musicId &&
            currentSession.playSessionId == command.playSessionId &&
            currentSession.epoch == command.epoch
        ) {
            return
        }

        stopActiveSession()
        if (command.epoch != commandEpoch.load() || closed.load()) {
            return
        }
        val newSession = PlaybackProgressSession(
            musicId = command.musicId,
            playSessionId = command.playSessionId,
            generation = ++nextGeneration,
            epoch = command.epoch
        )
        if (!canContinue(newSession)) {
            return
        }
        activeSession = newSession
        tickerJob = workerScope.launch {
            while (true) {
                delay(reportIntervalMillis)
                if (
                    commands.trySend(
                        PlaybackProgressCommand.Tick(newSession.generation)
                    ).isFailure
                ) {
                    return@launch
                }
            }
        }
    }

    /**
     * 处理即时上报，只接受当前活动会话对应的歌曲。
     */
    private suspend fun handleReportNow(command: PlaybackProgressCommand.ReportNow) {
        if (command.epoch != commandEpoch.load() || closed.load()) {
            return
        }
        val session = activeSession ?: return
        if (session.musicId != command.musicId || session.epoch != command.epoch) {
            return
        }
        if (!canContinue(session)) {
            stopActiveSession()
            return
        }
        enqueueSnapshot(
            PlaybackProgressSnapshot(
                musicId = session.musicId,
                playSessionId = session.playSessionId,
                positionMs = command.positionMs,
                generation = session.generation
            )
        )
    }

    /**
     * 处理周期触发，并在会话条件失效时自动停止。
     */
    private suspend fun handleTick(command: PlaybackProgressCommand.Tick) {
        if (closed.load()) {
            stopActiveSession()
            return
        }
        val session = activeSession ?: return
        if (
            session.generation != command.generation ||
            session.epoch != commandEpoch.load()
        ) {
            return
        }
        if (!canContinue(session)) {
            stopActiveSession()
            return
        }
        enqueueSnapshot(
            PlaybackProgressSnapshot(
                musicId = session.musicId,
                playSessionId = session.playSessionId,
                positionMs = currentPosition(),
                generation = session.generation
            )
        )
    }

    /**
     * 处理停止命令并在全部旧任务结束后完成屏障信号。
     */
    private suspend fun handleStop(command: PlaybackProgressCommand.Stop) {
        try {
            val session = activeSession
            if (session == null || session.epoch < command.epoch) {
                stopActiveSession()
            }
        } finally {
            command.completion?.complete(Unit)
        }
    }

    /**
     * 处理远端请求完成事件，并继续发送合并后的最新快照。
     */
    private suspend fun handleRequestCompleted(
        command: PlaybackProgressCommand.RequestCompleted
    ) {
        if (activeRequestId != command.requestId) {
            return
        }
        requestJob = null
        activeRequestId = null

        val snapshot = pendingSnapshot
        pendingSnapshot = null
        val session = activeSession
        if (
            !closed.load() &&
            snapshot != null &&
            session != null &&
            snapshot.generation == command.generation &&
            snapshot.generation == session.generation &&
            session.epoch == commandEpoch.load() &&
            canContinue(session)
        ) {
            launchRequest(snapshot)
        }
    }

    /**
     * 排队快照；存在在途请求时只覆盖保留最新值。
     */
    private fun enqueueSnapshot(snapshot: PlaybackProgressSnapshot) {
        if (requestJob != null) {
            pendingSnapshot = snapshot
            return
        }
        launchRequest(snapshot)
    }

    /**
     * 启动唯一的远端播放进度上报请求。
     */
    private fun launchRequest(snapshot: PlaybackProgressSnapshot) {
        val requestId = ++nextRequestId
        activeRequestId = requestId
        val newRequestJob = workerScope.launch(start = CoroutineStart.LAZY) {
            try {
                reportProgress(snapshot)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                onReportFailure(throwable)
            } finally {
                commands.trySend(
                    PlaybackProgressCommand.RequestCompleted(
                        requestId = requestId,
                        generation = snapshot.generation
                    )
                )
            }
        }
        requestJob = newRequestJob
        newRequestJob.start()
    }

    /**
     * 清空当前会话并等待其定时器和远端请求结束。
     */
    private suspend fun stopActiveSession() {
        activeSession = null
        pendingSnapshot = null

        val activeTickerJob = tickerJob
        tickerJob = null
        activeTickerJob?.cancelAndJoin()

        val activeReportJob = requestJob
        requestJob = null
        activeRequestId = null
        activeReportJob?.cancelAndJoin()
    }

    /**
     * 永久关闭 worker，关闭后不再接受任何新命令。
     */
    override fun close() {
        if (!closed.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }
        commandEpoch.fetchAndAdd(1L)
        if (commands.trySend(PlaybackProgressCommand.Close).isFailure) {
            workerJob.cancel()
        }
        commands.close()
    }
}
