package com.github.omwah.giftevents;

import java.util.Calendar;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 */
public class AnniversaryEvent extends ConfiguredEvent {
    private final EventsInfo events_info;

    public AnniversaryEvent(Logger logger, ConfigurationSection eventSection, EventsInfo events_info) {
        super(logger, eventSection);
        this.events_info = events_info;
    }
    
    @Override
    public Calendar getDate(Player player) {
        return this.events_info.getFirstPlayedDate(player);
    }
}
