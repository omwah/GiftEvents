package com.github.omwah.giftevents;

import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventsCommand extends PlayerSpecificCommand {
    private final List<GiftEvent> events;
    private final EventsInfo events_info;
    private final DateFormat display_format;

    public EventsCommand(List<GiftEvent> events, EventsInfo events_info, DateFormat display_format) {
        super("events");

        this.events = events;
        this.events_info = events_info;
        this.display_format = display_format;

        setDescription("Get a list of events configured on this server");
        setUsage("/%s");
        setArgumentRange(0, 1);
        setIdentifiers(this.getName());
        setPermission("giftevents.events");
    }

    @Override
    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        String player_name = getDestPlayer(handler, sender, args, 0);
        if (player_name == null) {
            // Problem getting player name, reported to user
            return false;
        }
        
        sender.sendMessage("GiftEvents");
        sender.sendMessage("==========");

        for(GiftEvent gift_event : events) {
            String event_message = gift_event.getName();
                       
            Calendar cal = gift_event.getDate(player_name);
            event_message += " : " + display_format.format(cal.getTime());
            
            if (handler.hasAdminPermission(sender)) {
                event_message += String.format(" -- Gift Given? %b, Annoucements: %d",
                        events_info.hasGiftBeenGiven(gift_event, player_name), 
                        events_info.getNumAnnoucementsMade(gift_event, player_name));
            }
                        
            sender.sendMessage(event_message);
        }
        
        return true;
    }
}
