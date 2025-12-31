# 架构设计指南

## 1. 架构概述

ZenFeedApp采用现代化的MVVM（Model-View-ViewModel）架构，结合Jetpack Compose实现声明式UI，确保代码的可维护性、可测试性和可扩展性。

## 2. 核心架构原则

### 2.1 单一职责原则
- 每个组件只负责一项功能
- 数据层、UI层、业务逻辑层清晰分离
- 模块化设计，便于独立开发和测试

### 2.2 响应式设计
- 使用Kotlin Flow实现数据流的响应式处理
- UI自动响应数据变化
- 状态驱动UI更新

### 2.3 依赖倒置原则
- 高层模块不依赖低层模块，而是依赖抽象
- 使用依赖注入简化组件间的依赖关系
- 便于单元测试和模块替换

## 3. 架构分层

### 3.1 数据层（Data Layer）

**职责**：
- 负责数据的获取、存储和处理
- 封装数据来源（网络、本地存储）
- 提供统一的数据访问接口

**核心组件**：
- `FeedRepository.kt`：数据仓库，管理数据的获取和缓存
- `SettingsDataStore.kt`：使用DataStore存储用户偏好设置
- `network/`：网络请求相关组件
- `model/`：数据模型定义

**数据流**：
```
网络请求/本地存储 → Repository → ViewModel → UI
```

### 3.2 业务逻辑层（ViewModel Layer）

**职责**：
- 处理业务逻辑
- 管理UI状态
- 作为数据层和UI层之间的桥梁

**核心组件**：
- `FeedsViewModel.kt`：管理摘要列表状态
- `PlayerViewModel.kt`：管理播放器状态
- `SettingsViewModel.kt`：管理设置页面状态
- `SharedViewModel.kt`：多个页面共享的状态管理

**设计原则**：
- 无UI依赖，便于测试
- 使用StateFlow/SharedFlow暴露状态
- 异步操作使用Coroutines

### 3.3 UI层（Presentation Layer）

**职责**：
- 展示UI界面
- 处理用户交互
- 响应状态变化

**核心组件**：
- Jetpack Compose组件
- 导航配置
- 主题管理

**设计原则**：
- 声明式UI，数据驱动
- 组件化设计，复用性高
- 遵循Material Design规范

## 4. 模块化设计

### 4.1 模块划分

| 模块 | 主要职责 | 核心文件 |
|------|----------|----------|
| 数据模块 | 数据获取和存储 | `FeedRepository.kt`、`SettingsDataStore.kt` |
| 网络模块 | 网络请求处理 | `ApiClient.kt`、`ApiService.kt` |
| UI模块 | 界面展示 | 各种Compose组件 |
| 导航模块 | 页面跳转管理 | `AppNavigation.kt` |
| 主题模块 | 主题切换和管理 | `ThemeController.kt`、`Theme.kt` |
| 播放器模块 | 音频播放功能 | `PlayerService.kt`、`PlayerViewModel.kt` |
| 设置模块 | 用户偏好设置 | `SettingsScreen.kt`、`SettingsViewModel.kt` |

### 4.2 模块间通信

- 数据层 → ViewModel：通过Flow传递数据
- ViewModel → UI：通过StateFlow/SharedFlow暴露状态
- UI → ViewModel：通过函数调用传递用户交互
- 页面间通信：通过`SharedViewModel`或导航参数

## 5. 异步处理

### 5.1 Kotlin Coroutines

- 使用协程处理异步操作
- 避免回调地狱
- 简化异步代码的编写和维护

### 5.2 Flow

- 用于处理异步数据流
- 支持背压处理
- 可组合、可转换
- 与Compose无缝集成

## 6. 状态管理

### 6.1 UI状态

- 使用`sealed class`定义UI状态（加载中、成功、错误）
- ViewModel维护UI状态
- UI根据状态自动更新

### 6.2 共享状态

- 使用`SharedViewModel`在多个页面间共享状态
- 适用于全局状态（如播放器状态、主题设置）

## 7. 测试策略

### 7.1 单元测试

- 测试数据层的逻辑
- 测试ViewModel的业务逻辑
- 测试扩展函数和工具类

### 7.2 集成测试

- 测试组件间的交互
- 测试网络请求和数据存储

### 7.3 UI测试

- 使用Compose Testing测试UI组件
- 测试用户交互流程
- 测试UI状态变化

## 8. 代码组织最佳实践

### 8.1 包结构

```
com.ddyy.zenfeed/
├── data/                    # 数据层
├── extension/              # Kotlin扩展函数
├── receiver/               # 广播接收器
├── service/                # 后台服务
├── ui/                     # UI层
├── MainActivity.kt         # 主活动
└── ZenFeedApplication.kt   # 应用入口
```

### 8.2 命名规范

- 类名：PascalCase（如`FeedRepository`）
- 函数名：camelCase（如`getFeeds()`）
- 变量名：camelCase（如`feedsViewModel`）
- 常量：UPPER_SNAKE_CASE（如`DEFAULT_API_BASE_URL`）
- 文件名：PascalCase（如`SettingsDataStore.kt`）

### 8.3 代码风格

- 使用Kotlin的现代特性（空安全、扩展函数、协程）
- 避免冗余代码
- 保持函数简洁，单一职责
- 适当添加注释，解释复杂逻辑
- 遵循Android Kotlin Style Guide

## 9. 性能优化

### 9.1 内存管理

- 使用`by lazy`和`remember`延迟初始化
- 避免内存泄漏（注意协程和回调的生命周期）
- 及时清理资源

### 9.2 UI性能

- 使用`remember`缓存计算结果
- 避免不必要的重组
- 使用`LaunchedEffect`和`DisposableEffect`处理副作用
- 合理使用`derivedStateOf`

### 9.3 网络性能

- 实现数据缓存，减少网络请求
- 使用适当的请求策略（如重试机制、超时设置）
- 压缩网络数据

## 10. 扩展性设计

- 模块化设计，便于添加新功能
- 抽象接口，便于替换实现
- 预留扩展点，如主题、播放器等
- 支持插件化架构

## 11. 总结

ZenFeedApp的架构设计遵循了现代Android开发的最佳实践，结合了MVVM架构、Jetpack Compose、Kotlin协程等技术，确保了代码的可维护性、可测试性和可扩展性。在开发新功能时，应严格遵循现有架构设计，保持代码风格的一致性，确保项目的长期健康发展。