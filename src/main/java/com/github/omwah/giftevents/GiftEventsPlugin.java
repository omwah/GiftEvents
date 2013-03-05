package com.github.omwah.giftevents;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/*
 * This is the main class of GiftEventsPlugin
 */
public class GiftEventsPlugin extends JavaPlugin {
    private EventsInfo events_info;

    /*
     * Enable plugin, set up commands and configuration
     */
    @Override
    public void onEnable() {
        // save the configuration file
        saveDefaultConfig();
               
        // Create events objects from configuration file
        ConfigurationSection events_section = this.getConfig().getConfigurationSection("events");
        if (events_section == null) {
            this.getLogger().log(Level.SEVERE, "events section not found in configuration, could not enable events");
            return;
        }
        
        // Load event information database for keeping track of 
        // birthdays and whether gifts have been handed out
        File db_file = new File(this.getDataFolder(), "events_info");
        events_info = new EventsInfo(this, this.getName(), db_file);
        
        ArrayList<GiftEvent> events = new ArrayList<GiftEvent>();
        for(String event_name : events_section.getKeys(false)) {
            ConfigurationSection event_config = events_section.getConfigurationSection(event_name);
            if(event_name.equalsIgnoreCase("birthday")) {
                events.add(new BirthdayEvent(this.getLogger(), event_config, events_info));
            } else if(event_name.equalsIgnoreCase("anniversary")) {
                events.add(new AnniversaryEvent(this.getLogger(), event_config, events_info));
            } else {
                events.add(new GlobalEvent(this.getLogger(), event_config, this.getInputDateFormat()));
            }
        }

        // Create the Listener for giving gifts to logging in players
        int max_announcements = this.getConfig().getInt("maximum_announcements", 5);
        Listener login_listener = new GiftEventsListener(this.getLogger(), events, events_info, this.getServer(), max_announcements);
        this.getServer().getPluginManager().registerEvents(login_listener, this);
        
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
     * Return the date format specified in the configuration file for inputting values
     */

    public DateFormat getInputDateFormat() {
        return new SimpleDateFormat(getConfig().getString("date_format.input", "MM-dd"));
    }
    
    /*
     * Return the date format specified in the configuration file for outputting dates
     */

    public DateFormat getOutputDateFormat() {
        return new SimpleDateFormat(getConfig().getString("date_format.output", "MM-dd"));
    }
}