package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.GiftEventsPlugin;
import com.github.omwah.giftevents.EventsInfo;
import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;

import java.text.DateFormat;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventsListCommand extends PlayerSpecificCommand {
    private final GiftEventsPlugin plugin;
    private final EventsInfo events_info;
    private final DateFormat display_format;

    public EventsListCommand(GiftEventsPlugin plugin, DateFormat display_format) {
        super("list");

        this.plugin = plugin;
        this.events_info = plugin.getEventsInfo();
        this.display_format = display_format;

        setDescription("Get a list of events configured on this server");
        setUsage("/%s [player_name]");
        setArgumentRange(0, 1);
        setIdentifiers(this.getName());
        setPermission("giftevents.events");
    }

    @SuppressWarnings("deprecation")
	public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        // Player name is optional and some events might not need
        // a player name to get their date, such as global events
        String player_name = null;
        if (args.length > 0 || sender instanceof Player) {
        	player_name = getDestPlayer(handler, sender, args, 0);
            if (player_name == null) {
                // Problem getting player name, reported to user
                return false;
            }
        }
        
        boolean admin_output = handler.hasAdminPermission(sender) && player_name != null;
                
        sender.sendMessage(ChatColor.RED + "---- [ " + ChatColor.WHITE + "Events" + ChatColor.RED + " ] ----");
        
        if(admin_output) {
            sender.sendMessage(ChatColor.GRAY + "Name : Date : Has Gift : Announcements");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Name : Date");
        }

        for(GiftEvent gift_event : plugin.getEvents()) {
                       
            Calendar cal = gift_event.getDate(Bukkit.getPlayer(player_name).getUniqueId());
            if (cal != null) {
                String event_message = gift_event.getName() + " : " + display_format.format(cal.getTime());
                
                if (admin_output) {
                     event_message += " : " + events_info.hasGiftBeenGiven(gift_event, Bukkit.getPlayer(player_name).getUniqueId()) + " : " +
                             events_info.getNumAnnoucementsMade(gift_event, Bukkit.getPlayer(player_name).getUniqueId());                
                }
                
                sender.sendMessage(event_message);                
            }
        }
        
        return true;
    }
}
