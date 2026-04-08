package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import com.github.panpf.sketch.Sketch
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
expect class SketchModule {

    @Single
    fun sketch(dataSourceManager: DataSourceManager): Sketch
}