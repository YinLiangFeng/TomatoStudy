# 番茄自习室 Android 项目完整实现计划

## 1. 项目定位

项目名称：番茄自习室

项目主题：基于 Android 的学习任务与专注管理系统

核心定位：待办任务 + 番茄钟 + 专注统计 + 学习打卡

技术栈：

- Java
- MVVM
- SQLite
- Activity
- Fragment
- XML 布局

项目性质：

- 本地单机 App
- 不接服务器
- 不做云同步
- 数据全部保存在本地 SQLite 中
- 重点体现 Android 客户端页面、跳转、数据库、计时器、通知、统计展示能力

## 2. 全局实现要求

本项目以课程设计展示为主，优先保证代码可读性、结构清楚、功能完整。

代码要求：

- 尽量使用简单 Java 语法，不使用复杂写法。
- 不使用复杂泛型、反射、注解处理、依赖注入框架等不必要技术。
- 不使用 Room，数据库使用原生 `SQLiteOpenHelper + DAO`。
- 不使用 RxJava、协程等复杂异步方案。
- MVVM 分层保留，但每层职责要简单直接。
- Repository 只作为数据操作封装层，不做复杂架构设计。
- Activity 和 Fragment 负责界面展示、点击事件、页面跳转。
- ViewModel 负责页面逻辑、表单校验、调用 Repository。
- Repository 负责调用 DAO，组合简单的数据操作。
- DAO 负责 SQLite 的增删改查。
- 类名、方法名、变量名要直观，例如 `addTask()`、`loadTodayFocus()`、`saveFocusRecord()`。
- 页面和功能优先完整跑通，不追求复杂动画和高级架构技巧。

中文注释要求：

- 关键业务流程必须尽可能加入中文注释。
- 注释重点说明当前流程在做什么，以及为什么要这样处理。
- 避免无意义注释，例如只重复变量名含义的注释。
- 注释语言统一使用中文。

必须添加中文注释的位置：

- `SplashActivity` 中判断登录状态、跳转登录页或主页。
- `LoginViewModel` 和 `UserRepository` 中注册、登录、保存登录状态。
- `TaskFragment` 中区分点击任务卡片和点击开始按钮的逻辑。
- `TaskActionDialogFragment` 中编辑、删除、查看统计等操作入口。
- `TaskEditActivity` 中新增任务和编辑任务的区别。
- `FocusActivity` 中专注状态、暂停状态、休息状态的切换。
- `FocusActivity` 中结束专注后保存 `focus_record` 的逻辑。
- `FocusActivity` 中休息结束后返回首页的逻辑。
- `StatisticsRepository` 中累计专注、当日专注、时长分布、本月时段分布、月度统计的 SQL 查询逻辑。
- `CheckInRepository` 中判断当天是否已打卡。
- `SettingsRepository` 中保存和读取番茄钟默认设置。

开发操作要求：

- 禁止批量删除文件或目录。
- 需要删除文件时，只能一次删除一个明确路径的文件。
- 不使用 `del /s`、`rd /s`、`rmdir /s`、`Remove-Item -Recurse`、`rm -rf`。

## 3. 整体页面结构

项目主要由 5 个 Activity 和多个 Fragment 组成。

```text
SplashActivity
  -> LoginActivity
      -> LoginFragment
      -> RegisterFragment

  -> MainActivity
      -> TaskFragment
      -> TaskCollectionFragment
      -> LockFocusFragment
      -> StatisticsFragment
      -> MineFragment

TaskFragment
  -> TaskActionDialogFragment
  -> TaskEditActivity
  -> FocusActivity

MineFragment
  -> ProfileFragment
  -> PomodoroSettingFragment
  -> ReminderSettingFragment
  -> CheckInFragment
```

Activity 职责：

- `SplashActivity`：启动页，判断是否已有登录用户。
- `LoginActivity`：登录和注册容器。
- `MainActivity`：主界面容器，承载底部导航栏和五个主 Fragment。
- `TaskEditActivity`：新增或编辑任务。
- `FocusActivity`：专注计时和休息倒计时。

