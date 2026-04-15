# Compose for Desktop UI 还原任务

## 任务目标
请根据提供的 HTML/CSS/JS Web 原型（见 `XyMusic_Windows_UI_Prototype.html`），为我的 Kotlin Multiplatform (KMP) 项目生成完全对应的 **Compose for Desktop** 桌面端 UI 代码。

我的项目基于 **Compose Multiplatform (v1.10.2)**，主界面运行在 Windows 桌面上。

## 架构与状态要求
你**不需要**处理真实的数据加载（Ktor/Room）和实际的媒体播放逻辑，仅需要生成**纯 UI 展示层**，但必须做好合理的架构分层，以便我后续接入真实数据。

1. **页面路由 (Navigation)**：
   - 请使用简单的状态提升（State Hoisting）或简单的 `enum class Screen` 来管理页面切换。原型中的 `switchPage` 逻辑应映射为改变当前 `Screen` 状态，并使用 `Crossfade` 或简单的 `when(currentScreen)` 切换主内容区的 Composable。
   - 需要包含的页面状态：`Home`, `Search`, `Library`, `Albums`, `Artists`, `PlaylistDetail`, `AlbumDetail`, `ArtistDetail`。

2. **全局布局 (Scaffold/Layout)**：
   - 请使用 `Row` 和 `Column` 还原原型的 CSS Grid 布局：
     - 左侧固定宽度的 `Sidebar` (宽度约 240dp)。
     - 右侧占据剩余空间的 `MainContent` (需支持独立垂直滚动)。
     - 底部固定高度的 `PlayerBar` (高度约 90dp)。
   - 播放队列 (Play Queue) 需要实现为一个从右侧滑出的抽屉 (Drawer) 或叠加在 `MainContent` 右侧的 `AnimatedVisibility` 面板。

3. **组件化 (Composables)**：
   - 将 UI 拆分为高内聚的局部组件，例如：
     - `Sidebar()`
     - `PlayerBar()`
     - `SongList()` (使用 `LazyColumn` 或简单的 `Column` 遍历)
     - `AlbumGrid()` (必须使用 `LazyVerticalGrid` 实现原型的 5 列响应式网格)
     - `SongItemCompact()` (推荐音乐/最近播放的两列网格项)
   - 对于图片，由于是预览版，可以使用 `Box` 配合背景色，或者使用占位图标代替 URL 图片。如果你打算使用真实的 URL 图片，请使用简单的修饰符占位，因为我项目中使用了 `Sketch`，后续我会自己替换。

## 样式与主题要求
- **不要硬编码颜色**：原型中的 CSS 颜色变量（如 `--bg-base`, `--bg-elevated`, `--theme-color`）应映射为 Compose `MaterialTheme.colorScheme` 的颜色，或者定义在局部的 `val colors` 中。
  - 背景黑：`#000000`
  - 模块背景灰：`#121212` 和 `#1a1a1a`
  - 主题色绿：`#1DB954`
- **排版与间距**：严格还原原型中的 `padding`, `gap`, `border-radius`, `font-size` 等数值。
- **毛玻璃效果**：原型中顶部导航栏和底部 `PlayerBar` 使用了 `backdrop-filter: blur(10px)`。如果 Compose for Desktop 原生不支持毛玻璃，请使用半透明的背景色 (如 `Color(0xCC000000)`) 代替。

## 交互与动画要求
- **Hover 效果**：侧边栏菜单、歌曲列表行、专辑卡片在鼠标悬停时需要有背景色变化的反馈。请使用 `Modifier.pointerInput` 或 `Modifier.hoverable` (或使用 clickable 自带的 ripple 效果，并调整其颜色)。
- **悬停显示播放按钮**：原型中“专辑卡片”在 Hover 时，右下角会浮现绿色的播放按钮。请使用 `AnimatedVisibility` 或 `Modifier.alpha` 结合 hover 状态实现。
- **进度条/音量条**：使用 `Slider` 或自定义的 `Box` 实现，并还原 Hover 时高度变粗或颜色变亮的交互。

## 附件
请详细阅读工作区根目录下的 `XyMusic_Windows_UI_Prototype.html` 文件，它包含了所有页面结构的 DOM 树和完整的 CSS 样式。请确保你生成的 Compose 代码能够在 Desktop 环境下达到与之 95% 以上的视觉相似度。