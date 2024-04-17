package ban.scheck;

import ban.scheck.impl.Check;
import ban.scheck.impl.Events;
import ban.scheck.impl.commands.AdminsCommand;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
public class SCheck extends JavaPlugin implements Listener {

    public static FileConfiguration config;
    public static final String NOTIFY_PERMISSION = "SCheck.notify";
    public static final String anticheck = "SCheck.anticheck";

    public static final Map<Player, Player> checkRequests = new HashMap<>();
    public static String banReason;
    public static boolean Quit;
    public static String banQuit;
    public static String bancommand;
    public static int banforQuit;
    public static int banDuration;
    public static String banIgnore;
    public static Map<Player, BossBar> bossBars = new HashMap<>();
    public static Map<Player, Boolean> StopTimer = new HashMap<>();
    public static Map<Player, BukkitRunnable> timers = new HashMap<>();


    @Override
    public void onEnable() {
        getCommand("check").setExecutor(new Check());
        getCommand("scheck").setExecutor(new AdminsCommand());
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        config = this.getConfig();
        config.options().copyDefaults(true);
        this.banQuit = config.getString("Ban.banQuit");
        this.banforQuit = config.getInt("Ban.banquitday");
        this.Quit = config.getBoolean("Ban.Quit");
        this.banReason = config.getString("Ban.banReason");
        this.banDuration = config.getInt("Ban.banDuration");
        this.bancommand = config.getString("Ban.BanCommand");
        this.banIgnore = config.getString("Ban.BanIgnore");
        getServer().getLogger().info(" ");
        getServer().getLogger().info(colorize("&c » &7" + getDescription().getName() + " Включен"));
        getServer().getLogger().info(colorize("&c » &7Версия " + getDescription().getVersion()));
        getServer().getLogger().info(" ");
        this.saveConfig();

    }
    @Override
    public void onDisable() {
        bossBars.clear();
        checkRequests.clear();
        StopTimer.clear();
        getServer().getLogger().info(" ");
        getServer().getLogger().info(colorize("&c » &7" + getDescription().getName() + " Выключен"));
        getServer().getLogger().info(colorize("&c » &7Версия " + getDescription().getVersion()));
        getServer().getLogger().info(" ");
    }


    public static String colorize(String msg) {
        String coloredMsg = "";
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == '&')
                coloredMsg += '§';
            else
                coloredMsg += msg.charAt(i);
        }
        return coloredMsg;
    }
}



