//todo 我的模拟长生路吧，之前看到过两百多章，还可以
//加一个播放记录列表,
//需要放一些基本数据,防止刚进入的时候没有数据=Room
//页面缓存技术 下一次打开后加载上次数据 =Room
//本地音乐
//点击后台的暂停 ui没更新
//关闭lazy的懒加载 需要修改为Column一次性加载
//需要解决接口重复提交问题
//home未标记刷新状态 完成
//背景渐变色 完成
//todo 图片加载中的数据需要加加载图片
//todo 自建音乐服务也可以听歌,可以从自建服务抽取数据,在app上显示
//搜索功能信息
//-1L
//接口同时使用异步,而不是用同一个异步进行顺序加载
//MusicAlbumScreen修改为自己专属的viewModel
//todo 可以接收用户自定义数据
//播放历史
// 用户信息
//播放历史少个card上的播放按钮
//播放音乐有点不太对,应该不设置musicInfo信息
// 播放历史未去重
//musicPlay看看是否可以转@Componse
//重复点击后不播放,需要暂停再开始才会播放
//todo 同一个音乐列表,如果点击其中的音乐,不重新赋值,而是跳转到相应的音乐里
//区分是点击音乐,还是点击下一首
//播放音乐功能给放到协程里
//todo 历史专辑未生效
//todo 历史播放横向二十个,点击右侧的标识符进入全部分类,里面只显示全部,专辑(歌单),歌曲,有声小说
//todo 需要缓存歌词
//todo 歌词显示的时候动画会卡顿
//todo 看drawable/f.jpg
//todo 监听制定目录的变化,从而判断文件是否已经被删除,从而从本地目录中获取数据  https://blog.csdn.net/duleilewuhen/article/details/119055533
//todo modifier.semantics 实现本地化
//room的一对多关系的查询修改 https://developer.android.com/training/data-storage/room/relationships?hl=zh-cn
//防止多次点击
//下载功能已完成,但是下载列表恢复,未完成
//viewModel分隔mainViewModel
//分页加载 https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data?hl=zh-cn
// https://developer.android.com/topic/libraries/architecture/paging/v3-network-db?hl=zh-cn
// paging使用 https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary
// 重复点击问题, 设置的某些选择项未生效问题,我的页面的几个按钮问题, 歌单应该增加个创建的框撑起页面
// 抽取搜索
//搜索列表-综合和歌手 将综合和歌手去除了
//todo tg群命令/vmo关不上的窗
//播放历史
//todo 暂停后切换到歌词,切换回,需要点击两次才能暂停
//todo 隐私和关于页面
//列表查询加上@Transaction
//音乐切换前,将当前音乐的播放进度清除,增加有声小说的播放进度历史
// 专辑页面的歌曲和有声小说调换位置,然后有声小说也用Card
//跳转音频,跳转进度,暂停的时候存储
//todo 最近播放未生效
//专辑页面还是需要使用LazyColumn或者使用导航
//navController.popBackStack()返回会直接首页
//下载接口 http://192.168.3.12:8096/Items/175b12f97855eb6b3fffff8871881733/Download?api_key=e979cb67aa844028ad2b889af69c4d3c
//通用方法参数类型-抽取出来onRouter(Long)0>Unit 修改成 onRouter(object)->Unit
//专辑,音乐的 艺术家使用join后续需要思考一下合不合理
//todo api切换后app重启
//todo 收藏歌曲没有显示
//todo 提交收藏歌曲/专辑

