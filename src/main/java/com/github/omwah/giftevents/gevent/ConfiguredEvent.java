package com.github.omwah.giftevents.gevent;

import com.github.omwah.giftevents.GiftSet;

import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Base class for all event types where they have common behavior
 */
public abstract class ConfiguredEvent implements GiftEvent {
    private final boolean belated;
    private final String announcement;
    private final GiftSet gifts;
    private final String name;
            
    public ConfiguredEvent(Logger logger, ConfigurationSection eventSection) {
        this.name = eventSection.getName();               
        this.belated = eventSection.getBoolean("belated", false);
        this.announcement = eventSection.getString("announcement");
        
        ConfigurationSection gifts_sect = eventSection.getConfigurationSection("gift");
        if(gifts_sect != null) {
            this.gifts = new GiftSet(logger, gifts_sect);
        } else {
            this.gifts = null;
            logger.log(Level.INFO, "Could not find 'gift' section for event: {0}", this.getName());
        }
        
    }

    public abstract String getPermissionPath();
    
    public abstract Calendar getDate(UUID playerUUID);

    public String getName() {
        return this.name;
    }
    
    public boolean canGiveBelated() {
        return this.belated;
    }
   
    public String getAnnouncement(UUID playerUUID) {
        if (announcement != null) {
            return String.format(announcement, Bukkit.getPlayer(playerUUID).getName(), this.getDate(playerUUID));
        } else {
            return null;
        }
    }
    
    public boolean giveGifts(Player player) {
        // Send gifts to player if available
        if(gifts != null) {
            gifts.giveToPlayer(player);      
        
            // Only send a message to the player if they are online and we actually
            // have a message
            if(player.isOnline() && gifts.getMessageTemplate() != null) {
                String message = String.format(gifts.getMessageTemplate(), player.getName(), this.getDate(player.getUniqueId()));
                player.sendMessage(message);
            }
            
            return true;
        } else {
            return false;
        }

    }

    public GiftSet getGifts() {
        return gifts;
    }
}
