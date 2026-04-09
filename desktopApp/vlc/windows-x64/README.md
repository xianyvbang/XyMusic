Place the bundled Windows VLC runtime archive here as `vlc-runtime.zip`.

Download url: https://get.videolan.org/vlc/

Expected archive contents:
- `libvlc.dll`
- `libvlccore.dll`
- `plugins/`

You can also point Gradle to another archive location:
- `-PvlcRuntimeArchive=C:\path\to\vlc-runtime.zip`
- `VLC_RUNTIME_ARCHIVE=C:\path\to\vlc-runtime.zip`

The build copies the archive into app resources. At runtime, the app extracts
the archive on first launch to `%LOCALAPPDATA%\XyMusic\runtime\vlc\windows-x64\<resource-fingerprint>\`.
