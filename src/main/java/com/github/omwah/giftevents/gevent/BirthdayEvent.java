package com.github.omwah.giftevents.gevent;

import com.github.omwah.giftevents.EventsInfo;
import java.util.Calendar;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * An event that is a birthday specific to a player
 */
public class BirthdayEvent extends ConfiguredEvent {
    private final EventsInfo events_info;

    public BirthdayEvent(Logger logger, ConfigurationSection eventSection, EventsInfo events_info) {
        super(logger, eventSection);
        this.events_info = events_info;
    }
     
    @Override
    public Calendar getDate(String playerName) {
        return this.events_info.getBirthday(playerName);
    }

    @Override
    public String getPermissionPath() {
        return "giftevents.birthday";
    }
    
}
