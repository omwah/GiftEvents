package com.github.omwah.giftevents.gevent;

import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.github.omwah.giftevents.EventsInfo;
import com.github.omwah.giftevents.GiftSet;


public class IncrementalEvent implements GiftEvent {
       
    private final String name;
    private final EventsInfo events_info;
 
    //Map of gifts for the day after the first join
    SortedMap<Integer, GiftSet> gifts = new TreeMap<Integer, GiftSet>();    
    
    public IncrementalEvent(Logger logger, ConfigurationSection eventSection, EventsInfo events_info) {
	this.name = eventSection.getName();
	this.events_info = events_info;	

	for(String sectionKeys : eventSection.getKeys(false)) {
	    try {
		int dayDistance = Integer.parseInt(sectionKeys);
		
		ConfigurationSection gift_section = eventSection.getConfigurationSection(sectionKeys).getConfigurationSection("gift");
		
		if(gift_section!=null) {
		    GiftSet gift = new GiftSet(logger, gift_section);
		    gifts.put(dayDistance, gift);
		}else {
		    gifts.put(dayDistance, null);
	            logger.log(Level.INFO, "Could not find 'gift' section for incremental event: {0}", this.getName());
		}
		
	    }catch(Exception e) {
		logger.log(Level.WARNING, "Error while parsing date for incremental event: {0}", this.getName());
		gifts = null;
	    }
	}
    }    
    
    public String getPermissionPath() {
	return "giftevents.incremental";
    }
    
    public String getName() {
   	return this.name;
    }

    // No announcement messages for now
    public String getAnnouncement(UUID playerUUID) {
	return null;
    }

    public Calendar getDate(UUID playerUUID) {
	Calendar date = Calendar.getInstance();

	List<Calendar> loginDates = events_info.getLoginDates(playerUUID);
	
	if(gifts.get(loginDates.size())!=null && !events_info.hasGiftBeenGiven(this, playerUUID)) {
	    return date;
	}else if(gifts.lastKey() < loginDates.size()) {	   
	    return null;
	}else if(gifts.lastKey() >= loginDates.size() && gifts.firstKey() <= loginDates.size()) {
	    
	    int temp = loginDates.size()+1;
	    
	    for(int key : gifts.keySet()) {
		if(key==temp) {
		    break;
		}else {
		    if(key<=temp) {
			continue;
		    }else if(key>temp) {
			temp = key;
			break;
		    }
		}
		
	    }
	    
	    temp -= loginDates.size();	
	    
//	    System.out.println(date.get(Calendar.DAY_OF_MONTH));
//	    System.out.println(temp);
	    date.add(Calendar.DAY_OF_MONTH, temp);	
//	    System.out.println(date.get(Calendar.DAY_OF_MONTH) + ":" + (date.get(Calendar.MONTH)+1) + ":" + date.get(Calendar.YEAR));
	    return date;	    
	}
	
	return null;
    }

    // Incremental events can not be belated 
    public boolean canGiveBelated() {
	return false;
    }
    
    public boolean giveGifts(Player player) {
	
	List<Calendar> loginDates = events_info.getLoginDates(player.getUniqueId());	
	
	// Send gifts to player if available
	if(gifts!=null) {
	    if(gifts.get(loginDates.size())!=null) {
		GiftSet gift = gifts.get(loginDates.size());
		
		gift.giveToPlayer(player);
		
		// Only send a message to the player if they are online and we actually
		// have a message
		if(player.isOnline() && gift.getMessageTemplate() != null) {
		    String message = String.format(gift.getMessageTemplate(), player.getName(), this.getDate(player.getUniqueId()));
		    player.sendMessage(message);
		}
		return true;		
	    } 
	}
	return false;
    }

    public GiftSet getGifts() {
	// TODO Auto-generated method stub
	return null;
    }
    
}