MainActivity 底部导航：

- 待办：`TaskFragment`
- 待办集：`TaskCollectionFragment`
- 锁机：`LockFocusFragment`
- 统计数据：`StatisticsFragment`
- 我的：`MineFragment`

## 4. 用户模块

用户模块采用本地账号，不接服务器。

页面：

- `SplashActivity`
- `LoginActivity`
- `LoginFragment`
- `RegisterFragment`

核心功能：

- 注册本地账号。
- 登录本地账号。
- 保存当前登录用户。
- 退出登录。
- 下次打开 App 时自动判断登录状态。

流程：

```text
打开 App
  -> SplashActivity 查询是否有登录用户
      -> 有登录用户：进入 MainActivity
      -> 无登录用户：进入 LoginActivity

LoginFragment 登录成功
  -> 保存登录状态
  -> 进入 MainActivity

MineFragment 点击退出登录
  -> 清除登录状态
  -> 返回 LoginActivity
```

数据表：`user`

| 字段 | 说明 |
| --- | --- |
| id | 用户 ID |
| username | 用户名 |
| password | 密码 |
| nickname | 昵称 |
| avatar | 头像路径或资源名 |
| profile | 个人简介 |
| is_login | 是否为当前登录用户 |
| created_time | 注册时间 |

对应类：

- `User`
- `UserDao`
- `UserRepository`
- `UserViewModel`

## 5. 待办首页模块

待办首页对应参考图 1。

页面：

- `TaskFragment`
- `TaskAdapter`
- `TaskActionDialogFragment`
- `TaskEditActivity`

页面内容：

- 顶部标题：待办
- 学霸模式入口按钮
- 顶部功能图标：统计、计时、添加、更多
- 任务卡片列表
- 底部导航栏

任务卡片内容：

- 任务名称，例如“面试”“电路分析”“复试”
- 计时模式，例如“正向计时”
- 背景图
- 右侧“开始”按钮

任务卡片交互：

```text
点击任务卡片主体
  -> 打开 TaskActionDialogFragment

点击任务卡片右侧“开始”
  -> 进入 FocusActivity
  -> 开始该任务的专注计时
```

注意事项：

- 卡片主体点击和开始按钮点击必须分开处理。
- 点击卡片主体不能直接开始计时。
- 点击开始按钮不能弹出任务操作面板。
- `TaskAdapter` 中需要分别设置 `itemView.setOnClickListener` 和 `startButton.setOnClickListener`。

任务筛选：

- 全部任务
- 待完成任务
- 已完成任务

任务排序：

- 支持通过 `sort_order` 字段控制显示顺序。
- 课程设计阶段可以先实现简单排序，例如按 `sort_order` 和创建时间排序。

## 6. 任务操作弹窗模块

任务操作弹窗对应参考图 2。

页面：

- `TaskActionDialogFragment`

形式：

- 可以使用 `DialogFragment`
- 也可以使用 `BottomSheetDialogFragment`
- 背景页面需要变暗
- 弹窗显示在当前任务列表页面上方

展示内容：

- 当前任务名称
- 当前任务背景图
- 定时功能入口
- 更换背景入口
- 独立白名单入口
- 编辑按钮
- 排序或移动按钮
- 删除按钮
- 专注历史记录入口
- 数据统计入口
- 周热力图
- 累计专注次数
- 累计专注分钟数
- 定时功能提示

按钮功能：

- 编辑：进入 `TaskEditActivity`，传入当前任务 ID。
- 排序或移动：修改当前任务的 `sort_order` 或所属待办集。
- 删除：删除当前任务。
- 专注历史记录：查看当前任务的专注记录。
- 数据统计：查看当前任务的专注统计。
- 更换背景：修改任务背景图。
- 定时功能：设置任务提醒。

删除要求：

- 只删除当前明确点击的一个任务。
- 不做批量删除。
- 删除前可以弹出确认框。

## 7. 任务新增和编辑模块

页面：

- `TaskEditActivity`

进入方式：

