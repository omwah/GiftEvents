package com.github.omwah.giftevents;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

/*
 * This is the main class of GiftEvents
 */
public class GiftEvents extends JavaPlugin {
    private EventsInfo events_info;

    /*
     * Enable plugin, set up commands and configuration
     */
    @Override
    public void onEnable() {
        // save the configuration file
        saveDefaultConfig();
        
        // Create the PluginListener
        new GiftEventsListener(this);

        // Load event information database for keeping track of 
        // birthdays and whether gifts have been handed out
        File db_file = new File(this.getDataFolder(), "events_info");
        events_info = new EventsInfo(this, this.getName(), db_file, getDateFormat()); 
        
        // Load up the list of commands in the plugin.yml and register each of these
        // This makes is simpler to update the command names that this Plugin responds
        // to just by editing plugin.yml
        for(String command_name : this.getDescription().getCommands().keySet()) {
            // set the command executor for the Command
            PluginCommand curr_cmd = this.getCommand(command_name);
            
            if (command_name.equalsIgnoreCase("birthday")) {
                curr_cmd.setExecutor(new BirthdayCommandExecutor(this, curr_cmd));
            } else if (command_name.equalsIgnoreCase("anniversary")) {
                curr_cmd.setExecutor(new AnniversaryCommandExecutor(this, curr_cmd));
            } else {
                getLogger().log(Level.INFO, "Uknown command name in config.yml: {0}", command_name);
            }
           
        }

    }
    
    /*
     * Called when the plug-in shuts down
     */
    @Override
    public void onDisable() {
        // Close database connection
        events_info.close();        
    }

    /*
     * Returns the EventsInfo databvase  
     */

    public EventsInfo getEventsInfo() {
        return events_info;
    }

    /*
     * Return the date format specified in the configuration file
     */

    public DateFormat getDateFormat() {
        return new SimpleDateFormat(getConfig().getString("date_format", "MM-dd"));
    }
}
