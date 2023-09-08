package net.fwupp.plugin.shopgui.commands;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CommandGetHead implements CommandExecutor, Listener {
    static HeadDatabaseAPI headDatabaseApi;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (player.getGameMode() == GameMode.CREATIVE) {
            String headID  = args[0];
            ItemStack itemHead = headDatabaseApi.getItemHead(headID);
            player.getInventory().setItemInOffHand(itemHead);
        } else {
            player.sendMessage("This command can only be used in creative mode.");
            return false;
        }

        return true;
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        headDatabaseApi = new HeadDatabaseAPI();
    }
}
