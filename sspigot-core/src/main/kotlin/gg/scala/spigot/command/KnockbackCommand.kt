package gg.scala.spigot.command

import io.github.nosequel.command.annotation.Command
import io.github.nosequel.command.annotation.Help
import io.github.nosequel.command.annotation.Subcommand

/**
 * @author GrowlyX
 * @since 11/3/2021
 */
class KnockbackCommand
{
    @Help
    @Command(
        label = "knockback",
        aliases = ["kb"],
        permission = "scalaspigot.command.knockback"
    )
    fun onLol()
    {

    }
}
