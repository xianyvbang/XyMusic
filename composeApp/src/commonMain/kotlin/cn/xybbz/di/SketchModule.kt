package cn.xybbz.di

import cn.xybbz.platform.ContextWrapper
import com.github.panpf.sketch.Sketch
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
expect class SketchModule {

    @Single
    fun sketch(contextWrapper: ContextWrapper): Sketch
}