//too LazyColumn+ collectAsLazyPagingItems 加载新数据闪烁 可以在item内的组件里加上remember
//home的顶部加载有问题
//playType 循环模式重启应用未恢复(可能恢复了,待查看)
//下载使用DownloadService https://android-dot-google-developers.gonglchuangl.net/media/media3/exoplayer/downloading-media?hl=zh-cn
//收藏接口数据
//todo 歌词有问题
//token时间校验
// AwtWindow
// todo 一些特别的组件 https://developer.android.com/develop/ui/compose/libraries?hl=zh-cn
//home的下面的随机列表需要解决索引问题,还有就是加载当前列表的问题
// 填空组件 Spacer
// todo 音乐焦点-是否同时播放 https://developer.android.com/media/optimize/audio-focus?hl=zh-cn
//使用cdn隐藏真实ip
//搜索列表无法播放音乐,专辑列表无法点击进入
// todo 本地化 context.getString(
//						R.string.app_notification_update_soon,
//						currentServerVersion,
//						ServerRepository.upcomingMinimumServerVersion
//					)
//acctoken是否有过期时间
//todo 各个数据源之间的收藏数据要互通
//todo 收藏页面的草图 https://docs.qq.com/flowchart/DSEVZWXJ0S2t6VERi?u=62449d5d6fc246fdae9e2f48657379ec
//播放历史不需要根据数据源查询,只是显示数据源
// apiclient的部分异常未处理
// 删除黑名单功能
//数据源准备中的时候报错,需要增加刷新按钮
//todo 权限的监测变化需要重写,尤其是本地扫描那块
//需要一个全局的AlertDialog MainScope()
//删除有声音乐类型为专辑
//todo 需要增加一个更换服务器的按钮
//todo 增加删除播放进度功能
//歌词加载需要重新写
//todo 加入动画共享元素,从而实现专辑和艺术家点击的时候的切换
//todo 垫了一块navigationBarsPadding
//todo HorizontalPager里的page是开始展示的分页数据
//todo scrollBehavior可以控制TopAppBar和BottomAppBar的显隐  https://stackoverflow.com/questions/72240936/horizontalpager-with-lazycolumn-inside-another-lazycolumn-jetpack-compose

//MusicTypeEnum.ALBUM.code

//            val connectShare = session.connectShare("docker") as DiskShare
//            println(share.smbPath)
//            val list = connectShare.list("")
//            val file = connectShare.openFile("/music/23年11-12月更新/12월의 기적 (十二月的奇迹) - EXO.mp3", EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
//var shares1 = serverService.shares1


//todo 这里需要测试播放功能
/*for (fileIdBothDirectoryInformation in list) {

    Log.i("=====", "共享文件名称" + fileIdBothDirectoryInformation.fileName)
}*/
/*
list.forEach {
    println("File : " + it.fileName)
}*/

//todo 相机和选择图片使用 https://juejin.cn/post/6965828380713287716?searchId=202405282318581B41DB10AC27D81ACD62
//    rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)


//todo 分页想页面在其他页面存储数据进入的时候不变化,需要 distinctUntilChanged()

//todo FTS4导致升级数据库的时候无法使用alert table修改字段

//todo Paging + Room 时有bug, append的时候,列表会向下移动一小段距离
//沉浸模式 https://developer.android.com/develop/ui/views/layout/immersive?hl=zh-cn
//蓝牙断开应该使用: https://android-dot-google-developers.gonglchuangl.net/media/platform/output?hl=zh-cn 里的方法
//完善定时关闭使用: https://blog.csdn.net/weixin_53324308/article/details/129966643 里的方法

// TODO 在文字中添加Icon
//val inlineContentId = "inlineContentId"
//
//    val secondaryAnnotatedText = buildAnnotatedString {
//        append(secondaryText)
//        appendInlineContent(inlineContentId, "[icon]")
//    }
//
//    val inlineContent = mapOf(
//        Pair(inlineContentId,
//            InlineTextContent(
//                //设置宽高
//                Placeholder(
//                    width = 14.sp,
//                    height = 14.sp,
//                    placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
//                )
//            ) {
//                Icon(
//                    imageVector = Icons.Default.HelpOutline,
//                    contentDescription = null,
//                    modifier = Modifier.clickable {
//                        Log.i("===", "点击了问号")
//                    })
//            })
//    )
