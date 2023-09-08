package net.fwupp.plugin.shopgui;

import lombok.SneakyThrows;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    private String headShopPermission;
    private String oreShopPermission;
    public ShopGuiPlusHook(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        this.timerIntervalTicks = (long) (config.getRefreshIntervalInHours() * 72_000);
        this.headShopPermission = "shopguiplus.shops." + config.getHeadShopName();
        this.oreShopPermission = "shopguiplus.shops." + config.getOreShopName();
    }

    @EventHandler // when ShopGuiPlus finishes loading
    public void onShopGUIPlusPostEnable(ShopGUIPlusPostEnableEvent event) {
        plugin.getLogger().info("Starting shop refresher timer..");
        startTimer();
    }
    @EventHandler // when HeadDatabase finishes loading
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        lock.countDown();
        HeadDatabaseAPI headDatabaseApi = new HeadDatabaseAPI();
        headShopVillagerHead = headDatabaseApi.getItemHead(config.getHeadShopVillagerHeadID());
        oreShopVillagerHead = headDatabaseApi.getItemHead(config.getOreShopVillagerHeadID());
    }
    @SneakyThrows
    private void startTimer() {
        lock.await(); // ShopGuiPlus and HeadDatabase are finished loading at this point
        shopRefresherRunnable = new ShopRefresherRunnable(config, plugin.getLogger());
        plugin.getLogger().info("Timer interval in ticks: " + timerIntervalTicks);
        shopRefresherRunnable.runTaskTimer(plugin, 0, timerIntervalTicks);
    }

    @EventHandler
    public void onVillagerClick(PlayerInteractEntityEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            // Your command execution here
            if (event.getRightClicked() instanceof Villager) {
                handleVillagerClick(event);
            }
        });
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

        // otherwise, maybe it was some sort of naming event; give the villager a head, if so.
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.NAME_TAG && itemInHand.hasItemMeta()) {
            giveVillagerHelmetIfApplicable((Villager) event.getRightClicked(), itemInHand);
        }
    }

    private boolean openShopIfApplicable(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName().equals(config.getHeadShopVillagerName())) {
            Player player = event.getPlayer();
            setPermissionIfNeeded(player, headShopPermission);
            return player.performCommand("shop " + config.getHeadShopName());
        }
        else if(event.getRightClicked().getName().equals(config.getOreShopVillagerName())) {
            Player player = event.getPlayer();
            setPermissionIfNeeded(player, oreShopPermission);
            return player.performCommand("shop " + config.getOreShopName());
        }
        return false;
    }

    private void setPermissionIfNeeded(Player player, String permissionNode) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        UserManager userManager = luckPerms.getUserManager();
        User playerAsLuckPermsUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        CachedPermissionData permissionData = playerAsLuckPermsUser.getCachedData().getPermissionData();
        boolean hasShopPermission = permissionData.checkPermission(permissionNode).asBoolean();
        if(!hasShopPermission) {
            plugin.getLogger().info(String.format("Giving %s permissions for shop. Permission: %s",
                    player.getDisplayName(), permissionNode));

            playerAsLuckPermsUser.data().add(PermissionNode.builder(permissionNode).value(true).build());
            userManager.saveUser(playerAsLuckPermsUser);
        }

//        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), String.format("lp user %s permission set %s true", player.getDisplayName(), permissionNode));
//        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp reload");

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
}