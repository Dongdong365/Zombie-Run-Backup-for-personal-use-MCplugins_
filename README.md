# Zombie Run Plugin

| 占位符                                     | 描述                       | 是否需要玩家 | 示例                       |
|-----------------------------------------|--------------------------|--------|--------------------------|
| %zombierun_human_count%                 | 当前人类数量                   | 否      | 3                        |
| %zombierun_zombie_count%                | 当前僵尸数量（含母体）              | 否      | 5                        |
| %zombierun_alpha_zombie_name%           | 母体玩家名                    | 否      | Steve                    |
| %zombierun_alpha_zombie_health%         | 母体当前血量（整数）               | 否      | 450                      |
| %zombierun_alpha_zombie_max_health%     | 母体最大血量                   | 否      | 500                      |
| %zombierun_alpha_zombie_health_percent% | 母体血量百分比（0.0~1.0）         | 否      | 0.9                      |
| %zombierun_game_state%                  | 游戏状态英文大写                 | 否      | RUNNING                  |
| %zombierun_game_state_formatted%        | 游戏状态带颜色中文                | 否      | §a进行中                    |
| %zombierun_time_left%                   | 游戏剩余秒数（仅运行中）             | 否      | 120                      |
| %zombierun_time_left_formatted%         | 格式化剩余时间 mm:ss            | 否      | 02:00                    |
| %zombierun_min_players%                 | 最小开始人数                   | 否      | 8                        |
| %zombierun_max_players%                 | 最大玩家数                    | 否      | 32                       |
| %zombierun_online_players%              | 当前在线玩家总数                 | 否      | 15                       |
| %zombierun_progress%                    | 进度（等待时为在线/最少，运行时为已过/总时长） | 否      | 0.5                      |
| %zombierun_bossbar%                     | 整合状态信息（含颜色格式，适合BossBar）  | 否      | <aqua>人类: 3  <red>僵尸: 5  |
| %zombierun_coins%                       | 玩家硬币数量                   | 是      | 1200                     |
| %zombierun_kills%                       | 玩家击杀僵尸数                  | 是      | 7                        |
| %zombierun_infections%                  | 玩家感染人类数                  | 是      | 3                        |
| %zombierun_selected_weapon%             | 玩家选择的枪械编号（1-12，未选为0）     | 是      | 5                        |
| %zombierun_room%                        | 玩家当前房间号                  | 是      | 3                        |
| %zombierun_team%                        | 玩家队伍英文大写                 | 是      | HUMAN                    |
| %zombierun_team_formatted%              | 玩家队伍带颜色中文                | 是      | §b人类                     |
| %zombierun_stamina%                     | 玩家当前体力值（整数）              | 是      | 80                       |
| %zombierun_max_stamina%                 | 玩家最大体力值                  | 是      | 100                      |
| %zombierun_stamina_percent%             | 玩家体力百分比                  | 是      | 0.8                      |
| %zombierun_stamina_bar%                 | 玩家体力条（20格，带颜色）           | 是      | §a████████████████§7████ |
| %zombierun_stamina_state%               | 体力状态（1正常，2耗尽）            | 是      | 1                        |
