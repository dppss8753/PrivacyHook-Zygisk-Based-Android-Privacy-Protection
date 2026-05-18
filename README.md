# 🛡️ PrivacyHook

> **A Stealthy & Smart Android Privacy Protection Framework**
> 基于 Zygisk 的非侵入式、极高隐蔽性安卓隐私防护框架

[![Android API](https://img.shields.io/badge/API-30%2B-brightgreen.svg?style=flat)](#)
[![Magisk](https://img.shields.io/badge/Magisk-Zygisk-blue.svg?style=flat)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](#)

PrivacyHook 是一款针对现代高版本 Android 系统设计的底层隐私防护系统。旨在解决传统权限模型“全有或全无”的困境，并彻底攻克传统全局 Xposed 模块极易被商业风控系统（如银行、游戏 App）检测的痛点。

本项目为网络空间安全专业优秀毕业设计开源成果。

---

## ✨ 核心特性 (Core Features)

### 👻 1. 降维级隐蔽性 (Stealth Injection)
完全抛弃物理文件替换，基于 **Zygisk** 实现内存级动态寄生。配合 Mount Namespace（挂载命名空间）隔离技术，对未勾选的非目标应用实现绝对物理级隐形，完美规避反作弊 SDK 的环境检测。

### 🧠 2. 确定性逻辑欺骗 (Deterministic Fuzzing)
首创**哈希种子模糊化算法**。在处理通讯录等敏感数据时，利用真实数据的 HashCode 作为随机数引擎种子，确保每次生成的伪装数据（姓名、号码）高度稳定连贯，彻底消除 UI 列表闪烁与数据跳变异常。

### ⚡ 3. 非对称静默通信 (Asymmetric IPC Bus)
针对 Android 11+ SELinux 严格的 Enforcing 模式，创新设计基于 Linux VFS 的**非对称文件流审计总线**。无需拦截端申请高危 Binder 权限，实现零 AVC Denial 弹窗的毫秒级静默日志回传。

### 🎯 4. 硬件级载体接管 (Hardware-Level Spoofing)
* **位置重定向：** 无视 A-GPS/Wi-Fi 复合定位策略，直接下沉至 `android.location.Location` 载体对象进行坐标覆盖。
* **设备指纹伪造：** 精准拦截 `Settings.Secure`，基于时间戳注入动态 16 进制伪造指纹，阻断画像追踪。

---

## 🛠️ 环境要求 (Requirements)

* **操作系统:** Android 11+ (API 30 及以上)
* **Root 环境:** Magisk (需开启 Zygisk 模式) 或 KernelSU
* **注入框架:** LSPosed (Zygisk 版本)

---

## 🚀 安装与使用 (Installation & Usage)

1. 下载最新的 Release 压缩包或克隆本仓库并编译。
2. 在 LSPosed 管理器中启用 **PrivacyHook** 模块。
3. **关键步骤：** 在 LSPosed 的作用域中，仅勾选你需要进行隐私拦截的“目标应用”（⚠️ 强烈建议不要勾选系统核心组件）。
4. 打开 PrivacyHook 控制端主程序，配置伪造策略（如 GPS 经纬度、通讯录双语伪装规则）。
5. 点击“同步配置”，模块将在目标应用下次冷启动时自动生效。
6. 在主程序控制台实时查看底层拦截审计日志。

---

## 📸 运行截图 (Screenshots)

*(使用提示：请将你在真机或模拟器上运行的主界面截图、拦截日志截图重命名为 `screenshot1.png` 等，放入项目的 `images` 文件夹中，取消下方注释即可显示)*

---

## 📊 性能评估 (Performance)

经 AIDA64 及多款主流商业应用（如百度地图、高德地图等）实测验证：
* **拦截与接管成功率：** 100%
* **单次 Hook 系统延迟：** 约 1.2ms ~ 1.4ms（远优于传统 XPrivacy 方案的 4ms+ 与动态污点分析的 15ms+）
* **用户感知：** 极轻量级，对目标应用的冷启动速度（Cold Start）及 UI 渲染帧率（FPS）无任何可察觉的负面影响。

---

## ⚠️ 免责声明 (Disclaimer)

本项目仅供学术研究、毕业设计交流及 Android 安全防御技术探讨使用。请勿将本项目用于任何非法用途或恶意对抗正常商业软件的安全机制。因滥用本工具造成的账号封禁、数据丢失或法律纠纷，开发者概不承担任何责任。

## 📄 开源协议 (License)

本项目采用 [MIT License](LICENSE) 协议开源。

## 📄 项目声明

* 项目名称: PrivacyHook：基于 Zygisk 的隐蔽式安卓隐私防护系统
* 项目作者: Wong TszKit
* 作者单位: 暨南大学网络空间安全学院
* 开发语言: Java,XML
* 框架: Zygisk,LSPosed
* 核心技术: 内存级动态寄生与隔离,非对称 IPC 静默通信,哈希种子模糊化算法