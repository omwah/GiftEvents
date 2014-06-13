package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.EventsInfo;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class AnniversaryCommand extends PlayerSpecificCommand {
    private final EventsInfo events_info;
    private final DateFormat display_format;

    public AnniversaryCommand(EventsInfo events_info, DateFormat display_format) {
        super("anniversary");

        this.events_info = events_info;
        this.display_format = display_format;

        setDescription("Get a player's first play date");
        setUsage("/%s §8[player_name]");
        setArgumentRange(0, 1);
        setIdentifiers(this.getName(), "anniv", "first_play");
        setPermission("giftevents.anniversary");
    }


    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        UUID player_uuid = getDestPlayer(handler, sender, args, 0);
        if (player_uuid == null) {
            // Problem getting player name, reported to user
            return false;
        }

        Calendar player_anniversary = events_info.getFirstPlayedDate(player_uuid);
        if (player_anniversary != null) {
            sender.sendMessage(Bukkit.getPlayer(player_uuid).getName() + "'s first play anniversary is: " + display_format.format(player_anniversary.getTime()));
        } else {
            sender.sendMessage("Weird, no first play anniversary found for " + Bukkit.getPlayer(player_uuid).getName()); 
        }
        return true;
    }
}
