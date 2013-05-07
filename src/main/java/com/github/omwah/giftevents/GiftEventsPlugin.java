package com.github.omwah.giftevents;

import com.github.omwah.giftevents.gevent.BirthdayEvent;
import com.github.omwah.giftevents.gevent.AnniversaryEvent;
import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.giftevents.gevent.GlobalEvent;
import com.github.omwah.giftevents.command.BirthdayCommandExecutor;
import com.github.omwah.giftevents.command.EventsCommandExecutor;
import com.github.omwah.giftevents.command.AnniversaryCommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.mcstats.Metrics;

/*
 * This is the main class of GiftEventsPlugin
 */
public class GiftEventsPlugin extends JavaPlugin {
    private EventsInfo events_info;
    private List<GiftEvent> events;

    /*
     * Enable plugin, set up commands and configuration
     */
    @Override
    public void onEnable() {
        // save the configuration file
        saveDefaultConfig();
               
        // Load event information database for keeping track of 
        // birthdays and whether gifts have been handed out
        File db_file = new File(this.getDataFolder(), "events_info");
        boolean first_join_gift = this.getConfig().getBoolean("first_join_gift", false);
        events_info = new EventsInfo(this, this.getName(), db_file, first_join_gift);
        
        // Load list of events from the config file        
        if (!this.loadEvents()) {
            this.getLogger().log(Level.SEVERE, "events section not found in configuration, could not enable events");
            return;
        }
                
        // Create the Listener for giving gifts to logging in players
        int max_announcements = this.getConfig().getInt("maximum_announcements", 5);
        Listener login_listener = new GiftEventsListener(this, max_announcements);
        this.getServer().getPluginManager().registerEvents(login_listener, this);
        
        // Load commands for using the plugin
        loadCommands();

        // Try and send metrics to MCStats
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not send data to MCStats!");
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
     * Loads the events list from the config file
     */
    
    private boolean loadEvents() {
        // Create events objects from configuration file
        ConfigurationSection events_section = this.getConfig().getConfigurationSection("events");
        if (events_section == null) {
            return false;
        }
        
        events = new ArrayList<GiftEvent>();
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
        
        return true;
    }
    
    /*
     * Loads commands used by the plugin
     */
    private void loadCommands() {
        // Load up the list of commands based on what is actually in the plugin.yml
        for(String command_name : this.getDescription().getCommands().keySet()) {
            // set the command executor for the Command
            PluginCommand curr_cmd = this.getCommand(command_name);
            
            if (command_name.equalsIgnoreCase("birthday")) {
                curr_cmd.setExecutor(new BirthdayCommandExecutor(this, curr_cmd));
            } else if (command_name.equalsIgnoreCase("anniversary")) {
                curr_cmd.setExecutor(new AnniversaryCommandExecutor(this, curr_cmd));
            } else if (command_name.equalsIgnoreCase("events")) {
                curr_cmd.setExecutor(new EventsCommandExecutor(this, curr_cmd));
            } else {
                getLogger().log(Level.INFO, "Unknown command name in config.yml: {0}", command_name);
            }
           
        }        
    }
    
    /*
     * Returns the EventsInfo databvase  
     */

    public EventsInfo getEventsInfo() {
        return events_info;
    }
    
    /*
     * Get list of configured events
     */
    
    public List<GiftEvent> getEvents() {
        return events;
    }

    /*
     * Return the date format specified in the configuration file for inputting values
     */

    public SimpleDateFormat getInputDateFormat() {
        return new SimpleDateFormat(getConfig().getString("date_format.input", "MM-dd"));
    }
    
    /*
     * Return the date format specified in the configuration file for outputting dates
     */

    public SimpleDateFormat getOutputDateFormat() {
        return new SimpleDateFormat(getConfig().getString("date_format.output", "MM-dd"));
    }
}
