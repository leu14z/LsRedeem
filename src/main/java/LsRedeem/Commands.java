package me.leuz.lsredeem;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

    private final Main plugin = Main.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // --- COMANDO /REDEEM (PLAYER) ---
        if (cmd.getName().equalsIgnoreCase("redeem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cEste comando só pode ser utilizado por jogadores.");
                return true;
            }

            Player p = (Player) sender;

            if (!p.hasPermission("LsRedeem.redeem")) {
                sendPrefixedMessage(p, plugin.getConfig().getString("Messages.NoPermission"));
                return true;
            }

            if (args.length == 0) {
                sendPrefixedMessage(p, "&cUtilize: /redeem <codigo>");
                return true;
            }

            String code = args[0];

            if (!plugin.getConfig().contains("codes." + code)) {
                sendPrefixedMessage(p, plugin.getConfig().getString("Messages.CodeNotFound").replace("%code%", code));
                return true;
            }

            int uses = plugin.getConfig().getInt("codes." + code + ".uses");
            int initial = plugin.getConfig().getInt("codes." + code + ".initial_uses");

            if (initial != 0 && uses <= 0) {
                sendPrefixedMessage(p, plugin.getConfig().getString("Messages.CodeNotFound").replace("%code%", code));
                return true;
            }

            String ip = p.getAddress().getAddress().getHostAddress();
            boolean antiAbuse = plugin.getConfig().getBoolean("Settings.AntiAbuse");

            if (plugin.getDB().hasRedeemed(p.getName(), code, ip, antiAbuse)) {
                sendPrefixedMessage(p, plugin.getConfig().getString("Messages.AlreadyRedeemed"));
                return true;
            }

            List<String> commandsToRun = plugin.getConfig().getStringList("codes." + code + ".commands");
            for (String rawCmd : commandsToRun) {
                String formattedCmd = rawCmd
                        .replace("%player%", p.getName())
                        .replace("%code%", code)
                        .replace("%initial_uses%", String.valueOf(initial));

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            }

            plugin.getDB().addLog(p.getName(), code, ip);

            if (initial != 0) {
                plugin.getConfig().set("codes." + code + ".uses", uses - 1);
                plugin.saveConfig();
            }

            sendPrefixedMessage(p, plugin.getConfig().getString("Messages.Success").replace("%code%", code));
            return true;
        }

        // --- COMANDO /LSREDEEM (ADMIN) ---
        if (cmd.getName().equalsIgnoreCase("lsredeem")) {
            if (!sender.hasPermission("LsRedeem.admin")) {
                sendPrefixedMessage(sender, plugin.getConfig().getString("Messages.NoPermission"));
                return true;
            }

            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }

            String sub = args[0].toLowerCase();

            switch (sub) {
                case "criar":
                    if (args.length < 3) {
                        sendPrefixedMessage(sender, "&cUse: /lsredeem criar <codigo> <usos>");
                        return true;
                    }
                    try {
                        String id = args[1];
                        int amount = Integer.parseInt(args[2]);

                        plugin.getConfig().set("codes." + id + ".uses", amount);
                        plugin.getConfig().set("codes." + id + ".initial_uses", amount);
                        plugin.getConfig().set("codes." + id + ".commands", Collections.singletonList("give %player% diamond 1"));
                        plugin.saveConfig();

                        sendPrefixedMessage(sender, "&aCódigo &f" + id + " &acriado com &f" + amount + " &ausos.");
                    } catch (NumberFormatException e) {
                        sendPrefixedMessage(sender, "&cO número de usos deve ser um numeral. Use 0 para ilimitado.");
                    }
                    break;

                case "remover":
                    if (args.length < 2) {
                        sendPrefixedMessage(sender, "&cUse: /lsredeem remover <codigo>");
                        return true;
                    }
                    String codeToRemove = args[1];
                    if (plugin.getConfig().contains("codes." + codeToRemove)) {
                        plugin.getConfig().set("codes." + codeToRemove, null);
                        plugin.saveConfig();
                        sendPrefixedMessage(sender, "&aCódigo &f" + codeToRemove + " &aremovido com sucesso!");
                    } else {
                        sendPrefixedMessage(sender, "&cEste código não existe na configuração.");
                    }
                    break;

                case "list":
                    sender.sendMessage(" ");
                    sender.sendMessage("§b§lCÓDIGOS ATIVOS §8(LsRedeem)");
                    if (plugin.getConfig().getConfigurationSection("codes") == null || plugin.getConfig().getConfigurationSection("codes").getKeys(false).isEmpty()) {
                        sender.sendMessage(" §7• §cNenhum código cadastrado.");
                    } else {
                        for (String key : plugin.getConfig().getConfigurationSection("codes").getKeys(false)) {
                            int u = plugin.getConfig().getInt("codes." + key + ".uses");
                            int i = plugin.getConfig().getInt("codes." + key + ".initial_uses");
                            String status = (i == 0) ? "§bIlimitado" : "§f" + u + "§7/§f" + i;
                            sender.sendMessage(" §8• §f" + key + " §7- Restantes: " + status);
                        }
                    }
                    sender.sendMessage(" ");
                    break;

                case "resetar":
                    if (args.length < 3) {
                        sendPrefixedMessage(sender, "&cUse: /lsredeem resetar <jogador> <codigo>");
                        return true;
                    }
                    plugin.getDB().resetSpecific(args[1], args[2]);
                    sendPrefixedMessage(sender, "&aHistórico de &f" + args[1] + " &ano código &f" + args[2] + " &alimpado.");
                    break;

                case "resetartudo":
                    if (args.length < 2) {
                        sendPrefixedMessage(sender, "&cUse: /lsredeem resetartudo <jogador>");
                        return true;
                    }
                    plugin.getDB().resetAll(args[1]);
                    sendPrefixedMessage(sender, "&aTodo o histórico de resgates de &f" + args[1] + " &afoi deletado.");
                    break;

                case "reload":
                    plugin.reloadConfig();
                    sendPrefixedMessage(sender, plugin.getConfig().getString("Messages.Reload"));
                    break;

                default:
                    sendHelp(sender);
                    break;
            }
            return true;
        }

        return false;
    }

    private void sendPrefixedMessage(CommandSender sender, String message) {
        String prefix = plugin.getConfig().getString("Settings.Prefix");
        sender.sendMessage((prefix + message).replace("&", "§"));
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(" ");
        s.sendMessage("  §b§lLSREDEEM §8- §fAdministração");
        s.sendMessage("  §8» §f/lsredeem criar <id> <usos>");
        s.sendMessage("  §8» §f/lsredeem remover <id>");
        s.sendMessage("  §8» §f/lsredeem resetar <player> <id>");
        s.sendMessage("  §8» §f/lsredeem resetartudo <player>");
        s.sendMessage("  §8» §f/lsredeem list");
        s.sendMessage("  §8» §f/lsredeem reload");
        s.sendMessage(" ");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("lsredeem")) {
            if (args.length == 1) {
                return Arrays.asList("criar", "remover", "list", "resetar", "resetartudo", "reload").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            // Auto-completar códigos existentes para o comando remover ou resetar
            if (args.length == 2 && args[0].equalsIgnoreCase("remover")) {
                if (plugin.getConfig().getConfigurationSection("codes") != null) {
                    return new ArrayList<>(plugin.getConfig().getConfigurationSection("codes").getKeys(false));
                }
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("resetar")) {
                if (plugin.getConfig().getConfigurationSection("codes") != null) {
                    return new ArrayList<>(plugin.getConfig().getConfigurationSection("codes").getKeys(false));
                }
            }
        }
        return new ArrayList<>();
    }
}