```text
TaskFragment 点击顶部添加按钮
  -> 打开 TaskEditActivity
  -> 新增任务

TaskActionDialogFragment 点击编辑
  -> 打开 TaskEditActivity
  -> 编辑已有任务
```

新增任务需要填写：

- 任务名称
- 任务描述
- 任务背景图
- 计时模式：正向计时或倒计时
- 默认专注时长
- 默认休息时长
- 提醒时间
- 优先级
- 所属待办集

编辑任务：

- 根据传入的 `taskId` 查询任务详情。
- 将原有数据显示到表单。
- 用户修改后保存更新。

数据表：`task`

| 字段 | 说明 |
| --- | --- |
| id | 任务 ID |
| user_id | 所属用户 ID |
| title | 任务名称 |
| description | 任务描述 |
| background_res | 背景资源名或路径 |
| focus_mode | 计时模式 |
| focus_minutes | 默认专注分钟数 |
| rest_minutes | 默认休息分钟数 |
| priority | 优先级 |
| status | 任务状态 |
| sort_order | 排序字段 |
| reminder_time | 提醒时间 |
| created_time | 创建时间 |
| updated_time | 更新时间 |

对应类：

- `Task`
- `TaskDao`
- `TaskRepository`
- `TaskViewModel`

## 8. 专注计时和休息倒计时模块

专注计时页面对应参考图 3。

休息倒计时页面对应参考图 4。

页面：

- `FocusActivity`

`FocusActivity` 内部状态：

- `FOCUSING`：专注中
- `PAUSED`：暂停中
- `RESTING`：休息中

从首页进入专注页：

```text
TaskFragment 点击任务卡片的“开始”按钮
  -> Intent 传入 taskId、taskTitle、focusMinutes、restMinutes、background
  -> FocusActivity 显示专注中页面
  -> 开始计时
```

专注中页面功能：

- 显示背景图。
- 显示励志语。
- 显示当前计时。
- 显示任务名称。
- 显示状态“进行中”。
- 支持暂停和继续。
- 支持结束专注。

点击结束专注后的流程：

```text
点击结束按钮
  -> 计算本次专注时长
  -> 保存 focus_record
  -> 更新任务累计专注次数和分钟数
  -> 切换为休息中状态
  -> 显示休息倒计时页面
```

休息中页面功能：

- 显示背景图。
- 显示圆形倒计时。
- 显示任务名称。
- 显示状态“休息中”。
- 支持主动结束休息。
- 休息倒计时结束后自动返回首页。

休息结束流程：

```text
休息倒计时结束
  -> FocusActivity.finish()
  -> 返回 MainActivity
  -> TaskFragment 刷新任务列表

用户主动点击结束休息
  -> FocusActivity.finish()
  -> 返回 MainActivity
  -> TaskFragment 刷新任务列表
```

数据表：`focus_record`

| 字段 | 说明 |
| --- | --- |
| id | 记录 ID |
| user_id | 所属用户 ID |
| task_id | 所属任务 ID |
| task_title | 任务名称快照 |
| start_time | 专注开始时间 |
| end_time | 专注结束时间 |
| duration_minutes | 专注分钟数 |
| completed | 是否正常完成 |
| created_date | 日期 |

对应类：

- `FocusRecord`
- `FocusDao`
- `FocusRepository`
- `FocusViewModel`

## 9. 统计数据模块

统计数据页面从底部导航栏“统计数据”进入，对应参考统计页面。

页面：

- `StatisticsFragment`

本项目统计页只实现以下 5 个模块。

### 9.1 累计专注

展示内容：

- 当前用户累计专注次数
- 当前用户累计专注总时长

数据来源：

- 查询 `focus_record` 表中当前用户的有效专注记录。

### 9.2 当日专注

展示内容：

- 指定日期的专注次数
- 指定日期的专注总时长

功能：

- 默认显示今天。
- 支持左右切换日期。

数据来源：

- 按 `created_date` 查询 `focus_record`。

### 9.3 专注时长分布

展示内容：

- 某一天不同任务的专注时长占比。
- 可以使用简单列表、柱状图或自定义 View 展示。

示例：

- 面试：30 分钟
- 电路分析：25 分钟
- 高数：15 分钟

