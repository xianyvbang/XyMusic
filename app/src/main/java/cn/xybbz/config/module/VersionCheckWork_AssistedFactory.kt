package cn.xybbz.config.module

import androidx.hilt.work.WorkerAssistedFactory
import cn.xybbz.config.update.VersionCheckWorker
import dagger.assisted.AssistedFactory
import javax.annotation.processing.Generated

@Generated("androidx.hilt.AndroidXHiltProcessor")
@AssistedFactory
interface VersionCheckWork_AssistedFactory:WorkerAssistedFactory<VersionCheckWorker> {
}