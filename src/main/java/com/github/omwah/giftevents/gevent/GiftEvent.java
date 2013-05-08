package com.github.omwah.giftevents.gevent;

import com.github.omwah.giftevents.GiftSet;
import java.util.Calendar;
import org.bukkit.entity.Player;

/**
 * An GiftEvent represents an event that might result in an getAnnouncement made
 * on behalf of a player. Additionally players can receive gifts for an event.
 */
public interface GiftEvent {
 
    /*
     * Name of the event
     */
    public String getName();
    
    /*
     * Permission string to check when processing event
     */
    public String getPermissionPath();
    
    /*
     * The date of the event
     */
    public Calendar getDate(String playerName);

    /*
     * Whether or not the gift can be given after the actual date has passed
     */
    public boolean canGiveBelated();
    
    /*
     * Returns an annoucement to be made about the event
     */
    public String getAnnouncement(String playerName);
    
    /*
     * Sends gifts to the player and returns a message for the player
     * 
     * Returns true if any gifts were given
     */
    public boolean giveGifts(Player player);
    
    /*
     * Get the GiftSet configured for this Event
     */
    public GiftSet getGifts();
    
}
