package cn.oneachina.zombieRun.model

import cn.oneachina.zombieRun.manager.GameManager

class Button(
    val name: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val mode: String,               // normal, tp, escape
    val doorNumber: Int? = null,     // for normal mode
    val areaDoorNumber: Int? = null, // for tp mode (area door number)
    val doorNumbers: List<Int>? = null, // for tp mode (controlled doors)
    val playerTargetX: Int? = null,  // for tp mode (human target)
    val playerTargetY: Int? = null,
    val playerTargetZ: Int? = null,
    val zombieTargetX: Int? = null,  // for tp mode (zombie target)
    val zombieTargetY: Int? = null,
    val zombieTargetZ: Int? = null
) {

    fun matches(x: Int, y: Int, z: Int): Boolean {
        return this.x == x && this.y == y && this.z == z
    }

    fun isNormal(): Boolean = mode.equals("normal", ignoreCase = true)
    fun isTp(): Boolean = mode.equals("tp", ignoreCase = true)
    fun isEscape(): Boolean = mode.equals("escape", ignoreCase = true)

    fun getTargetForTeam(team: GameManager.Team): Triple<Int, Int, Int>? {
        if (!isTp()) return null
        return when (team) {
            GameManager.Team.HUMAN -> {
                if (playerTargetX != null && playerTargetY != null && playerTargetZ != null)
                    Triple(playerTargetX, playerTargetY, playerTargetZ)
                else null
            }
            GameManager.Team.ZOMBIE, GameManager.Team.ZOMBIE_MAIN -> {
                if (zombieTargetX != null && zombieTargetY != null && zombieTargetZ != null)
                    Triple(zombieTargetX, zombieTargetY, zombieTargetZ)
                else null
            }
            else -> null
        }
    }

    override fun toString(): String {
        return "Button(name='$name', mode=$mode, location=($x, $y, $z), doorNumber=$doorNumber, areaDoorNumber=$areaDoorNumber, doorNumbers=$doorNumbers, playerTarget=($playerTargetX,$playerTargetY,$playerTargetZ), zombieTarget=($zombieTargetX,$zombieTargetY,$zombieTargetZ))"
    }
}