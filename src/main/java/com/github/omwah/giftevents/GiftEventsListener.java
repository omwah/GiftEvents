package com.github.omwah.giftevents;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/*
 */
public class GiftEventsListener implements Listener {
    private final GiftEvents plugin;

    /*
     * This listener needs to know about the plugin which it came from
     */
    public GiftEventsListener(GiftEvents plugin) {
        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        this.plugin = plugin;
    }

    /*
     * Check for events for player
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    }
    
}
