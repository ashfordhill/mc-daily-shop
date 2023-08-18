package net.fwupp.plugin.shopgui;

import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@Data
public class Config {
    private List<String> shopIDsToRefresh;
    private List<String> headIDsToForceInclude;
    private int refreshIntervalInHours;
    private int numItemsPerShop;
    private boolean reloadConfigOnRefresh;
    private boolean refreshOnStartup;


    public Config(FileConfiguration config) {
        shopIDsToRefresh = config.getStringList("shop-ids-to-refresh");
        headIDsToForceInclude = config.getStringList("head-ids-to-force-include");
        refreshIntervalInHours = config.getInt("refresh-interval-in-hours");
        numItemsPerShop = config.getInt("num-items-per-shop");
        reloadConfigOnRefresh = config.getBoolean("reload-config-on-refresh");
        refreshOnStartup = config.getBoolean("refresh-on-startup");
    }
}
