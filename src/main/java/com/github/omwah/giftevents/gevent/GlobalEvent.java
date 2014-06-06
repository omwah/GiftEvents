package com.github.omwah.giftevents.gevent;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 */
public class GlobalEvent extends ConfiguredEvent {
    private final Calendar event_calendar;
    
    public GlobalEvent(Logger logger, ConfigurationSection eventSection, SimpleDateFormat date_format) {
        super(logger, eventSection);
               
        String date_str = eventSection.getString("date");
        
        // Try and parse date from config file        
        if(date_str != null) {            
            Date date = null;
            try {
                date = date_format.parse(date_str);
            } catch (ParseException ex) {
                this.event_calendar = null;
                logger.log(Level.SEVERE, "Could not parse date supplied: {0} using date format: {1}", new Object[]{date_str, date_format.toPattern()});
                return;
            }
            
            this.event_calendar = Calendar.getInstance();
            this.event_calendar.setTime(date);  
            
        } else {
            this.event_calendar = null;
            logger.log(Level.SEVERE, "Could not find ''date'' section in event configuration for: {0}", this.getName());
        }
    }

    @Override
    public Calendar getDate(String playerName) {
        return this.event_calendar;
    }

    @Override
    public String getPermissionPath() {
        return "giftevents.events";
    }
    
}
