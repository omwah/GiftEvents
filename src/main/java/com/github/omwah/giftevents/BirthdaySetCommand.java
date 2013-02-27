package com.github.omwah.giftevents;

import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;
import org.bukkit.command.CommandSender;

public class BirthdaySetCommand extends PlayerSpecificCommand {
    private final EventsInfo events_info;
    
    public BirthdaySetCommand(EventsInfo events_info) {
        super("set");

        this.events_info = events_info;
        
        setDescription("Set a player's birthday");
        setUsage("/%s §8<date_string> [player_name]");
        setArgumentRange(1, 2);
        setIdentifiers(this.getName());
        setPermission("giftevents.birthday");
    }

    @Override
    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args)
    {
        String player_name = getDestPlayer(handler, sender, args, 1);
        if (player_name == null) {
            // Problem getting player name, reported to user
            return false;
        }

        boolean success = events_info.setBirthday(args[0], player_name);
        if (success) {
            sender.sendMessage("Succesfully changed birthday for " + player_name);
        } else {
            sender.sendMessage("Failed to change birthday for " + player_name);
        }
        return true;
    }
}
