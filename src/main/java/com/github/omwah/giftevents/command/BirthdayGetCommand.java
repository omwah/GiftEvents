package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.EventsInfo;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;
import java.text.DateFormat;
import java.util.Calendar;
import org.bukkit.command.CommandSender;

public class BirthdayGetCommand extends PlayerSpecificCommand {
    private final EventsInfo events_info;
    private final DateFormat display_format;

    public BirthdayGetCommand(EventsInfo events_info, DateFormat display_format) {
        super("get");

        this.events_info = events_info;
        this.display_format = display_format;

        setDescription("Get a player's birthday");
        setUsage("/%s §8[player_name]");
        setArgumentRange(0, 1);
        setIdentifiers(this.getName());
        setPermission("giftevents.birthday");
    }


    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        String player_name = getDestPlayer(handler, sender, args, 0);
        if (player_name == null) {
            // Problem getting player name, reported to user
            return false;
        }

        Calendar player_birthday = events_info.getBirthday(player_name);
        if (player_birthday != null) {
            sender.sendMessage(player_name + "'s birthday is: " + display_format.format(player_birthday.getTime()));
        } else {
            sender.sendMessage("No birthday found for " + player_name); 
        }
        return true;
    }
}
