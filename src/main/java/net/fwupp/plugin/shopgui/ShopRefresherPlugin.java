package net.fwupp.plugin.shopgui;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopRefresherPlugin extends JavaPlugin implements Listener {
    private Config config;
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.config = new Config(getConfig(), getLogger());

        this.getServer().getPluginManager().registerEvents(new ShopGuiPlusHook(this, config), this);
    }

}
