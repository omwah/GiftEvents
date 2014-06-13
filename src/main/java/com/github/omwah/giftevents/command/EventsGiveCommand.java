package com.github.omwah.giftevents.command;

import java.util.UUID;

import com.github.omwah.giftevents.GiftEventsPlugin;
import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class EventsGiveCommand extends PlayerSpecificCommand {
    private final GiftEventsPlugin plugin;

    public EventsGiveCommand(GiftEventsPlugin plugin) {
        super("give");

        this.plugin = plugin;

        setDescription("Gives a player the items that would be awarded for an event");
        setUsage("/%s <player_name> <event_name>");
        setArgumentRange(2, 2);
        setIdentifiers(this.getName());
        setPermission("giftevents.admin");
    }


    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
    	UUID player_uuid = getDestPlayer(handler, sender, args, 0);
        if (player_uuid == null) {
            // Problem getting player name, reported to user
            return false;
        }
        
        String event_name = args[1];
        
        // Look for matching event and award it
        for(GiftEvent gift_event : plugin.getEvents()) {
            if (gift_event.getName().matches(event_name)) {
                OfflinePlayer player_obj = plugin.getServer().getOfflinePlayer(player_uuid);
                if(player_obj == null) {
                    sender.sendMessage("Could not find offline player: " + Bukkit.getPlayer(player_uuid).getName());
                    return false;
                } else if(player_obj.getPlayer() == null) {
                    sender.sendMessage("Can not give items to offline player: " + Bukkit.getPlayer(player_uuid).getName());
                    return false;                    
                }
                gift_event.giveGifts(player_obj.getPlayer());
                sender.sendMessage("Gave gifts of event: " + gift_event.getName() + " to: " + Bukkit.getPlayer(player_uuid).getName());
            }
        }
        
        return true;
    }
}
