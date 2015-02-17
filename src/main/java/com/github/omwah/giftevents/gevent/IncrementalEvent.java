package com.github.omwah.giftevents.gevent;

import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;


public class IncrementalEvent extends ConfiguredEvent {
       
    public IncrementalEvent(Logger logger, ConfigurationSection eventSection) {
	super(logger, eventSection.getConfigurationSection(""));
	
    }

    public String getPermissionPath() {
	return "giftevents.incremental";
    }
    
    // Incremental events can not be belated
    @Override
    public boolean canGiveBelated() {
	return false;
    }

    public Calendar getDate(UUID playerUUID) {
	

	// TODO date parsing
	
	
	return null;
    }
    
}
