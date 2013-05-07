package com.github.omwah.giftevents;

import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventsListCommand extends PlayerSpecificCommand {
    private final List<GiftEvent> events;
    private final EventsInfo events_info;
    private final DateFormat display_format;

    public EventsListCommand(List<GiftEvent> events, EventsInfo events_info, DateFormat display_format) {
        super("list");

        this.events = events;
        this.events_info = events_info;
        this.display_format = display_format;

        setDescription("Get a list of events configured on this server");
        setUsage("/%s [player_name]");
        setArgumentRange(0, 1);
        setIdentifiers(this.getName());
        setPermission("giftevents.events");
    }

    @Override
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
                
        // Add 4 to width centered due to chat colors
        sender.sendMessage(ChatColor.RED + "---- [ " + ChatColor.WHITE + "Events" + ChatColor.RED + " ] ----");
        
        if(admin_output) {
            sender.sendMessage(ChatColor.GRAY + "Name : Date : Has Gift : Announcements");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Name : Date");
        }

        for(GiftEvent gift_event : events) {
                       
            Calendar cal = gift_event.getDate(player_name);
            if (cal != null) {
                String event_message = gift_event.getName() + " : " + display_format.format(cal.getTime());
                
                if (admin_output) {
                     event_message += " : " + events_info.hasGiftBeenGiven(gift_event, player_name) + " : " +
                             events_info.getNumAnnoucementsMade(gift_event, player_name);                
                }
                
                sender.sendMessage(event_message);                
            }
        }
        
        return true;
    }
}
