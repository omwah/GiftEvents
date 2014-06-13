package com.github.omwah.giftevents;

import com.github.omwah.giftevents.gevent.GiftEvent;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

/*
 */
public class GiftEventsListener implements Listener {
    private final GiftEventsPlugin plugin;
    private final EventsInfo events_info;
    private final int max_announcements;

    /*
     * Construct listener
     */
    public GiftEventsListener(GiftEventsPlugin plugin, int max_announcements) {
        this.plugin = plugin;
        this.events_info = plugin.getEventsInfo();
        this.max_announcements = max_announcements;
    }
    
    private boolean doesDateMatch(Calendar date, boolean belated) {
        Calendar now = new GregorianCalendar();
        if(now.get(Calendar.MONTH) ==  date.get(Calendar.MONTH) &&
           now.get(Calendar.DAY_OF_MONTH) ==  date.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else if(belated &&
                  ( (now.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    now.get(Calendar.DAY_OF_MONTH) > date.get(Calendar.DAY_OF_MONTH)) ||
                   (now.get(Calendar.MONTH) > date.get(Calendar.MONTH)) ) ) {
            return true;
        }

        return false;
    }

    /*
     * Check for events for player
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent bukkit_event) {
        Player player = bukkit_event.getPlayer();       
        UUID playerUUID = player.getUniqueId();
        
        // For each event check if it applies and give a gift if
        // applicable
        for(GiftEvent gift_event : plugin.getEvents()) {
            Calendar event_date = gift_event.getDate(playerUUID);
            if(event_date != null && player.hasPermission(gift_event.getPermissionPath())) {

                // Check if we should make an annoucement, don't announce for belated events
                if (doesDateMatch(event_date, false)) {
                    int num_annoucements = events_info.getNumAnnoucementsMade(gift_event, playerUUID);
                    String annoucement = gift_event.getAnnouncement(playerUUID);
                    if(annoucement != null && num_annoucements < max_announcements) {
                        plugin.getServer().broadcastMessage(annoucement);
                        events_info.setNumAnnoucementsMade(gift_event, playerUUID, num_annoucements + 1);
                    }
                }

                // Check if player can recieve a gift, allow belated gifts if configured
                if(doesDateMatch(event_date, gift_event.canGiveBelated()) &&
                        !events_info.hasGiftBeenGiven(gift_event, playerUUID)) {
                    gift_event.giveGifts(player);
                    events_info.setGiftGiven(gift_event, playerUUID, true);
                }

            }
        }
    }
    
}
