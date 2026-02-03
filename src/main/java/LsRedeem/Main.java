package me.leuz.lsredeem;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Main extends JavaPlugin {

    private static Main instance;
    private Database database;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Inicializa o Banco de Dados
        String dbName = getConfig().getString("Settings.DatabaseFile", "database.db");
        this.database = new Database(new File(getDataFolder(), dbName).getAbsolutePath());

        // Registro de Comandos e TabCompleter
        Commands commandsHandler = new Commands();
        getCommand("redeem").setExecutor(commandsHandler);
        getCommand("lsredeem").setExecutor(commandsHandler);
        getCommand("lsredeem").setTabCompleter(commandsHandler);

        getServer().getConsoleSender().sendMessage("§b[LsRedeem] §fVersao: §a" + getDescription().getVersion());
        getServer().getConsoleSender().sendMessage("§b[LsRedeem] §aPlugin iniciado com sucesso! Criado por leu14z");
    }

    public static Main getInstance() { return instance; }
    public Database getDB() { return database; }
}