目前首页加载复杂的根本原因，是登录、数据源恢复、首页刷新、播放器初始化分别由多个类负责：
- StartupInitializer 加载本地缓存
- DataSourceBootstrapper 恢复连接并登录
- HomeViewModel.observeLoginSuccess() 监听登录事件
- HomeDataRepository 监听数据库数据
- HomeScreen 又参与触发后置初始化
- DataSourceManager 同时维护登录状态、连接 ID、数据源对象和错误状态
  这些流程之间通过多个 Flow、回调和状态变量互相触发，所以首次进入时容易出现时序问题。

建议以后收敛成一个“应用会话协调器”，只维护一个状态：
冷启动
↓
读取本地连接
↓
本地缓存可用
↓
后台登录
↓
登录成功 → 刷新首页
登录失败 → 显示本地缓存和重试按钮
核心状态可以设计为：
AppSessionState =
NoConnection
Restoring(connection)
OfflineReady(connection)
LoggingIn(connection)
Ready(connection)
LoginFailed(connection, error)
推荐职责划分：
1. SessionCoordinator
   统一负责：
    - 恢复当前连接
    - 切换连接
    - 登录和重登录
    - 暴露当前连接 ID
    - 暴露当前登录状态
      所有登录入口最终都调用它，不再分别维护多套登录流程。
2. HomeDataRepository
   只负责：
    - 读取本地首页缓存
    - 监听数据库变化
    - 执行一次首页远程刷新
      它不监听登录事件，也不关心播放器和页面生命周期。
3. HomeViewModel
   只负责：
    - 订阅 HomeDataRepository
    - 响应用户下拉刷新
    - 响应重试
    - 显示页面状态
      不再通过 mergeFlow 判断登录成功。
4. 首页入口
   首页只做两件事：
    - 订阅本地首页数据
    - 根据 SessionState.Ready 显示当前连接
      登录完成后的刷新由协调器统一触发，页面不再通过 LaunchedEffect、回调或事件补刷。
      推荐的简化时序是：
      App 启动
      ↓
      SessionCoordinator.initialize()
      ↓
      恢复连接并发布 OfflineReady
      ↓
      首页显示本地缓存
      ↓
      后台登录
      ↓
      发布 Ready
      ↓
      HomeDataRepository.refresh()
      ↓
      数据库 Flow 自动更新首页
      连接切换也复用同一条流程：
      用户选择连接
      ↓
      SessionCoordinator.switch(connection)
      ↓
      取消旧连接刷新任务
      ↓
      恢复新连接缓存
      ↓
      登录新连接
      ↓
      刷新新连接首页
      这套方案可以消除几个主要问题：
- 不再依赖 observeLoginSuccess() 是否及时订阅
- 不再需要 mergeFlow 合并多个异构事件
- 不再需要首页主动调用后置启动回调
- 当前连接 ID 和登录状态只有一个来源
- 首页可以先显示缓存，网络登录不阻塞首屏
- 登录失败时仍能保留本地数据
- 切换连接时不会继续读取旧连接数据
  建议后续按这个顺序重构：
1. 先定义统一的 SessionState
2. 将所有登录入口收敛到 SessionCoordinator
3. 删除首页对登录事件的监听
4. 让首页刷新只由 SessionState.Ready 触发
5. 最后把播放器初始化从数据源登录流程中拆出去