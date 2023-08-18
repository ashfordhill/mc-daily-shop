package net.fwupp.plugin.shopgui;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ShopRefresherPlugin extends JavaPlugin implements Listener {
    private long HOURS_INTERVAL = 12; // Adjust this to the desired interval in hours
    private long intervalInMilliseconds = HOURS_INTERVAL * 60 * 60 * 1000; // Convert hours to milliseconds

    private CountDownLatch shopGuiLatch = new CountDownLatch(1);
    private CountDownLatch headDatabaseLatch = new CountDownLatch(1);

    private HeadDatabaseAPI api;
    private Config config;
    private int numTimesTimedTaskEntered = 0;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Starting refresh shop timer!");
        config = new Config(getConfig());
        startTimer();
    }

    private void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {

                try {
                    getLogger().info("Awaiting ShopGui and HeadDatabase to load..");

                    // Wait for both APIs to be ready
                    shopGuiLatch.await();
                    headDatabaseLatch.await();

                    if(config.isReloadConfigOnRefresh()) {
                        config = new Config(getConfig());
                    }
                    if (numTimesTimedTaskEntered > 0 || config.isRefreshOnStartup()) {
                        for(int i = 0; i < config.getShopIDsToRefresh().size(); ++i) {
                            Shop shop = ShopGuiPlusApi.getPlugin().getShopManager().getShopById(
                                    config.getShopIDsToRefresh().get(i));
                            refreshShop(shop, i);
                        }
                    }
                    numTimesTimedTaskEntered++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.runTaskTimer(this, 0, intervalInMilliseconds);
    }

    public void refreshShop(Shop shop, int index) {
        shop.setSize(config.getNumItemsPerShop()); // maybe this 'clears' the shop?

        List<ShopItem> shopItems = new ArrayList<>();
        int randomHeadsNeeded = config.getNumItemsPerShop();

        // first shop will get the 'forced' items, if applicable
        if(index == 1 && !config.getHeadIDsToForceInclude().isEmpty()) {
            randomHeadsNeeded = config.getHeadIDsToForceInclude().size() - config.getNumItemsPerShop();
            config.getHeadIDsToForceInclude().forEach((headID) -> {
                ShopItem shopItem = new ShopItem(shop, null, ShopItemType.ITEM, api.getItemHead(headID));
                shopItems.add(shopItem);
            });
        }

        // rest of first shop's items and/or all of the other shops items will be random
        shopItems.addAll(getRandomHeadsForShop(randomHeadsNeeded, shop));
        shop.setShopItems(shopItems);
    }

    public List<ShopItem> getRandomHeadsForShop(int numRandomHeads, Shop shop) {
        List<ShopItem> shopHeads = new ArrayList<>();

        for (int i = 0; i < numRandomHeads; i++) {
            ItemStack randomHead = api.getRandomHead();
            ShopItem shopItem = new ShopItem(shop, null, ShopItemType.ITEM, randomHead);
            shopHeads.add(shopItem);
        }

        return shopHeads;
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        api = new HeadDatabaseAPI();
        headDatabaseLatch.countDown();
    }

    @EventHandler
    public void onShopGUIPlusPostEnable(ShopGUIPlusPostEnableEvent event){
        shopGuiLatch.countDown();
    }

}
