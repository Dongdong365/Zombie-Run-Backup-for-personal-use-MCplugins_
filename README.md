###由于萌新第一次开始参与代码编辑，如果遇到了某些忽视许可证的改动请提交issus来联系我改正或删库，谢谢!<br/>
---<br/>
哦天哪我干了什么，难道第  次使用github就要闯祸了吗<br/>
这是被AI进行修改的*自用*“分叉”(我这么叫对吗..)<br/>
因为我的文件经常太久了就找不到地方了<br/>
所以才弄了个这个。<br/>
<br/>
## 我改变了什么<br/>
 - 与原版相同但修复了粒子在paper1.21.111的刷屏报错<br/>
 - 并使用ai加上了配置的注释(当然不知道能不能用了)<br/>
 - 配置内添加了母体设置(额好像也不能用喵)<br/>
 - reset-all 指令 当你退出游戏是玩家血量可能不对，应该是正常的吧我没测试，还是有别的bug我还没改到。
 - 游戏开始时传送玩家和母体 到对应重生点(我不知道这是不是修复，或许原版就有吧);
 - 修复tab补全越界崩溃;
 - 新增枪械商店GUI与配置
---<br/>
总之，这是个无能的腐竹在使用AI改来改去原版的产物<br/>
确实能用~<br/>
请不要直接使用以及编译，请编译*官方版本*和使用，因为是我自用的东西*可能*会无脑加入一些其他指令让我用的更顺手，但是权限没设置或者仅限于我。<br/>所以可能会有你所谓的“后门”。<br/>
对于直接编译的，*我不负任何由于使用我修改的插件内的新加(删改)条目带来的任何后果！！！！！*<br/>
# *不要用！或慎用！*<br/>
~你也可以无视风险继续安装~<br/>
~叠甲部分结束<br/>
---<br/>
如果有什么不对的地方请您
~联系我~  打issus 我会第一时间处理！ 谢谢!<br/>
祝大家拥有美味的一天...<br/>
---<br/>
原版的介绍一并附上<br/>
<br/><br/>
---<br/><br/>
<br/>
# Zombie_Run_BFPU Plugin

一个基于 Paper 1.21.11 的僵尸逃生小游戏插件，包含以下核心机制：

- 自动开局与倒计时（达到最小人数后自动进入准备阶段）
- 人类/僵尸/母体阵营与回合结算
- 门、按钮、区域、重生点驱动的地图流程控制
- 体力系统、硬币/击杀/感染统计
- PlaceholderAPI 占位符扩展（前缀 `zombierun`）
- 与 WeaponMechanics crackshot-guns 等等各种枪械插件 联动发放/购买枪械

## 运行环境

- Java: `21`
- 服务端: `Paper 1.21.11`（仅测试了paper1.21.11,其他的服务端不知道能不能用）
- 构建: `Gradle (Kotlin DSL)`
- 可选依赖:
  - `PlaceholderAPI`（启用占位符）

> 说明：`PlaceholderAPI` 未安装时插件会正常启动，但占位符不会注册。

## 安装与首次启动

1. 构建插件：

```bash
./gradlew.bat shadowJar
```

2. 将产物放入服务器 `plugins` 目录（通常在 `build/libs` 下，带 `-all` 的 jar）。
3. 启动服务器后，插件会生成配置文件：
   - `plugins/Zombie_Run_BFPU/config/config.yml`
   - `plugins>Zombie_Run_BFPU>config/gunandgui.yml`
4. 按你的地图修改门、按钮、区域、重生点等坐标。
5. 使用 `/zr reload` 热重载配置，或重启服务器生效。

## 命令说明

[18:46:38 INFO]: ===== 僵尸快跑命令 =====
[18:46:38 INFO]: 玩家命令:
[18:46:38 INFO]: /zr shop - 打开枪械商店 (选择你喜欢的枪械)
[18:46:38 INFO]: /zr coins - 查看你的硬币余额
[18:46:38 INFO]: /zr coins <玩家> - 查看他人硬币余额
[18:46:38 INFO]: /zr transfer <玩家> <金额> - 转账硬币给其他玩家
[18:46:38 INFO]: /zr reset-select - 清除你的枪械选择
[18:46:38 INFO]: /zr lobby - 返回大厅
[18:46:38 INFO]: 
[18:46:38 INFO]: 管理员命令:
[18:46:38 INFO]: /zr start - 开始游戏
[18:46:38 INFO]: /zr spawn add <名称> <类型> - 添加重生点
[18:46:38 INFO]: /zr spawn remove <名称> - 删除重生点
[18:46:38 INFO]: /zr spawn list - 列出重生点
[18:46:38 INFO]: /zr doors add ... - 添加门
[18:46:38 INFO]: /zr doors remove <名称> - 删除门
[18:46:38 INFO]: /zr doors list - 列出所有门
[18:46:38 INFO]: /zr buttons add ... - 添加按钮
[18:46:38 INFO]: /zr buttons remove <名称> - 删除按钮
[18:46:38 INFO]: /zr buttons list - 列出所有按钮
[18:46:38 INFO]: /zr reload - 重载配置
[18:46:38 INFO]: /zr open - 开始游戏
[18:46:38 INFO]: /zr close - 结束游戏
[18:46:38 INFO]: /zr coins set <玩家> <金额> - 设置玩家硬币
[18:46:38 INFO]: /zr coins add <玩家> <金额> - 给玩家添加硬币
[18:46:38 INFO]: /zr coins take <玩家> <金额> - 扣除玩家硬币
[18:46:38 INFO]: /zr reset-all - 清除所有玩家身份与游戏数据(保留硬币)

