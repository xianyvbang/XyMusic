Place the bundled Windows VLC runtime files in this directory.

稳定版下载: https://get.videolan.org/vlc/
不建议使用 nightly / 4.0.0-dev 运行时，它们会在 Windows 上触发播放器 timer 断言。

参考: https://github.com/open-ani/mediamp/tree/main/mediamp-vlc

Expected runtime contents:
- `libvlc.dll`
- `plugins/`

请优先使用 VLC 3.0.x 稳定版运行时。

You can also point Gradle to another runtime directory:
- `-PvlcRuntimeDir=C:\path\to\vlc-runtime`
- `VLC_RUNTIME_DIR=C:\path\to\vlc-runtime`

The build copies this directory into Compose Desktop app resources as `lib/`.
At runtime, vlcj discovers `compose.application.resources.dir/lib` directly,
without extracting a zip into `%LOCALAPPDATA%`.
