package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.EventsInfo;
import com.github.omwah.giftevents.GiftEventsPlugin;
import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EventsInfoCommand extends PlayerSpecificCommand {
    private final GiftEventsPlugin plugin;
    private final EventsInfo events_info;
    private final DateFormat display_format;
    
    public EventsInfoCommand(GiftEventsPlugin plugin, DateFormat display_format) {
        super("info");

        this.plugin = plugin;
        this.events_info = plugin.getEventsInfo();
        this.display_format = display_format;
        
        setDescription("Gives more detailed information on an avent");
        setUsage("/%s <event_name> [player_name]");
        setArgumentRange(1, 2);
        setIdentifiers(this.getName());
        setPermission("giftevents.events");
    }


    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        // Player name is optional and some events might not need
        // a player name to get their date, such as global events
        UUID player_uuid = null;
        if (args.length > 1 || sender instanceof Player) {
        	player_uuid = getDestPlayer(handler, sender, args, 1);
            if (player_uuid == null) {
                // Problem getting player name, reported to user
                return false;
            }
        }
        
        String event_name = args[0];
        
        boolean admin_output = handler.hasAdminPermission(sender) && player_uuid != null;

        // Look for matching event and award it
        for(GiftEvent gift_event : plugin.getEvents()) {
            if (gift_event.getName().matches(event_name)) {
                sender.sendMessage(ChatColor.RED + "---- [ " + ChatColor.WHITE + gift_event.getName() + " Event" + ChatColor.RED + " ] ----");
                sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + gift_event.getName());
                
                Calendar cal = gift_event.getDate(player_uuid);
                if (cal != null) {
                    sender.sendMessage(ChatColor.GRAY + "Date: " + ChatColor.WHITE + display_format.format(cal.getTime()));
                }
                
                sender.sendMessage(ChatColor.GRAY + "Gifts: ");
                for(ItemStack item : gift_event.getGifts().getItems()) {
                    // Use ItemStack toString but clean it up a bit
                    sender.sendMessage("- " + item.toString().replace("ItemStack{", "").replaceAll("}$", ""));
                }
                
                if (admin_output) {
                    sender.sendMessage(ChatColor.GRAY + "Has Gift Been Given: " + ChatColor.WHITE + events_info.hasGiftBeenGiven(gift_event, player_uuid));
                    sender.sendMessage(ChatColor.GRAY + "Num Announcements Made: " + ChatColor.WHITE + events_info.getNumAnnoucementsMade(gift_event, player_uuid));                
                }
            }
        }
        
        return true;
    }
}
