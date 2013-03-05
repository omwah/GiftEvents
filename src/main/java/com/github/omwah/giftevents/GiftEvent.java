package com.github.omwah.giftevents;

import java.util.Calendar;
import org.bukkit.entity.Player;

/**
 * An GiftEvent represents an event that might result in an announcement made
 * on behalf of a player. Additionally players can receive gifts for an event.
 */
public interface GiftEvent {
 
    /*
     * Name of the event
     */
    public String getName();
    
    /*
     * The date of the event
     */
    public Calendar getDate(Player player);

    /*
     * Whether or not the gift can be given after the actual date has passed
     */
    public boolean canGiveBelated();
    
    /*
     * Returns an annoucement to be made about the event
     */
    public String announcement(Player player);
    
    /*
     * Sends gifts to the player and returns a message for the player
     * 
     * Returns true if any gifts were given
     */
    public boolean giveGifts(Player player);
    
}
