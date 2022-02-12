package org.bukkit.craftbukkit.scoreboard;

import net.minecraft.server.*;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.WeakCollection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

public final class CraftScoreboardManager implements ScoreboardManager {
    private final CraftScoreboard mainScoreboard;
    private final MinecraftServer server;
    private final Collection<CraftScoreboard> scoreboards = new WeakCollection<CraftScoreboard>();
    private final Map<CraftPlayer, CraftScoreboard> playerBoards = new HashMap<CraftPlayer, CraftScoreboard>();

    public CraftScoreboardManager(MinecraftServer minecraftserver, Scoreboard scoreboardServer) {
        mainScoreboard = new CraftScoreboard(scoreboardServer);
        server = minecraftserver;
        scoreboards.add(mainScoreboard);
    }

    public CraftScoreboard getMainScoreboard() {
        return mainScoreboard;
    }

    public CraftScoreboard getNewScoreboard() {
        org.spigotmc.AsyncCatcher.catchOp("scoreboard creation"); // Spigot
        CraftScoreboard scoreboard = new CraftScoreboard(new ScoreboardServer(server));
        scoreboards.add(scoreboard);
        return scoreboard;
    }

    // CraftBukkit method
    public CraftScoreboard getPlayerBoard(CraftPlayer player) {
        CraftScoreboard board = playerBoards.get(player);
        return board == null ? getMainScoreboard() : board;
    }

    // CraftBukkit method
    public void setPlayerBoard(CraftPlayer player, org.bukkit.scoreboard.Scoreboard bukkitScoreboard) throws IllegalArgumentException {
        Validate.isTrue(bukkitScoreboard instanceof CraftScoreboard, "Cannot set player scoreboard to an unregistered Scoreboard");

        CraftScoreboard scoreboard = (CraftScoreboard) bukkitScoreboard;
        Scoreboard oldboard = getPlayerBoard(player).getHandle();
        Scoreboard newboard = scoreboard.getHandle();
        EntityPlayer entityplayer = player.getHandle();

        if (oldboard == newboard) {
            return;
        }

        if (scoreboard == mainScoreboard) {
            playerBoards.remove(player);
        } else {
            playerBoards.put(player, scoreboard);
        }

        // Old objective tracking
        HashSet<ScoreboardObjective> removed = new HashSet<ScoreboardObjective>();
        for (int i = 0; i < 3; ++i) {
            ScoreboardObjective scoreboardobjective = oldboard.getObjectiveForSlot(i);
            if (scoreboardobjective != null && !removed.contains(scoreboardobjective)) {
                entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardObjective(scoreboardobjective, 1));
                removed.add(scoreboardobjective);
            }
        }

        // Old team tracking
        Iterator<?> iterator = oldboard.getTeams().iterator();
        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();
            entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(scoreboardteam, 1));
        }

        // The above is the reverse of the below method.
        server.getPlayerList().sendScoreboard((ScoreboardServer) newboard, player.getHandle());
    }

    // CraftBukkit method
    public void removePlayer(Player player) {
        playerBoards.remove(player);
    }

    // CraftBukkit method
    public Collection<ScoreboardScore> getScoreboardScores(IScoreboardCriteria criteria, String name, Collection<ScoreboardScore> collection) {
        // Tuinity start - add timings for scoreboard search
        // plugins leaking scoreboards will make this very expensive, let server owners debug it easily
        co.aikar.timings.SpigotTimings.scoreboardScoreSearch.startTimingIfSync();
        try {
            // Tuinity end - add timings for scoreboard search
            for (final CraftScoreboard scoreboard : scoreboards) {
                final Scoreboard board = scoreboard.board;

                for (final ScoreboardObjective objective : board.getObjectivesForCriteria(criteria)) {
                    collection.add(board.getPlayerScoreForObjective(name, objective));
                }
            }
        } finally { // Tuinity start - add timings for scoreboard search
            co.aikar.timings.SpigotTimings.scoreboardScoreSearch.stopTimingIfSync();
        }
        // Tuinity end - add timings for scoreboard search

        return collection;
    }

    // CraftBukkit method
    public void updateAllScoresForList(IScoreboardCriteria criteria, String name, List<EntityPlayer> of) {
        for (ScoreboardScore score : getScoreboardScores(criteria, name, new ArrayList<ScoreboardScore>())) {
            score.updateForList((List) of);
        }
    }
}