数据来源：

- 按日期查询 `focus_record`。
- 再按 `task_id` 或 `task_title` 分组统计。

### 9.4 本月专注时段分布

展示内容：

- 本月不同时间段的专注分钟数。

时间段固定划分：

- 深夜：00:00 到 06:00
- 上午：06:00 到 12:00
- 下午：12:00 到 18:00
- 晚上：18:00 到 24:00

数据来源：

- 查询本月 `focus_record`。
- 根据 `start_time` 所属小时划分时段。

### 9.5 月度专注统计

展示内容：

- 本月每天的专注总时长。
- 可用简单折线图或柱状图展示。

数据来源：

- 查询本月 `focus_record`。
- 按日期分组统计分钟数。

暂不实现的统计内容：

- 总球打卡分布
- App 前台运行时间
- 睡眠打卡分布
- 本月打断原因分布
- 年度专注统计
- 统计图表设置

对应类：

- `StatisticsViewModel`
- `StatisticsRepository`
- `StatisticsCard`
- `TaskFocusDistribution`
- `MonthlyFocusItem`

建议方法：

- `loadTotalFocus()`
- `loadDailyFocus(String date)`
- `loadDailyTaskDistribution(String date)`
- `loadMonthlyTimePeriodDistribution(String month)`
- `loadMonthlyFocusTrend(String month)`

## 10. 待办集模块

页面：

- `TaskCollectionFragment`

功能：

- 展示任务分类或待办集。
- 支持查看某个待办集下的任务。
- 支持任务移动到其他待办集。

课程设计简化方案：

- 可以先内置默认待办集。
- 任务表中使用 `collection_id` 或简单的 `collection_name` 保存所属分类。
- 如果时间不够，待办集页面可以只做展示和筛选，不做复杂管理。

可选数据表：`task_collection`

| 字段 | 说明 |
| --- | --- |
| id | 待办集 ID |
| user_id | 所属用户 ID |
| name | 待办集名称 |
| sort_order | 排序 |
| created_time | 创建时间 |

## 11. 锁机和专注入口模块

页面：

- `LockFocusFragment`

功能：

- 展示锁机或专注模式入口。
- 可以选择一个任务开始专注。
- 可以使用默认番茄钟开始一次不关联任务的专注。

课程设计简化方案：

- 不真正实现系统级锁机。
- 页面中说明当前为应用内专注模式。
- 点击开始后进入 `FocusActivity`。
- 专注期间通过全屏页面减少干扰。

## 12. 学习打卡模块

页面入口：

- 可以从 `MineFragment` 进入 `CheckInFragment`。
- 也可以在后续放到待办集或首页更多菜单中。

页面：

- `CheckInFragment`

功能：

- 每日打卡。
- 当天只能打卡一次。
- 支持填写学习心得。
- 展示累计打卡天数。
- 展示连续打卡天数。

打卡流程：

```text
进入 CheckInFragment
  -> 查询今天是否已经打卡
      -> 已打卡：显示今日已打卡
      -> 未打卡：显示打卡按钮

点击打卡
  -> 保存 check_in 记录
  -> 刷新累计打卡和连续打卡天数
```

数据表：`check_in`

| 字段 | 说明 |
| --- | --- |
| id | 打卡 ID |
| user_id | 所属用户 ID |
| check_date | 打卡日期 |
| content | 学习心得 |
| created_time | 创建时间 |

对应类：

- `CheckIn`
- `CheckInDao`
- `CheckInRepository`
- `CheckInViewModel`

## 13. 我的和设置模块

页面：

- `MineFragment`
- `ProfileFragment`
- `PomodoroSettingFragment`
- `ReminderSettingFragment`

`MineFragment` 展示内容：

- 用户昵称
- 用户头像占位
- 累计专注时长
- 累计打卡天数
- 个人资料入口
- 番茄钟设置入口
- 提醒设置入口
- 学习打卡入口
- 退出登录按钮

`ProfileFragment` 功能：

- 修改昵称
- 修改头像占位
- 修改个人简介

`PomodoroSettingFragment` 功能：

