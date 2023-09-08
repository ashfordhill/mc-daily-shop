package net.fwupp.plugin.shopgui;

import net.fwupp.plugin.shopgui.commands.CommandGetHead;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopRefresherPlugin extends JavaPlugin implements Listener {
    private Config config;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.config = new Config(getConfig(), getLogger());


        CommandGetHead commandGetHead = new CommandGetHead();
        this.getServer().getPluginManager().registerEvents(commandGetHead, this);
        this.getCommand("gethead").setExecutor(commandGetHead);
        this.getServer().getPluginManager().registerEvents(new ShopGuiPlusHook(this, config), this);
    }

}
