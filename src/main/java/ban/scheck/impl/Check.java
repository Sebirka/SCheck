package ban.scheck.impl;

import ban.scheck.SCheck;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static ban.scheck.SCheck.*;
import static org.bukkit.ChatColor.*;
import static org.bukkit.ChatColor.RED;

public class Check implements CommandExecutor, Listener {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(anticheck)) {
                continue;
            }
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(RED + "Команды доступны только игрокам!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("SCheck.check")) {
            player.sendMessage(RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("check")) {
            if (args.length < 1) {
                player.sendMessage(GOLD + "[Check]" + GREEN + " Команды проверки на читы:");
                player.sendMessage(GOLD + "[Check]" + YELLOW + " /check ник - вызвать игрока на проверку на читы.");
                player.sendMessage(GOLD + "[Check]" + YELLOW + " /check ник allow - разрешить игроку после успешной проверки.");
                player.sendMessage(GOLD + "[Check]" + YELLOW + " /check ник dis - забанить игрока за использование читов.");
                player.sendMessage(GOLD + "[Check]" + YELLOW + " /check ник ignore - забанить игрока за игнор проверкив.");
                player.sendMessage(GOLD + "[Check]" + YELLOW + " /check ник stop - остановить таймер.");
                player.sendMessage(GOLD + "[Check]" + YELLOW + " Создатель: Sebirka");
                return true;
            }

            Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
            if (targetPlayer == null) {
                player.sendMessage(RED + "Игрок с ником " + args[0] + " не найден.");
                return true;
            }
            if (checkRequests.containsKey(targetPlayer) && args.length == 1) {
                player.sendMessage(RED + "Вы уже вызвали этого игрока на проверку.");
                return true;
            }
            targetPlayer.setGameMode(GameMode.SURVIVAL);
            targetPlayer.setInvulnerable(true);

            if (args.length == 1) {
                BossBar bossBar = Bukkit.createBossBar("Проверка на читы", BarColor.RED, BarStyle.SOLID);
                bossBar.addPlayer(targetPlayer);
                Player moderator = player;
                bossBar.addPlayer(moderator);
                Location moderatorLocation = moderator.getLocation();
                targetPlayer.teleport(moderatorLocation);
                bossBars.put(targetPlayer, bossBar);
                checkRequests.put(targetPlayer, moderator);
                SCheck plugin = SCheck.getPlugin(SCheck.class);
                List<String> messag = plugin.getConfig().getStringList("moder.message");
                List<String> check = plugin.getConfig().getStringList("moder.check");
                check.stream().map(message -> message
                        .replace("<moderator>", moderator.getName()).replace("<target>", targetPlayer.getName())).filter(message -> player.hasPermission(NOTIFY_PERMISSION)).forEach(message -> player.sendMessage(colorize(message)));
                BukkitRunnable timer = new BukkitRunnable() {
                    int time = plugin.getConfig().getInt("timer.time");
                    public int timeLeft = time * 60;
                    public int messageCounter = 0;

                    @Override
                    public void run() {
                        if (!checkRequests.containsKey(targetPlayer)) {
                            this.cancel();
                            bossBar.removeAll();
                        }
                        timeLeft -= 1;
                        bossBar.setProgress((double) timeLeft / (time * 60));

                        if (timeLeft <= 0) {
                            this.cancel();
                            bossBar.removeAll();
                            moderator.performCommand("scheck:check " + targetPlayer.getName() + " ignore");
                        }

                        messageCounter += 1;
                        targetPlayer.sendActionBar(RED + "Следуйте инструкции в чате");
                        bossBar.setTitle("Осталось времени: " + timeLeft / 60 + " минут " + timeLeft % 60 + " секунд");
                        if (messageCounter % 5 == 0) {
                            String minutes = String.valueOf(timeLeft / 60);
                            String seconds = String.valueOf(timeLeft % 60);
                            targetPlayer.sendTitle(RED + "Проверка на читы", "", 0, 100, 0);
                            messag.stream().map(message -> message
                                            .replace("<minutes>", minutes)
                                            .replace("<seconds>", seconds)
                                            .replace("<moderator>", moderator.getName())
                                            .replace("<target>", targetPlayer.getName()))
                                    .forEach(message -> targetPlayer.sendMessage(colorize(message)));
                        }
                    }
                };
                timer.runTaskTimer(plugin, 0L, 20L);
                timers.put(targetPlayer, timer);
            } else if (args.length == 2 && args[1].equalsIgnoreCase("allow")) {
                if (checkRequests.containsKey(targetPlayer)) {
                    Player moderator = checkRequests.get(targetPlayer);
                    checkRequests.remove(targetPlayer);
                    moderator.sendMessage(GREEN + "Игрок " + targetPlayer.getName() + " успешно прошел проверку на читы.");
                    targetPlayer.sendMessage(GREEN + "Вы успешно прошли проверку на читы.");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        targetPlayer.setGameMode(GameMode.SURVIVAL);
                        targetPlayer.setInvulnerable(false);
                        SCheck plugin = SCheck.getPlugin(SCheck.class);
                        List<String> allow = plugin.getConfig().getStringList("chat.allow");
                        allow.stream().map(message -> message
                                .replace("<moderator>", moderator.getName()).replace("<target>", targetPlayer.getName()))
                                .filter(message -> p.hasPermission(NOTIFY_PERMISSION))
                                .forEach(message -> p.sendMessage(colorize(message)));
                    }
                } else {
                    player.sendMessage(RED + "Игрок не находится на проверке.");
                    return true;
                }
            } else if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
                if (checkRequests.containsKey(targetPlayer)) {
                    StopTimer.put(targetPlayer, true);
                    stopCheck(targetPlayer);

                    sender.sendMessage(ChatColor.GREEN + "Таймер для игрока " + targetPlayer.getName() + " был остановлен.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Игрок " + args[0] + " не находится на проверке.");
                }
                return false;
            } else if (args.length == 2 && args[1].equalsIgnoreCase("ignore")) {
                if (checkRequests.containsKey(targetPlayer)) {
                    Player moderator = checkRequests.get(targetPlayer);
                    checkRequests.entrySet().removeIf(entry -> entry.getValue().equals(player));
                    bancommand = SCheck.bancommand + " " + targetPlayer.getName() + " " + banDuration + "d " + banIgnore;
                    moderator.performCommand(bancommand);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        targetPlayer.setGameMode(GameMode.SURVIVAL);
                        targetPlayer.setInvulnerable(false);
                        SCheck plugin = SCheck.getPlugin(SCheck.class);

                        List<String> allow = plugin.getConfig().getStringList("moder.ignore");
                        allow.stream().map(message -> message
                                .replace("<moderator>", moderator.getName()).replace("<target>", targetPlayer.getName())).filter(message -> p.hasPermission(NOTIFY_PERMISSION)).forEach(message -> p.sendMessage(colorize(message)));
                    }
                }
            } else if (args.length == 2 && args[1].equalsIgnoreCase("dis")) {
                if (checkRequests.containsKey(targetPlayer)) {
                    targetPlayer.sendTitle(RED + "", "", 0, 0, 0);
                    Player moderator = checkRequests.get(targetPlayer);
                    checkRequests.entrySet().removeIf(entry -> entry.getValue().equals(player));
                    bancommand = SCheck.bancommand + " " + targetPlayer.getName() + " " + banDuration + "d " + banReason;
                    moderator.performCommand(bancommand);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        targetPlayer.setGameMode(GameMode.SURVIVAL);
                        targetPlayer.setInvulnerable(false);
                        SCheck plugin = SCheck.getPlugin(SCheck.class);
                        List<String> allow = plugin.getConfig().getStringList("moder.dis");
                        allow.stream().map(message -> message
                                .replace("<moderator>", moderator.getName()).replace("<target>", targetPlayer.getName())).filter(message -> p.hasPermission(NOTIFY_PERMISSION)).forEach(message -> p.sendMessage(colorize(message)));
                    }
                }
            } else {
                player.sendMessage(RED + "Вы не можете использовать эту команду. Игрок не находится на проверке или вы не вызывали его.");
            }
        } else {
            player.sendMessage(RED + "Использование: /check <ник> [allow|dis]");
        }
        return false;
    }

    public void stopCheck(Player targetPlayer) {
        if (timers.containsKey(targetPlayer)) {
            timers.get(targetPlayer).cancel();
            timers.remove(targetPlayer);
        }
        if (bossBars.containsKey(targetPlayer)) {
            bossBars.get(targetPlayer).removeAll();
            bossBars.remove(targetPlayer);
        }
    }
}