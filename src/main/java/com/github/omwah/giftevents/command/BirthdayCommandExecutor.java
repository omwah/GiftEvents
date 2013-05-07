package com.github.omwah.giftevents.command;

import com.github.omwah.giftevents.GiftEventsPlugin;
import com.github.omwah.omcommands.NestedCommandExecutor;
import com.github.omwah.omcommands.PluginCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Handles dispatching of commands
 */
public class BirthdayCommandExecutor extends NestedCommandExecutor {

    /**
     *  Initialize class through super class constructor
     */
    public BirthdayCommandExecutor(GiftEventsPlugin plugin, Command cmd) {
        super(plugin, cmd, "giftevents.admin");
    }
    
    /*
     * Declares commands to be used by the plugin
     */
    @Override
    protected List<PluginCommand> getSubCommands(JavaPlugin plugin) {
        GiftEventsPlugin gift_events = (GiftEventsPlugin) plugin;

        // Set up which subcommands of the main command are available
        ArrayList<PluginCommand> sub_cmd_list = new ArrayList<PluginCommand>();
        
        sub_cmd_list.add(new BirthdayGetCommand(gift_events.getEventsInfo(), gift_events.getOutputDateFormat()));
        sub_cmd_list.add(new BirthdaySetCommand(gift_events.getEventsInfo(), gift_events.getInputDateFormat()));

        return sub_cmd_list;
    }
  

}
