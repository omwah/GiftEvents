package com.github.omwah.giftevents;

import java.util.Calendar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/*
 */
public class Event {
    private String name;
    private Calendar date;
    private boolean belated;
    private String annoucement;
    private GiftSet gifts;
    
    public Event(EventsInfo eventsInfo, ConfigurationSection eventSection) {
        
    }
    
    public String getName() {
        return this.name;
    }
    
    public Calendar getDate(Player playerObj) {
        return null;   
    }
}
