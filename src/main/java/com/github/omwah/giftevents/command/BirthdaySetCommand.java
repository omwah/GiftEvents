package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.EventsInfo;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class BirthdaySetCommand extends PlayerSpecificCommand {
    private final EventsInfo events_info;
    private final SimpleDateFormat date_format;
    
    public BirthdaySetCommand(EventsInfo events_info, SimpleDateFormat date_format) {
        super("set");

        this.events_info = events_info;
        this.date_format = date_format;
        
        setDescription("Set a player's birthday");
        setUsage("/%s §8<date_string> [player_name]");
        setArgumentRange(1, 2);
        setIdentifiers(this.getName());
        setPermission("giftevents.birthday");
    }


    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        UUID player_uuid = getDestPlayer(handler, sender, args, 1);
        if (player_uuid == null) {
            // Problem getting player name, reported to user
            return false;
        }
        
        String date_str = args[0];
        Date date;
        try {
            date = date_format.parse(date_str);
        } catch (ParseException ex) {
            sender.sendMessage("Could not parse date supplied: " + date_str + " using date format: " + date_format.toPattern());            
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);        

        boolean success = events_info.setBirthday(calendar, player_uuid);
        if (success) {
            sender.sendMessage("Succesfully changed birthday for " + Bukkit.getPlayer(player_uuid).getName());
        } else {
            sender.sendMessage("Failed to change birthday for " + Bukkit.getPlayer(player_uuid).getName());
        }
        return true;
    }
}