- 设置默认专注时长
- 设置默认休息时长
- 设置长休息时长
- 设置长休息间隔

`ReminderSettingFragment` 功能：

- 开启或关闭任务提醒
- 开启或关闭专注结束提醒
- 开启或关闭每日打卡提醒
- 设置每日提醒时间

数据表：`settings`

| 字段 | 说明 |
| --- | --- |
| id | 设置 ID |
| user_id | 所属用户 ID |
| default_focus_minutes | 默认专注分钟数 |
| default_rest_minutes | 默认休息分钟数 |
| long_rest_minutes | 长休息分钟数 |
| long_rest_interval | 长休息间隔 |
| task_reminder_enabled | 是否开启任务提醒 |
| focus_end_reminder_enabled | 是否开启专注结束提醒 |
| check_in_reminder_enabled | 是否开启打卡提醒 |
| daily_reminder_time | 每日提醒时间 |

对应类：

- `Settings`
- `SettingsDao`
- `SettingsRepository`
- `SettingsViewModel`

## 14. 本地提醒模块

提醒方式：

- 使用 Android 本地通知。
- 可以配合 `AlarmManager` 触发提醒。
- 可以使用 `NotificationManager` 显示通知。

提醒类型：

- 任务提醒
- 专注结束提醒
- 每日打卡提醒

实现要求：

- Android 8.0 及以上需要创建通知渠道。
- Android 13 及以上需要申请通知权限。
- 提醒开关从 `settings` 表读取。
- 课程设计阶段优先保证能触发简单通知。

对应类：

- `NotificationHelper`
- `ReminderReceiver`
- `ReminderRepository`

## 15. MVVM 分层设计

项目采用简单清晰版 MVVM。

```text
ui
  activity
  fragment
  adapter
  dialog

viewmodel
  UserViewModel
  TaskViewModel
  FocusViewModel
  StatisticsViewModel
  CheckInViewModel
  SettingsViewModel

repository
  UserRepository
  TaskRepository
  FocusRepository
  StatisticsRepository
  CheckInRepository
  SettingsRepository

database
  TomatoDbHelper
  dao
  model
```

各层职责：

- Activity 和 Fragment：负责界面显示、点击事件、页面跳转。
- ViewModel：负责页面逻辑、表单校验、调用 Repository。
- Repository：负责统一管理某个模块的数据操作。
- DAO：负责 SQLite 增删改查。
- Model：普通 Java 实体类，表示数据库中的数据。

Repository 示例职责：

- `UserRepository`：注册、登录、退出登录、获取当前用户。
- `TaskRepository`：新增任务、编辑任务、删除任务、查询任务列表。
- `FocusRepository`：保存专注记录、查询任务专注记录。
- `StatisticsRepository`：查询累计专注、当日专注、月度统计。
- `CheckInRepository`：保存打卡记录、判断今日是否已打卡。
- `SettingsRepository`：保存和读取设置。

## 16. 数据库设计汇总

需要创建的数据表：

- `user`
- `task`
- `focus_record`
- `check_in`
- `settings`
- `task_collection`，可选

数据库帮助类：

- `TomatoDbHelper extends SQLiteOpenHelper`

`TomatoDbHelper` 职责：

- 创建数据库。
- 创建所有数据表。
- 数据库版本升级。
- 提供可读写数据库对象。

DAO 职责：

- 每个 DAO 只负责一类表。
- SQL 语句尽量简单。
- 查询方法命名清楚。
- 复杂统计查询需要添加中文注释。

## 17. 页面跳转关系

启动和登录：

```text
SplashActivity
  -> LoginActivity
  -> MainActivity
```

登录注册：

```text
LoginActivity
  -> LoginFragment
  -> RegisterFragment
```

主页底部导航：

```text
MainActivity
  -> TaskFragment
  -> TaskCollectionFragment
  -> LockFocusFragment
  -> StatisticsFragment
  -> MineFragment
```

任务相关：

```text
TaskFragment
  -> TaskActionDialogFragment
  -> TaskEditActivity
  -> FocusActivity
```

专注流程：

