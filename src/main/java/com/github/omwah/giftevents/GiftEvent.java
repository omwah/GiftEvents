package com.github.omwah.giftevents;

import java.util.Calendar;
import org.bukkit.entity.Player;

/**
 */
public interface Event {
 
    public String getName();
    
    public Calendar getDate();
    
    public boolean canGiftBelated();
    
    public String announcement();
    
    public GiftSet getGifts();
    
}
