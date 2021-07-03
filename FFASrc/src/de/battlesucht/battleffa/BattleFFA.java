package de.battlesucht.battleffa;

import de.battlesucht.api.utils.player.Language;
import de.battlesucht.api.utils.server.global.BitsAPI;
import de.battlesucht.battleffa.utils.InventoryHandler;
import java.util.HashMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class BattleFFA extends JavaPlugin {
    public static Plugin plugin;

    public void onEnable() {
        plugin = (Plugin)this;
        update(plugin);
        Bukkit.getPluginManager().registerEvents((Listener)new InventoryHandler(), (Plugin)this);
    }

    public void onDisable() {}

    public static void update(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                Bukkit.getWorld("world").setTime(2000L);
                Bukkit.getWorld("world").setThunderDuration(0);
                Bukkit.getWorld("world").setThundering(false);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Language.prefix +"§cTeams verboten!"));
                    if (p.getLocation().getY() <= 43.0D) {
                        if (p.getInventory().contains(Material.CHEST))
                            InventoryHandler.setInventory(p, "down");
                        continue;
                    }
                    if (p.getLocation().getY() >= 43.0D &&
                            p.getInventory().contains(Material.STICK))
                        InventoryHandler.setInventory(p, "up");
                }
                for (Scoreboard board : BattleFFA.boards.keySet()) {
                    Player p = BattleFFA.boards.get(board);
                    board.getTeam("bits").setSuffix("§7"+BitsAPI.getBits(p));
                    board.getTeam("kills").setSuffix("§7"+p.getStatistic(Statistic.PLAYER_KILLS));
                    board.getTeam("tode").setSuffix("§7"+p.getStatistic(Statistic.DEATHS));
                }
            }
        }, 0L, 40L);
    }

    private static HashMap<Scoreboard, Player> boards = new HashMap<>();

    public static void setSidebar(Player p) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("aaa", "bbb");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§8»§b§lBATTLE§5§lSUCHT§8«");
        obj.getScore("§5").setScore(12);
        obj.getScore("§8» §5Profil").setScore(11);
        obj.getScore("§7● " + p.getName()).setScore(10);
        obj.getScore("§1").setScore(9);
        obj.getScore("§8» §5Tode").setScore(8);
        obj.getScore("§2").setScore(6);
        obj.getScore("§8» §5Kills").setScore(5);
        obj.getScore("§3").setScore(3);
        obj.getScore("§8» §5Bits").setScore(2);
        obj.getScore("§4").setScore(0);
        Team tode = board.registerNewTeam("tode");
        tode.setPrefix("§7● ");
        tode.setSuffix("§7"+p.getStatistic(Statistic.DEATHS));
        tode.addEntry(ChatColor.RED.toString());
        Team kills = board.registerNewTeam("kills");
        kills.setPrefix("§7● ");
        kills.setSuffix("§7"+p.getStatistic(Statistic.PLAYER_KILLS));
        kills.addEntry(ChatColor.BLACK.toString());
        Team clouds = board.registerNewTeam("bits");
        clouds.setPrefix("§7● ");
        clouds.setSuffix("§7"+BitsAPI.getBits(p));
        clouds.addEntry(ChatColor.AQUA.toString());
        obj.getScore(ChatColor.AQUA.toString()).setScore(1);
        obj.getScore(ChatColor.BLACK.toString()).setScore(4);
        obj.getScore(ChatColor.RED.toString()).setScore(7);
        boards.put(board, p);
        p.setScoreboard(board);
    }
}
