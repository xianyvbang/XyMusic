Place the bundled Windows VLC runtime files in this directory.

Download url: https://get.videolan.org/vlc/
不稳定版本: https://nightlies.videolan.org/

参考: https://github.com/open-ani/mediamp/tree/main/mediamp-vlc

Expected runtime contents:
- `libvlc.dll`
- `plugins/`

You can also point Gradle to another runtime directory:
- `-PvlcRuntimeDir=C:\path\to\vlc-runtime`
- `VLC_RUNTIME_DIR=C:\path\to\vlc-runtime`

The build copies this directory into Compose Desktop app resources as `lib/`.
At runtime, vlcj discovers `compose.application.resources.dir/lib` directly,
without extracting a zip into `%LOCALAPPDATA%`.
