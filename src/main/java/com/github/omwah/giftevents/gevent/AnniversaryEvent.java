package com.github.omwah.giftevents.gevent;

import com.github.omwah.giftevents.EventsInfo;

import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

/**
 */
public class AnniversaryEvent extends ConfiguredEvent {
    private final EventsInfo events_info;

    public AnniversaryEvent(Logger logger, ConfigurationSection eventSection, EventsInfo events_info) {
        super(logger, eventSection);
        this.events_info = events_info;
    }
    
    @Override	
    public Calendar getDate(UUID playerUUID) {
        return this.events_info.getFirstPlayedDate(playerUUID);
    }

    @Override
    public String getPermissionPath() {
        return "giftevents.anniversary";
    }
    
}
