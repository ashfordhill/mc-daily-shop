package net.fwupp.plugin.shopgui;

import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Logger;

@Data
public class Config {
    private String headShopName;
    private List<String> headIDsToForceInclude;
    private double refreshIntervalInHours;
    private int numItemsInShop;
    private double pricePerHead;
    private boolean reloadConfigOnRefresh;
    private boolean refreshOnStartup;
    private String headShopVillagerName;
    private String headShopVillagerHeadID;
    private String oreShopVillagerName;
    private String oreShopVillagerHeadID;
    private String oreShopName;


    public Config(FileConfiguration config, Logger logger) {
        headShopName = config.getString("head-shop-name");
        headIDsToForceInclude = config.getStringList("head-ids-to-force-include");
        refreshIntervalInHours = config.getDouble("refresh-interval-in-hours");
        numItemsInShop = config.getInt("num-items-per-shop");
        reloadConfigOnRefresh = config.getBoolean("reload-config-on-refresh");
        refreshOnStartup = config.getBoolean("refresh-on-startup");
        pricePerHead = config.getDouble("price-per-head");
        headShopVillagerName = config.getString("head-shop-villager-name");
        headShopVillagerHeadID = config.getString("head-shop-villager-head-id");
        oreShopVillagerName = config.getString("ore-shop-villager-name");
        oreShopVillagerHeadID = config.getString("ore-shop-villager-head-id");
        oreShopName = config.getString("ore-shop-name");
        logger.info("Config loaded! Printing values..");
        logger.info(String.format(
                "\nhead-shop-name: %s\n" +
                "head-ids-to-force-include: %s\n" +
                "price-per-head: %s\n" +
                "refresh-interval-in-hours: %s\n" +
                "num-items-per-shop: %s\n" +
                "refresh-on-startup: %s\n" +
                "head-shop-villager-name: %s\n" +
                "head-shop-villager-head-id: %s\n" +
                "ore-shop-villager-name: %s\n" +
                "ore-shop-villager-head-id: %s\n" +
                "ore-shop-name: %s\n",
                headShopName.toString(),
                headIDsToForceInclude.toString(),
                pricePerHead,
                refreshIntervalInHours,
                numItemsInShop,
                refreshOnStartup,
                headShopVillagerName,
                headShopVillagerHeadID,
                oreShopVillagerName,
                oreShopVillagerHeadID,
                oreShopName
                ));
    }
}
