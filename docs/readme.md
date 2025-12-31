# ZenfeedApp 知识库

欢迎使用 ZenfeedApp 知识库！本知识库提供了项目的详细文档，帮助开发者理解项目架构、功能设计和使用方法。

## 文档目录

| 文档名称 | 描述 | 文件路径 |
|----------|------|----------|
| 项目介绍 | 项目概述、技术栈和快速入门 | [readme.md](readme.md) |
| API 文档 | API接口定义和使用说明 | [api.md](api.md) |
| 架构设计 | 项目架构、分层设计和最佳实践 | [architecture.md](architecture.md) |
| 数据模块 | 数据获取、存储和管理 | [data_module.md](data_module.md) |
| UI模块 | 界面设计、导航和主题管理 | [ui_module.md](ui_module.md) |
| 播放器模块 | 音频播放功能和设计 | [player_module.md](player_module.md) |

## 项目介绍

ZenfeedApp 是一款现代化的 Android RSS 摘要阅读器，提供简洁、美观且功能丰富的资讯浏览体验，支持播客音频播放。

### 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| 语言 | Kotlin | 基于Java，语法更简洁，与Java完全兼容 |
| UI框架 | Jetpack Compose | Android的声明式UI框架，类似Flutter的Widget系统 |
| 架构 | MVVM | Model-View-ViewModel，与Flutter的Provider/Bloc模式类似 |
| 网络 | Retrofit + OkHttp | 网络请求框架，类似Flutter的Dio |
| 数据存储 | DataStore | 现代化的数据存储解决方案，替代SharedPreferences |
| 异步处理 | Kotlin Coroutines | 协程，类似Flutter的async/await |
| 导航 | Jetpack Navigation Compose | 页面导航，类似Flutter的Navigator |

### 项目结构

```
app/src/main/java/com/ddyy/zenfeed/
├── data/                    # 数据层
│   ├── network/            # 网络请求(ApiClient, ApiService)
│   ├── model/              # 数据模型
│   ├── FeedRepository.kt   # 数据仓库
│   └── SettingsDataStore.kt # 设置存储
├── extension/              # Kotlin扩展函数
├── receiver/               # 广播接收器
├── service/                # 后台服务
│   └── PlayerService.kt    # 音频播放器服务
├── ui/                     # UI层
│   ├── feeds/              # 摘要列表和详情页
│   ├── player/             # 播放器相关
│   ├── settings/           # 设置页面
│   ├── theme/              # 主题管理
│   └── navigation/         # 导航配置
├── MainActivity.kt         # 主活动
└── ZenFeedApplication.kt   # 应用入口
```

### 核心功能

1. **信息流列表**：卡片式布局展示摘要，支持分类筛选和时间显示
2. **详情页浏览**：HTML内容展示，支持左右滑动切换
3. **播客播放**：支持后台播放、通知栏控制、倍速播放等
4. **内置浏览器**：WebView实现，支持代理配置
5. **个性化设置**：主题切换、API配置、AI模型设置
6. **阅读状态管理**：自动标记已读，记住浏览位置

### 快速入手指南

#### 环境要求
- Android Studio 2020.3.1+
- Android SDK API 24+
- Kotlin 1.8.0+

#### 运行步骤
1. 克隆仓库
2. 用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接设备或启动模拟器
5. 点击"Run"按钮运行

#### 配置说明
首次启动可在设置页面配置：
- API服务器地址（默认：https://zenfeed.xyz/）
- 代理设置（如需）
- 主题切换（浅色/深色/跟随系统）

### 与Flutter/Java的对比

#### Jetpack Compose vs Flutter
- 都是**声明式UI**，通过组合组件构建界面
- 都支持**热重载**，开发效率高
- Compose使用Kotlin DSL，Flutter使用Dart Widget
- 布局系统类似，都有Row、Column、Box等基础组件

#### Kotlin vs Java
- Kotlin完全兼容Java，可直接使用Java库
- 语法更简洁，支持空安全、扩展函数、协程等特性
- 变量声明：`val`（不可变）/`var`（可变），类似Java的final
- 函数定义：`fun 函数名(参数): 返回类型`
- 类定义：`class 类名`，默认是final，需要open才能继承

### 核心文件说明

1. **MainActivity.kt**：应用入口，设置Compose内容
2. **AppNavigation.kt**：定义应用的导航结构，类似Flutter的Routes
3. **FeedsViewModel.kt**：管理摘要列表的状态，类似Flutter的ViewModel
4. **PlayerService.kt**：后台音频播放服务，处理媒体会话
5. **SettingsDataStore.kt**：使用DataStore存储用户偏好

### 开发建议

1. **从UI层入手**：先查看`ui/`目录下的Compose组件，理解界面结构
2. **学习Compose基础**：官方文档：https://developer.android.com/jetpack/compose
3. **理解MVVM架构**：数据流向：Repository → ViewModel → UI
4. **掌握Coroutines**：用于处理异步操作，如网络请求、数据库操作
5. **熟悉Navigation**：理解如何在Compose中实现页面跳转

### 构建配置

项目使用Gradle Kotlin DSL配置：
- 编译SDK：36
- 最小SDK：24
- 支持Java 11
- 启用R8代码压缩（release构建）

### 总结

Zenfeed是一个典型的现代Android应用，使用了最新的Jetpack组件和Kotlin特性。对于熟悉Java和Flutter的开发者来说，上手难度较低，因为：
- Kotlin与Java语法相似，可快速迁移Java知识
- Jetpack Compose的声明式UI与Flutter高度相似
- MVVM架构模式在移动端开发中普遍使用

建议先从运行项目开始，然后逐步深入理解各个模块的实现，特别是Compose UI和ViewModel的结合使用。