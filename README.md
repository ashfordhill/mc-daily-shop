# ShopGUIPlus + HeadDatabase Shop Refresher
Uses the [GuiShop API](https://github.com/brcdev-minecraft/shopgui-api]) and 
[HeadDatabase API](https://github.com/Arcaniax-Development/HeadDatabase-API) to randomize custom head stocking for a single shop on a configurable interval.

## Installation
1. Make sure ShopGUIPlus and HeadDatabase are installed in plugins.
2. Create a `[shop-name].yml` and put in `ShopGUIPlus\shop` directory
     - The initial items don't matter, they will be overwritten
     - `shop-name` in this plugin's config should == the name of the `.yml` file
3. Configure this plugin as necessary. The economy type is determined by ShopGUIPlus' configuration.

## Usage

1. Use a name tag to name the villager whatever you configured as the `head-shop-villager-name`.
   ![](/pics/villager-naming.png)
2. As long as that villager keeps that name, it'll become the 'head shop' villager. If you right-click, it should open the head shop.
   ![](/pics/head-shop.png)
3. The same applies to the ore shop, if you choose to use that feature. It just provides a way for players to earn money if using Vault.
