package net.fwupp.plugin.shopgui;

import lombok.SneakyThrows;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CountDownLatch;

public class ShopGuiPlusHook implements Listener {
    private final JavaPlugin plugin;
    private final Config config;
    private final long timerIntervalTicks;
    private ItemStack headShopVillagerHead;
    private ItemStack oreShopVillagerHead;
    private CountDownLatch lock = new CountDownLatch(1);

    private ShopRefresherRunnable shopRefresherRunnable;

    public ShopGuiPlusHook(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        this.timerIntervalTicks = (long) (config.getRefreshIntervalInHours() * 72_000);
    }

    @EventHandler
    public void onShopGUIPlusPostEnable(ShopGUIPlusPostEnableEvent event) {
        plugin.getLogger().info("Starting shop refresher timer..");
        startTimer();
    }
    @SneakyThrows
    private void startTimer() {
        lock.await();
        shopRefresherRunnable = new ShopRefresherRunnable(config, plugin.getLogger());
        plugin.getLogger().info("Timer interval in ticks: " + timerIntervalTicks);
        shopRefresherRunnable.runTaskTimer(plugin, 0, timerIntervalTicks);
    }

    @EventHandler
    public void onVillagerClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            handleVillagerClick(event);
        }
    }

    private void handleVillagerClick(PlayerInteractEntityEvent event) {
        boolean shopOpened = false;
        if(!event.getPlayer().isSneaking()) {
            shopOpened = openShopIfApplicable(event);
        }

        // early terminate, shop was opened so no need to check the rest
        if(shopOpened) {
            return;
        }

        // otherwise, maybe it was some sort of naming event, so give the villager a head, if so.
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.NAME_TAG && itemInHand.hasItemMeta()) {
            giveVillagerHelmetIfApplicable((Villager) event.getRightClicked(), itemInHand);
        }
    }

    private boolean openShopIfApplicable(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName().equals(config.getHeadShopVillagerName())) {
            event.getPlayer().performCommand("shop " + config.getShopName());
            return true;
        }
        else if(event.getRightClicked().getName().equals(config.getOreShopVillagerName())) {
            event.getPlayer().performCommand("shop " + config.getOreShopName());
            return true;
        }
        return false;
    }
    private void giveVillagerHelmetIfApplicable(Villager event, ItemStack nameTagItem) {
        String headShopVillagerName = config.getHeadShopVillagerName();
        String oreShopVillagerName = config.getOreShopVillagerName();
        if(nameTagItem.getItemMeta().getDisplayName().equals(headShopVillagerName)) {
            if(!config.getHeadShopVillagerHeadID().isEmpty() && headShopVillagerHead != null) {
                event.getEquipment().setHelmet(headShopVillagerHead);
            }
        }
        else if(nameTagItem.getItemMeta().getDisplayName().equals(oreShopVillagerName)) {
            if(!config.getOreShopVillagerHeadID().isEmpty() && oreShopVillagerHead != null) {
                event.getEquipment().setHelmet(oreShopVillagerHead);
            }
        }
    }


    // we also want to wait until the HeadDatabase finishes loading
    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        lock.countDown();
        HeadDatabaseAPI headDatabaseApi = new HeadDatabaseAPI();
        headShopVillagerHead = headDatabaseApi.getItemHead(config.getHeadShopVillagerHeadID());
        oreShopVillagerHead = headDatabaseApi.getItemHead(config.getOreShopVillagerHeadID());
    }

}