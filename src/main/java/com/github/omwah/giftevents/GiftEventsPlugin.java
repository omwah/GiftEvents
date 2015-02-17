package com.github.omwah.giftevents;

import com.github.omwah.giftevents.gevent.BirthdayEvent;
import com.github.omwah.giftevents.gevent.AnniversaryEvent;
import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.giftevents.gevent.GlobalEvent;
import com.github.omwah.giftevents.gevent.IncrementalEvent;
import com.github.omwah.giftevents.command.BirthdayCommandExecutor;
import com.github.omwah.giftevents.command.EventsCommandExecutor;
import com.github.omwah.giftevents.command.AnniversaryCommandExecutor;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.mcstats.Metrics;

/*
 * This is the main class of GiftEventsPlugin
 */
public class GiftEventsPlugin extends JavaPlugin {
    private EventsInfo events_info;
    private List<GiftEvent> events;
    private File events_config_file;
    private boolean enableIncrementalEvents;
    
    /*
     * Enable plugin, set up commands and configuration
     */
    @Override
    public void onEnable() {
        // Save the default config file from and events file from the resource
        // directory inside the plugin
        saveDefaultConfig();
        
        this.events_config_file = new File(this.getDataFolder(), this.getConfig().getString("events_file", "events.yml"));     
        saveDefaultEventsFile();
               
        // Load event information database for keeping track of 
        // birthdays and whether gifts have been handed out
        File db_file = new File(this.getDataFolder(), "events_info.db");
        boolean first_join_gift = this.getConfig().getBoolean("first_join_gift", false);
        this.enableIncrementalEvents = this.getConfig().getBoolean("incrementalEvents");
        
        try {
            events_info = new EventsInfo(this, db_file, first_join_gift);
        } catch (SQLException ex) {
            this.getLogger().log(Level.SEVERE, "Could not create EventsInfo database file: {0}. Plugin can not be intialized!", db_file);
            return;
        } catch (ClassNotFoundException ex) {
            this.getLogger().log(Level.SEVERE, "Could find JDBC class for EventsInfo database. Plugin can not be intialized!");
            return;            
        }           
        
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

        // Is Metrics enabled?
        if(this.getConfig().getBoolean("useMetrics")==true) {
	        // Try and send metrics to MCStats
	        try {
	            Metrics metrics = new Metrics(this);
	            metrics.start();
	        } catch (IOException e) {
	            getLogger().log(Level.SEVERE, "Could not send data to MCStats!");
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
     * Loads the default events file from the resource directory
     * Also makes sure to migrate any events from the config file intp
     * a seperate config file if migrating from an earlier version
     * where the events were kept inside the config.yml file.
     */
    void saveDefaultEventsFile() {
        ConfigurationSection existing_events_section = this.getConfig().getConfigurationSection("events");
        if(existing_events_section != null && !this.events_config_file.exists()) {
            // Migrate events from old config file, only if the events
            // file does not yet exist
            getLogger().log(Level.INFO, "Migrating events section from config.yml to: {0}", this.events_config_file);
            
            YamlConfiguration events_config = new YamlConfiguration();
            events_config.set("events", existing_events_section);
            
            try {
                events_config.save(this.events_config_file);
                
                // Remove events section from config file
                this.getConfig().set("events", null);
                this.saveConfig();
            } catch (IOException ex) {
                getLogger().info("Error saving new events file, falling back to using config.yml");
                
                // Fall back to config file
                this.events_config_file = new File(this.getDataFolder(), "config.yml");
            }

        } else if (!this.events_config_file.exists()) {
            File resource_file = new File(this.getDataFolder(), "events.yml");
            
            getLogger().log(Level.INFO, "Creating events file from default: {0}", resource_file.getName());
            saveResource(resource_file.getName(), false);
            
            // Rename just in case configured file name is different
            resource_file.renameTo(this.events_config_file);
        } else if (existing_events_section != null) {
            getLogger().log(Level.SEVERE, "An events section exists in config.yml but an events file already exists in the plugin direction and will not be overwritten.");
        }
    }

    /*
     * Loads the events list from the config file
     */
    public boolean loadEvents() {
        // Reload the configuration in case this is being called to reload the events
        YamlConfiguration events_config;
        events_config = YamlConfiguration.loadConfiguration(this.events_config_file); 
        
        if(events_config == null) {
            getLogger().log(Level.INFO, "Error parsing config file: {0}", this.events_config_file);
            return false;
        }
        
        // Create events objects from configuration file
        ConfigurationSection events_section = events_config.getConfigurationSection("events");
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
        
        if(this.enableIncrementalEvents) {
            // Create incremental events from advanced-events config section
            ConfigurationSection advanced_section = events_config.getConfigurationSection("advanced-events");
            if(advanced_section == null) {
                return false;
            }
            
            for(String event_name : advanced_section.getKeys(false)) {
                ConfigurationSection event_config = advanced_section.getConfigurationSection(event_name);
                events.add(new IncrementalEvent(this.getLogger(), event_config, events_info));
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
    
    public boolean getIncrementalEnabled() {
	return enableIncrementalEvents;
    }
}
