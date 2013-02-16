package com.github.omwah.giftevents;

import com.github.omwah.omcommands.NestedCommandExecutor;
import com.github.omwah.omcommands.PluginCommand;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Handles dispatching of commands
 */
public class GiftEventsCommandExecutor extends NestedCommandExecutor {

    /**
     *  Initialize class through super class constructor
     * @param plugin
     * @param cmd
     */
    public GiftEventsCommandExecutor(GiftEvents plugin, Command cmd) {
        super(plugin, cmd, "giftevents.admin");
    }
    
    /*
     * Declares commands to be used by the plugin
     */
    @Override
    protected Map<String, PluginCommand> getSubCommands(JavaPlugin plugin) {
        GiftEvents gift_events = (GiftEvents) plugin;

        // Set up which subcommands of the main command are available
        ArrayList<PluginCommand> sub_cmd_list = new ArrayList<PluginCommand>();
        
        sub_cmd_list.add(new BirthdayGetCommand(gift_events.getEventsInfo(), gift_events.getDateFormat()));
        sub_cmd_list.add(new BirthdaySetCommand(gift_events.getEventsInfo()));

        // Use LinkedHashMap so values are in the order they were inserted
        Map<String, PluginCommand> sub_cmd_map = new LinkedHashMap<String, PluginCommand>();
        for (PluginCommand sub_cmd : sub_cmd_list) {
            sub_cmd_map.put(sub_cmd.getName(), sub_cmd);
        }

        return sub_cmd_map;
    }
  

}
