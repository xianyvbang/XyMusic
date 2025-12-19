package cn.xybbz.config.module;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;

import javax.annotation.processing.Generated;

import cn.xybbz.config.download.work.DownloadWork;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = DownloadWork.class
)
public interface DownloadWork_HiltModule {
  @Binds
  @IntoMap
  @StringKey("cn.xybbz.config.download.work.DownloadWork")
  WorkerAssistedFactory<? extends ListenableWorker> bind(DownloadWork_AssistedFactory factory);


  @Binds
  @IntoMap
  @StringKey("cn.xybbz.api.dispatchs.MediaLibraryAndFavoriteSyncWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind2(MediaLibraryAndFavoriteSyncWork_AssistedFactory factory);
}
