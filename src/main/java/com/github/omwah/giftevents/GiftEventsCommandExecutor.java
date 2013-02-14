package com.github.omwah.giftevents;

import com.github.omwah.omcommands.NestedCommandExecutor;
import com.github.omwah.omcommands.PluginCommand;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

/*
 */
public class GiftEventsCommandExecutor extends NestedCommandExecutor {
    
    /**
     *  Initialize class through super class constructor
     * @param plugin
     * @param cmd
     */
    public GiftEventsCommandExecutor(JavaPlugin plugin, Command cmd) {
        super(plugin, cmd, "giftevents.admin");
    }
    
    /*
     * Declares commands to be used by the plugin
     * 
     * @param plugin
     * @return
     */
    @Override
    protected Map<String, PluginCommand> getSubCommands(JavaPlugin plugin) {
        return null;
    }
  

}
