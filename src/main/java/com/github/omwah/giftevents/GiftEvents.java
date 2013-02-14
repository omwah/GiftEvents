package com.github.omwah.giftevents;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * This is the main class of GiftEvents
 */
public class GiftEvents extends JavaPlugin {
    /*
     * Enable plugin, set up commands and configuration
     */
    @Override
    public void onEnable() {
        // save the configuration file
        saveDefaultConfig();
        
        // Create the PluginListener
        new GiftEventsListener(this);
        
        // Load up the list of commands in the plugin.yml and register each of these
        // This makes is simpler to update the command names that this Plugin responds
        // to just by editing plugin.yml
        for(String command_name : this.getDescription().getCommands().keySet()) {
            // set the command executor for the Command
            PluginCommand curr_cmd = this.getCommand(command_name);
            curr_cmd.setExecutor(new GiftEventsCommandExecutor(this, curr_cmd));
        }
    }
    
    /*
     * Called when the plug-in shuts down
     */
    @Override
    public void onDisable() {
        
    }

}
