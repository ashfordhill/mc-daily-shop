package net.fwupp.plugin.shopgui;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

public class ShopRefresherRunnable extends BukkitRunnable {
    private final Logger logger;
    private final Config config;
    private final HeadDatabaseAPI headDatabaseApi;
    private int numTimesTimedTaskEntered = 0;
    private int nextShopSlotNum = 0;

    public ShopRefresherRunnable(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
        headDatabaseApi = new HeadDatabaseAPI();
    }

    @Override
    public void run() {
        // only refresh upon startup if the config says to, otherwise next refresh will be at the next interval
        if (numTimesTimedTaskEntered > 0 || config.isRefreshOnStartup()) {
            Shop shop = ShopGuiPlusApi.getPlugin().getShopManager().getShopById(config.getHeadShopName());
            refreshShop(shop);
            logger.info(String.format("Refreshed %s shop inventory with %s heads!", config.getHeadShopName(), config.getNumItemsInShop()));
        }
        numTimesTimedTaskEntered++;
    }
    private void refreshShop(Shop shop) {
        shop.setSize(config.getNumItemsInShop()); // maybe this 'clears' the shop?
        List<ShopItem> shopItems = new ArrayList<>();
        int randomHeadsNeeded = config.getNumItemsInShop();
        // Attempt to load any specific heads before loading random heads
        if(!config.getHeadIDsToForceInclude().isEmpty()) {
            randomHeadsNeeded =  config.getNumItemsInShop() - config.getHeadIDsToForceInclude().size();
            if(randomHeadsNeeded < 0) {
                logger.warning(
                        String.format(
                        "Number of heads to force include (%s) is greater than the number of items in the shop (%s). Using all random heads instead.",
                            config.getHeadIDsToForceInclude().size(),
                            config.getNumItemsInShop()));
            }
            else {
                shopItems.addAll(getSpecificHeadsForShop(shop));
            }
        }

        // rest of shop's items will be random
        shopItems.addAll(getRandomHeadsForShop(randomHeadsNeeded, shop));
        shop.setShopItems(shopItems);
        nextShopSlotNum = 0;
    }

    @NotNull
    private List<ShopItem> getSpecificHeadsForShop(Shop shop) {

        List<ShopItem> heads = new ArrayList<>();
        for (String headID : config.getHeadIDsToForceInclude()) {
            ItemStack specificHead = headDatabaseApi.getItemHead(headID);
            if (specificHead == null) {
                logger.warning(String.format("Head ID %s was not found in the Head Database. Skipping this head.", headID));
                continue;
            }
            ShopItem shopItem = createShopItem(specificHead, shop);
            heads.add(shopItem);
        }
        return heads;
    }

    @NotNull
    private List<ShopItem> getRandomHeadsForShop(int numRandomHeads, Shop shop) {
        List<ShopItem> shopHeads = new ArrayList<>();

        for (int i = 0; i < numRandomHeads; i++)  {
            ItemStack randomHead = headDatabaseApi.getRandomHead();
            ShopItem shopItem = createShopItem(randomHead, shop);
            shopHeads.add(shopItem);
        }

        return shopHeads;
    }

    // some known issues; need to manually set the Placeholder and Page
    // otherwise they seem to default to null and 0.
    @NotNull
    private ShopItem createShopItem(ItemStack itemForShop, Shop shop) {
        itemForShop.setAmount(1);
        ShopItem shopItem = new ShopItem(
                shop,
                String.valueOf(Math.random()), // guessing this just needs to be unique
                ShopItemType.ITEM,
                itemForShop);
        shopItem.setPlaceholder(itemForShop);
        shopItem.setPage(1); // defaults to 0 if left unset, then the shop won't show it
        shopItem.setBuyPrice(config.getPricePerHead());
        shopItem.setSellPrice(-1); // unsellable
        shopItem.setSlot(nextShopSlotNum++);
        return shopItem;
    }

}
