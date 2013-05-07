package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.GiftEventsPlugin;
import com.github.omwah.omcommands.CommandHandler;
import com.github.omwah.omcommands.PlayerSpecificCommand;
import org.bukkit.command.CommandSender;

public class EventsReloadCommand extends PlayerSpecificCommand {
    private final GiftEventsPlugin plugin;

    public EventsReloadCommand(GiftEventsPlugin plugin) {
        super("reload");

        this.plugin = plugin;

        setDescription("Reloads the list of events from the config file");
        setUsage("/%s");
        setArgumentRange(0, 1);
        setIdentifiers(this.getName());
        setPermission("giftevents.admin");
    }

    @Override
    public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args) {
        if(plugin.loadEvents()) {
            sender.sendMessage("Events successfully reloaded");
        } else {
            sender.sendMessage("Events failed to reload");            
        }
        return true;
    }
}