```text
TaskFragment 点击开始
  -> FocusActivity 专注中
  -> 点击结束
  -> FocusActivity 休息中
  -> 休息结束
  -> 返回 TaskFragment
```

设置相关：

```text
MineFragment
  -> ProfileFragment
  -> PomodoroSettingFragment
  -> ReminderSettingFragment
  -> CheckInFragment
```

## 18. 实现顺序

建议按下面顺序实现，先保证主流程跑通，再补充统计和设置。

1. 创建 Android 工程。（已完成）
2. 配置 Java、AndroidX、Material 底部导航。（已完成）
3. 创建基础包结构。
4. 创建 `TomatoDbHelper`。
5. 完成 `user`、`task`、`focus_record`、`check_in`、`settings` 表结构。
6. 创建 Model、DAO、Repository。
7. 实现 `SplashActivity` 登录状态判断。
8. 实现 `LoginActivity`、`LoginFragment`、`RegisterFragment`。
9. 实现注册、登录、退出登录。
10. 实现 `MainActivity` 和底部导航栏。
11. 实现 `TaskFragment` 任务列表。
12. 实现 `TaskAdapter` 任务卡片。
13. 实现任务新增和编辑。
14. 实现点击任务卡片弹出 `TaskActionDialogFragment`。
15. 实现任务删除、编辑入口、累计专注展示。
16. 实现点击“开始”进入 `FocusActivity`。
17. 实现专注计时、暂停、继续、结束。
18. 实现结束专注后保存 `focus_record`。
19. 实现休息倒计时。
20. 实现休息结束或主动结束后返回首页。
21. 实现统计数据页 5 个模块。
22. 实现学习打卡模块。
23. 实现我的页面和设置页面。
24. 实现本地通知提醒。
25. 统一检查页面跳转、数据保存、统计结果。
26. 添加关键流程中文注释。
27. 做课程答辩演示测试。

## 19. 测试计划

用户流程：

- 注册新账号成功。
- 重复用户名不能注册。
- 登录成功后进入主页。
- 退出登录后返回登录页。
- 重新打开 App 能根据登录状态跳转。

任务流程：

- 新增任务成功。
- 编辑任务成功。
- 删除单个任务成功。
- 首页显示任务卡片。
- 点击任务卡片主体弹出任务操作面板。
- 点击任务卡片“开始”进入专注页面。

专注和休息流程：

- 专注页面能正常计时。
- 暂停和继续功能正常。
- 点击结束后保存专注记录。
- 结束专注后进入休息页面。
- 休息倒计时结束后返回首页。
- 主动结束休息后返回首页。

统计流程：

- 累计专注次数正确。
- 累计专注时长正确。
- 当日专注次数正确。
- 当日专注时长正确。
- 专注时长分布能按任务显示。
- 本月专注时段分布能按时间段显示。
- 月度专注统计能按日期显示。

打卡流程：

- 当天未打卡时可以打卡。
- 当天已打卡时不能重复打卡。
- 累计打卡天数正确。
- 连续打卡天数正确。

设置和提醒流程：

- 修改昵称后能保存。
- 修改默认专注时长后能生效。
- 修改默认休息时长后能生效。
- 开启提醒后能触发本地通知。
- 关闭提醒后不再触发通知。

## 20. 验收标准

项目完成后应满足以下标准：

- App 能正常启动。
- 未登录时进入登录页。
- 已登录时进入主页。
- 可以注册、登录、退出登录。
- 主页显示类似参考图 1 的任务卡片列表。
- 点击任务卡片主体能弹出类似参考图 2 的任务操作面板。
- 点击任务卡片“开始”能进入类似参考图 3 的专注计时页。
- 点击结束专注后能进入类似参考图 4 的休息倒计时页。
- 休息结束或主动结束休息后能回到主页。
- 专注记录能保存到 SQLite。
- 统计数据页只展示指定的 5 类统计。
- 学习打卡能保存到 SQLite。
- 我的和设置页面能保存用户设置。
- 关键业务流程有中文注释。
- 代码结构清楚，适合课程设计答辩讲解。
