# 怪物等级系统 TinyMobLevels

让服务器里所有敌对怪拥有 1-10 级等级体系！等级决定血量/攻击/防御/装备，击杀掉落金币，探索越远怪越强。全中文界面，中英双语命令，零依赖。

## 功能特性

- **1-10 级所有敌对怪**：僵尸/骷髅/苦力怕/蜘蛛/末影人/烈焰人/循声守卫...全部可分级
- **等级决定属性**：
  - 血量：原版 ×1.6 ~ ×10（封顶 1024，溢出折算成防御）
  - 攻击：原版 ×1.4 ~ ×7
  - 防御：玩家攻击伤害 ÷1.2 ~ ÷7
- **装备随等级**：木剑→铁剑→附魔铁剑→钻石剑锋利3；护甲皮革→铁→钻石套保护4；"全套"随机 1-4 件，武器/护甲独立掷骰
- **头顶显示**：`[Lv.N] 怪物名 血量`（中文名 + 实时血量），可选"玩家看不到怪就隐藏"防透视
- **金币掉落**：击杀直接进玩家钱包（自家 Economy → Vault），1-10 级默认 1-2 → 20-30 金币，每级可自定义
- **等级来源二选一**：随机（默认）/ 离出生点距离（每 1000 米 +1 级）
- **自定义掉落**：config 按等级配置物品+数量+概率
- **经验随等级**（可选，默认关）
- **管理命令**：召唤任意等级/类型怪，重载配置

## 命令（中英双语）

| 功能 | 中文 | 英文 |
|---|---|---|
| 查看配置 | /怪物 等级 | /mob level |
| 召唤指定等级 | /怪物 召唤 <等级> [数量] | /mob spawn <lv> [n] |
| 召唤指定类型+等级 | /怪物 召唤 <类型> <等级> [数量] | /mob spawn <type> <lv> [n] |
| 重载 | /怪物 重载 | /mob reload |

## 配置（plugins/TinyMobLevels/config.yml）

- `settings.level-source`：random（随机）/ distance（离出生点距离）
- `settings.level-display`：头顶等级显示（默认开）
- `settings.level-hide-no-los`：玩家看不到怪时隐藏头顶信息（防透视，默认开）
- `levels`：1-10 级血量/攻击/防御倍数表
- `equipment`：各等级武器/护甲配置
- `economy`：金币掉落开关 + 每级金币范围
- `drops`：自定义掉落（物品英文名+数量+概率）

## 安装

1. 下载 jar 放入 `plugins/` 目录
2. 重启服务器（或 reload）
3. 启动日志显示 TinyAII 横幅 + 怪物等级系统已启用

> 需要 Java 17+，支持 Paper/Spigot 1.16 ~ 26.2。零依赖（金币掉落需装自家 Economy 或 Vault）。

## 兼容性

- Paper / Spigot 1.16 ~ 26.2，Java 17+
- 零依赖；经济用 EcoBridge 反射（自家 Economy → Vault → 无经济则金币不可用）

---

# TinyMobLevels - Mob Level System

Give every hostile mob a 1-10 level system! Level determines health/attack/defense/equipment, coin drops on kill, explore further for stronger mobs. Full Chinese UI, bilingual commands, zero dependency.

## Features

- **1-10 levels for all hostile mobs**: zombie/skeleton/creeper/spider/enderman/blaze/warden...
- **Stats by level**: health ×1.6~10 (cap 1024, overflow to defense), attack ×1.4~7, defense = damage ÷1.2~7
- **Equipment by level**: wood→iron→enchanted→diamond sword sharpness 3; leather→iron→diamond armor protection 4; random 1-4 pieces, weapon/armor rolled independently
- **Hover display**: `[Lv.N] MobName HP` (Chinese name + live HP), optional hide-when-not-visible (anti-xray)
- **Coin drops**: direct to wallet (own Economy → Vault), level 1-10 default 1-2 → 20-30 coins, per-level configurable
- **Level source (pick one)**: random (default) / distance from spawn (+1 level per 1000 blocks)
- **Custom drops**: per-level item+amount+chance in config
- **EXP by level** (optional, off by default)
- **Admin commands**: spawn any level/type, reload

## Commands

- `/mob level`, `/mob spawn <lv> [n]`, `/mob spawn <type> <lv> [n]`, `/mob reload`

## Install

1. Put jar into `plugins/`
2. Restart server (or reload)
3. Startup log shows TinyAII banner + mob level system enabled

> Java 17+, Paper/Spigot 1.16 ~ 26.2. Zero dependency (coin drops need own Economy or Vault).

## License

MIT License - free, open source. TinyAII brand banner preserved.
