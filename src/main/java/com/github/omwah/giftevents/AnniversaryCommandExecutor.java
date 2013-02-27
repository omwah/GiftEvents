package com.github.omwah.giftevents;

import com.github.omwah.omcommands.NestedCommandExecutor;
import com.github.omwah.omcommands.PluginCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Handles dispatching of commands
 */
public class AnniversaryCommandExecutor extends NestedCommandExecutor {

    /**
     *  Initialize class through super class constructor
     */
    public AnniversaryCommandExecutor(GiftEvents plugin, Command cmd) {
        super(plugin, cmd, "giftevents.admin");
    }
    
    /*
     * Declares commands to be used by the plugin
     */
    @Override
    protected List<PluginCommand> getSubCommands(JavaPlugin plugin) {
        GiftEvents gift_events = (GiftEvents) plugin;

        // Set up which subcommands of the main command are available
        ArrayList<PluginCommand> sub_cmd_list = new ArrayList<PluginCommand>();
        
        sub_cmd_list.add(new AnniversaryCommand(gift_events.getEventsInfo(), gift_events.getDateFormat()));

        return sub_cmd_list;
    }

}
