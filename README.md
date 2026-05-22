# TomatoStudy

番茄自习室是一个基于 Android 的学习任务与专注管理 App，核心目标是把待办任务、番茄钟、专注统计和本地学习数据管理整合在一个轻量的单机应用里。


## 功能概览

- 用户注册、登录、退出登录
- 启动页自动判断本地登录状态
- 底部导航主界面
- 学习任务列表
- 新增、编辑、删除任务
- 任务操作弹窗
- 专注计时、暂停、继续、结束
- 休息倒计时
- 专注记录保存到本地 SQLite
- 统计数据页
  - 累计专注
  - 当日专注
  - 专注时长分布
  - 本月专注时段分布
  - 月度专注趋势

## 技术栈

- Java
- Android XML Layout
- AppCompat
- Material Components
- RecyclerView
- AndroidX Lifecycle ViewModel
- SQLiteOpenHelper
- DAO + Repository + ViewModel 分层

## 项目结构

```text
TomatoStudy
+-- app
|   +-- src/main
|       +-- AndroidManifest.xml
|       +-- java/com/example/tomatostudy
|       |   +-- database      # SQLiteOpenHelper、DAO、数据模型
|       |   +-- repository    # 数据访问封装
|       |   +-- ui            # Activity、Fragment、Adapter、Dialog、自定义 View
|       |   +-- util          # 工具类
|       |   +-- viewmodel     # 页面逻辑与数据调度
|       +-- res
|           +-- drawable
|           +-- layout
|           +-- menu
|           +-- values
+-- build.gradle
+-- settings.gradle
+-- gradle.properties
+-- TomatoStudy_Project_Plan.md
```

## 环境要求

推荐环境：

- Android Studio
- JDK 17
- Android SDK Platform 35
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- Android 设备或模拟器

项目配置：

```text
compileSdk 35
minSdk 23
targetSdk 35
applicationId com.example.tomatostudy
```

依赖：

```gradle
implementation 'androidx.appcompat:appcompat:1.7.0'
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.8.7'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.google.android.material:material:1.12.0'
```

## 启动方式

### 使用 Android Studio 运行

1. 克隆项目：

   ```powershell
   git clone https://github.com/YinLiangFeng/TomatoStudy.git
   ```

2. 使用 Android Studio 打开项目根目录 `TomatoStudy`。

3. 等待 Gradle Sync 完成。

4. 如果 Android Studio 要求选择 Gradle JDK，请选择 JDK 17。

5. 连接 Android 设备或启动模拟器。

6. 点击 Android Studio 顶部的 Run 按钮运行 `app`。

### 使用命令行构建

当前仓库没有提交 Gradle Wrapper，因此命令行构建需要本机已安装 Gradle，或使用本机 Gradle 缓存中的 `gradle.bat`。

在 Windows PowerShell 中可以参考：

```powershell
$env:JAVA_HOME = "D:\JDK17"
$env:Path = "D:\JDK17\bin;" + $env:Path
gradle :app:assembleDebug --no-daemon
```

如果 `gradle` 不在 PATH 中，可以把命令中的 `gradle` 替换为本机 `gradle.bat` 的完整路径。

构建成功后，Debug APK 通常位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 本地数据说明

项目使用 SQLite 保存本地数据，数据库文件属于运行时数据，不应提交到 GitHub。仓库已经通过 `.gitignore` 忽略了常见数据库文件、构建产物、IDE 缓存和本机配置。

已忽略的典型内容包括：

- `.gradle/`
- `.idea/`
- `build/`
- `app/build/`
- `local.properties`
- `*.db`
- `*.apk`
- `*.jks`
- `*.keystore`

## 开发说明

项目以清晰、可读、可运行为优先目标。当前主要采用：

- Activity / Fragment 负责页面展示、点击事件和跳转
- ViewModel 负责页面逻辑和调用 Repository
- Repository 负责封装数据操作
- DAO 负责 SQLite 增删改查

后续可以继续扩展：

- 学习打卡
- 设置页面
- 通知提醒
- 锁机/专注模式增强
- 更完整的数据统计与可视化
