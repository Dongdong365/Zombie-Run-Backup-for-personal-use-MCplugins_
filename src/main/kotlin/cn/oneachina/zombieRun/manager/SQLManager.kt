package cn.oneachina.zombieRun.manager

import cn.oneachina.zombieRun.ZombieRun
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.io.FileWriter
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

class SQLManager(private val plugin: ZombieRun) {
    private var dataSource: HikariDataSource? = null
    private lateinit var table: String
    private lateinit var uuidColumn: String
    private lateinit var moneyColumn: String
    private lateinit var usernameColumn: String

    fun loadConfig() {
        val sqlFile = File(plugin.dataFolder, "sql.yml")
        if (!sqlFile.exists()) {
            sqlFile.parentFile.mkdirs()
            val defaultConfig = """
                database:
                  host: "localhost"
                  port: 3306
                  name: "survival_economy"
                  user: "root"
                  password: ""
                  table: "xconomy_data"
                  uuid_column: "uuid"
                  money_column: "money"
                  username_column: "username"
            """.trimIndent()
            FileWriter(sqlFile).use { it.write(defaultConfig) }
            plugin.logger.info("已创建默认 sql.yml 配置文件，请修改后重载插件。")
        }

        val config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(sqlFile)
        val db = config.getConfigurationSection("database") ?: run {
            plugin.logger.severe("sql.yml 中缺少 database 配置节！")
            return
        }

        val host = db.getString("host") ?: "localhost"
        val port = db.getInt("port", 3306)
        val database = db.getString("name") ?: "survival_economy"
        val user = db.getString("user") ?: "root"
        val password = db.getString("password") ?: ""
        table = db.getString("table") ?: "xconomy_data"
        uuidColumn = db.getString("uuid_column") ?: "uuid"
        moneyColumn = db.getString("money_column") ?: "money"
        usernameColumn = db.getString("username_column") ?: "username"

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://$host:$port/$database?useSSL=false&serverTimezone=UTC"
            this.username = user
            this.password = password
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000
        }
        dataSource = HikariDataSource(hikariConfig)
        plugin.logger.info("SQLManager initialized with table $table")
    }

    fun getConnection(): Connection? = try {
        dataSource?.connection
    } catch (e: SQLException) {
        plugin.logger.severe("Failed to get database connection: ${e.message}")
        null
    }

    fun addMoney(playerUUID: String, playerName: String, amount: Double): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            getConnection()?.use { conn ->
                val checkSql = "SELECT $moneyColumn FROM $table WHERE $uuidColumn = ?"
                conn.prepareStatement(checkSql).use { stmt ->
                    stmt.setString(1, playerUUID)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        val updateSql = "UPDATE $table SET $moneyColumn = $moneyColumn + ?, $usernameColumn = ? WHERE $uuidColumn = ?"
                        conn.prepareStatement(updateSql).use { updateStmt ->
                            updateStmt.setDouble(1, amount)
                            updateStmt.setString(2, playerName)
                            updateStmt.setString(3, playerUUID)
                            return@supplyAsync updateStmt.executeUpdate() > 0
                        }
                    } else {
                        val insertSql = "INSERT INTO $table ($uuidColumn, $usernameColumn, $moneyColumn) VALUES (?, ?, ?)"
                        conn.prepareStatement(insertSql).use { insertStmt ->
                            insertStmt.setString(1, playerUUID)
                            insertStmt.setString(2, playerName)
                            insertStmt.setDouble(3, amount)
                            return@supplyAsync insertStmt.executeUpdate() > 0
                        }
                    }
                }
            } ?: false
        }
    }

    fun close() {
        dataSource?.close()
    }
}