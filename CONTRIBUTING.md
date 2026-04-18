# Contributing

感谢你参与 `zombie-run` 的开发。

## 开发环境

- JDK: `21`
- 构建工具: `Gradle Wrapper`
- 推荐 IDE: `IntelliJ IDEA`（Kotlin 项目体验更好）

## 快速开始

1. 克隆仓库并进入项目目录。
2. 构建插件：

```bash
./gradlew.bat build
```

3. 启动本地 Paper 测试服（由 `run-paper` 插件拉起）：

```bash
./gradlew.bat runServer
```

4. 首次启动后，按地图实际情况修改：
   - `plugins/zombie-run/config/config.yml`

## 常用命令

```bash
./gradlew.bat clean build
./gradlew.bat shadowJar
./gradlew.bat runServer
```

- `build`: 编译并执行构建流程
- `shadowJar`: 生成可部署插件 jar
- `runServer`: 本地联调服务器

## 代码与提交建议

- 保持改动聚焦：一个 PR 只解决一类问题（功能/修复/重构）。
- 变更配置结构时，同步更新 `README.md` 的配置说明。
- 新增命令、权限、占位符时，同步更新文档。
- 提交前至少验证一次 `build` 成功。

## 提交 PR 前自检

- 能正常编译：`./gradlew.bat build`
- 核心流程可跑通（至少本地 `runServer` 冒烟）
- 相关文档已更新（`README.md` / 本文件）
