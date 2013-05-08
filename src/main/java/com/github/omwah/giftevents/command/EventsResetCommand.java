package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.GiftEventsPlugin;
import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class EventsResetCommand extends PlayerSpecificCommand {
    private final GiftEventsPlugin plugin;

    public EventsResetCommand(GiftEventsPlugin plugin) {
        super("reset");

        this.plugin = plugin;

        setDescription("Resets a player's gift and annoucement status");
        setUsage("/%s <player_name> <event_name>");
        setArgumentRange(2, 2);
        setIdentifiers(this.getName());
        setPermission("giftevents.admin");
    }

    @Override
    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        String player_name = getDestPlayer(handler, sender, args, 0);
        if (player_name == null) {
            // Problem getting player name, reported to user
            return false;
        }
        
        String event_name = args[1];
        
        // Look for matching event and award it
        for(GiftEvent gift_event : plugin.getEvents()) {
            if (gift_event.getName().matches(event_name)) {
                plugin.getEventsInfo().setGiftGiven(gift_event, player_name, false);
                plugin.getEventsInfo().setNumAnnoucementsMade(gift_event, player_name, 0);
                sender.sendMessage("Reset gift status of event: " + gift_event.getName() + " for: " + player_name);
            }
        }
        
        return true;
    }
}