### 其他

- `/doorperf`：门区域检测性能测试（管理员）

## 配置结构速览 (这里我准备修改了 但是没有太理解这个插件 当我理解了以后会更改这个部分 或者放到配置注释 嗯 大概吧..)

配置文件：`plugins/zombie-run/config/config.yml`

- `game`：开局人数、开始延迟、最大回合时长
- `spawn`：默认出生点（兜底）
- `doors`：门区域与行为参数（`mode`/`delay`/`material` 等）
- `buttons`：按钮定义（普通门按钮、传送按钮、撤离按钮）
- `regions`：区域房间号判定
- `respawns`：等待区、人类、僵尸、母体等重生点
- `start-effects`：开场指令序列
- `zombie` / `human` / `stamina` / `misc`：属性与体力、杂项参数
- `weapons`：枪械默认价格与按名称覆写价格

## PlaceholderAPI 占位符

> 标识符：`zombierun`  
> 示例：`%zombierun_human_count%`

| 占位符                                     | 描述                       | 是否需要玩家 | 示例                       |
|-----------------------------------------|--------------------------|--------|--------------------------|
| `%zombierun_human_count%`                 | 当前人类数量                   | 否      | 3                        |
| `%zombierun_zombie_count%`                | 当前僵尸数量（含母体）              | 否      | 5                        |
| `%zombierun_alpha_zombie_name%`           | 母体玩家名                    | 否      | Steve                    |
| `%zombierun_alpha_zombie_health%`         | 母体当前血量（整数）               | 否      | 450                      |
| `%zombierun_alpha_zombie_max_health%`     | 母体最大血量                   | 否      | 500                      |
| `%zombierun_alpha_zombie_health_percent%` | 母体血量百分比（0.0~1.0）         | 否      | 0.9                      |
| `%zombierun_game_state%`                  | 游戏状态英文大写                 | 否      | RUNNING                  |
| `%zombierun_game_state_formatted%`        | 游戏状态带颜色中文                | 否      | §a进行中                    |
| `%zombierun_time_left%`                   | 游戏剩余秒数（仅运行中）             | 否      | 120                      |
| `%zombierun_time_left_formatted%`         | 格式化剩余时间 mm:ss            | 否      | 02:00                    |
| `%zombierun_min_players%`                 | 最小开始人数                   | 否      | 8                        |
| `%zombierun_max_players%`                 | 最大玩家数                    | 否      | 32                       |
| `%zombierun_online_players%`              | 当前在线玩家总数                 | 否      | 15                       |
| `%zombierun_progress%`                    | 进度（等待时为在线/最少，运行时为已过/总时长） | 否      | 0.5                      |
| `%zombierun_bossbar%`                     | 整合状态信息（含颜色格式，适合 BossBar） | 否      | `<aqua>人类: 3 <red>僵尸: 5` |
| `%zombierun_coins%`                       | 玩家硬币数量                   | 是      | 1200                     |
| `%zombierun_kills%`                       | 玩家击杀僵尸数                  | 是      | 7                        |
| `%zombierun_infections%`                  | 玩家感染人类数                  | 是      | 3                        |
| `%zombierun_selected_weapon%`             | 玩家选择枪械编号（未选为 0）          | 是      | 5                        |
| `%zombierun_room%`                        | 玩家当前房间号                  | 是      | 3                        |
| `%zombierun_team%`                        | 玩家队伍英文大写                 | 是      | HUMAN                    |
| `%zombierun_team_formatted%`              | 玩家队伍带颜色中文                | 是      | §b人类                     |
| `%zombierun_stamina%`                     | 玩家当前体力值（整数）              | 是      | 80                       |
| `%zombierun_max_stamina%`                 | 玩家最大体力值                  | 是      | 100                      |
| `%zombierun_stamina_percent%`             | 玩家体力百分比                  | 是      | 0.8                      |
| `%zombierun_stamina_bar%`                 | 玩家体力条（20 格，带颜色）          | 是      | `§a████████████████§7████` |
| `%zombierun_stamina_state%`               | 体力状态（1 正常，2 耗尽）          | 是      | 1                        |

## 参与开发

请前往"CoffeePopStudio/Zombie-Run"这里可能不支持哦。

## 常见问题

- 枪械列表不对或不发武器：请确认 gunandgui 或 guiandgun 的配置文件是否设置正确。
- 占位符无效：请确认 `PlaceholderAPI` 已安装并在插件启动后注册成功。
- 人数达到后不自动开始：检查 `game.min-players`、`game.start-delay` 以及玩家是否在线于同一服务器实例。
| `%zombierun_stamina_state%`               | 体力状态（1 正常，2 耗尽）          | 是      | 1                        |

## 参与开发


