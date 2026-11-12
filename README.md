# 山海同契 · 清风入卷 0.2.1

原生 Android 横屏离线策略卡牌游戏。Java + Android Canvas，无 WebView，无联网权限。

## 0.2.1 重制内容
- 四位完全重绘的拟人角色：青丘直视含笑、应龙侧身沉思、精卫迎风回身、烛阴端坐凝视。清透自然肤色，去掉上一版统一暗黄调色及精卫举石的直白表达。
- 全新山水牌桌、环桌座位、边框与层次完整的按钮和弹窗；按压、禁用、选中状态。
- 再点取消选牌、换牌清空目标、合法目标亮框、长按牌/角色详情、结束回合确认、重开确认。
- 14 种术牌 / 114 张牌：新增破障、援契、封潮、共生，支持装备破除、同伴补牌/恢复和摸牌抑制。
- 出牌记录、事件呈现队列、抽牌位移、指向线、伤害数字、技能提示、托管、三档节奏。
- 15 类声音事件、分层合成音色与重新编排的 72 秒背景音乐；音乐/音效各八档音量、音频焦点处理。
- 自动存档、规则、神祇和术牌图鉴、历史战报翻页、结算、离场后观战。

## 安装
Android 6.0（API 23）以上，目标 API 35。交付 APK 沿用 0.1.0 的开发签名，可直接覆盖安装。
0.2.1 的规则存档结构已经更新，0.1.0 未结束的对局不迁移，需要重新开局；设置和累计战绩保留。
这是四人 2v2 离线游戏，目前不含真人联网、真人配音及商业级骨骼动画。

## 构建
安装 JDK 17+ 和 Android SDK（platforms;android-35、build-tools;35.0.0）：
```sh
export ANDROID_SDK_ROOT=/path/to/android-sdk
bash build.sh
```
输出 `build/shanhai-tongqi-0.2.1.apk`，无需 Gradle 或额外 Maven 依赖。
BUILD_TOOLS、ANDROID_JAR、JAVAC 可覆盖具体路径。首次本地构建会生成 signing.p12，请私下保留自己的签名文件；签名文件不提交到仓库。CI 使用临时开发签名，其 APK 不能覆盖由另一个密钥签名的安装包。

## 测试
```sh
mkdir -p build/test
javac -encoding UTF-8 -d build/test app/src/main/java/cn/shanhai/game/Engine.java app/src/main/java/cn/shanhai/game/Board.java tests/*.java
java -cp build/test cn.shanhai.game.RulesTest
java -cp build/test cn.shanhai.game.InteractionTest
java -cp build/test cn.shanhai.game.PolishTest
java -Djava.awt.headless=true -cp build/test cn.shanhai.game.RenderPreview
```
GitHub Actions 另有 Android API 29 模拟器安装、启动、选将、规则、设置和进程重启后的恢复检查，结果以 Actions 实际日志及截图为准。桌面预览使用相同 Board 绘制逻辑，但不代替设备测试。

## 结构
- Engine.java：平台无关规则、目标合法性、启发式 AI、可序列化状态与事件。
- Board.java：1280×720 界面和点击区域，自绘控件与交互状态。
- MainActivity.java：Android 生命周期、Canvas、音频焦点、存档、长按及触摸。
- tools/audio.py：原创程序音频生成，需要 Python、numpy、scipy。
- tests/：规则、按钮路径、交互状态回归、桌面预览。

## 参考与素材
参考了以下公开项目中的牌桌布局、选中/取消与目标检查、信息弹层和按事件触发音效的设计。未复制其源码、美术或音频：
- https://github.com/libnoname/noname （apps/core/noname/ui/click/index.js、apps/core/card/standard.js）
- https://github.com/zziyoo/decadeUI （src/overrides/ui/click.js、src/core/layout.js、src/styles/dialog.css、src/audio/audioHooks.js）

角色与场景是为本项目生成的原创视觉诠释。字体为 Noto Serif CJK 字符子集，许可见 licenses/Noto-CJK.txt。
