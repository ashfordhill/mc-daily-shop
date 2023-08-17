package net.fwupp.plugin.shopgui;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import net.brcdev.shopgui.exception.shop.ShopsNotLoadedException;
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
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ShopRefresherPlugin extends JavaPlugin implements Listener {
    private long HOURS_INTERVAL = 12; // Adjust this to the desired interval in hours
    private long intervalInMilliseconds = HOURS_INTERVAL * 60 * 60 * 1000; // Convert hours to milliseconds

    private CountDownLatch shopGuiLatch = new CountDownLatch(1);
    private CountDownLatch headDatabaseLatch = new CountDownLatch(1);

    private HeadDatabaseAPI api;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        startTimer();


    }

    private void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {

                try {
                    // Wait for both APIs to be ready
                    shopGuiLatch.await();
                    headDatabaseLatch.await();

                    // this would eff over any player shops.
                    Set<Shop> shops = ShopGuiPlusApi.getPlugin().getShopManager().getShops();
                    shops.forEach(shop -> refreshShop(shop));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ShopsNotLoadedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskTimer(this, 0, intervalInMilliseconds);
    }

        int numItems = 10;
    public void refreshShop(Shop shop) {
        shop.setSize(numItems); // maybe this 'clears' the shop?
        shop.setShopItems(getRandomHeadsForShop(numItems, shop));
    }

    public List<ShopItem>  getRandomHeadsForShop(int numRandomHeads, Shop shop) {
        List<ShopItem> shopHeads = new ArrayList<>();

        for (int i = 0; i < numRandomHeads; i++) {
            ItemStack randomHead = api.getRandomHead();
            ShopItem shopItem = new ShopItem(shop, null, ShopItemType.ITEM, randomHead);
            shopHeads.add(shopItem);
        }

        return shopHeads;
    }

    // Need to wait for both of these events to happen first, to avoid NPE/issues
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
