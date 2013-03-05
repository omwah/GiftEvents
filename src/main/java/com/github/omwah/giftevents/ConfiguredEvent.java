package com.github.omwah.giftevents;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        ConfigurationSection gifts_sect = eventSection.getConfigurationSection("gifts");
        if(gifts_sect != null) {
            this.gifts = new GiftSet(logger, gifts_sect);
        } else {
            this.gifts = null;
            logger.log(Level.INFO, "Could not find gifts section for event: {0}", this.getName());
        }
        
    }

    public abstract Calendar getDate(Player player);

    public String getName() {
        return this.name;
    }
    
    public boolean canGiveBelated() {
        return this.belated;
    }
   
    public String announcement(Player player) {
        if (announcement != null) {
            return String.format(announcement, player.getName(), this.getDate(player));
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
                String message = String.format(gifts.getMessageTemplate(), player.getName(), this.getDate(player));
                player.sendMessage(message);
            }
            
            return true;
        } else {
            return false;
        }

    }
    
}
