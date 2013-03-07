package com.github.omwah.giftevents;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/*
 */
public class GiftEventsListener implements Listener {
    private final Logger logger;
    private final List<GiftEvent> events;
    private final EventsInfo events_info;
    private final Server server;
    private final int max_announcements;

    /*
     * Construct listener
     */
    public GiftEventsListener(Logger logger, List<GiftEvent> events, EventsInfo events_info, Server server, int max_announcements) {
        this.logger = logger;
        this.events = events;
        this.events_info = events_info;
        this.server = server;
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
        String playerName = player.getName();
        
        // For each event check if it applies and give a gift if
        // applicable
        for(GiftEvent gift_event : events) {
            Calendar event_date = gift_event.getDate(playerName);
            if(event_date != null && player.hasPermission(gift_event.getPermissionPath())) {

                // Check if we should make an annoucement, don't announce for belated events
                if (doesDateMatch(event_date, false)) {
                    int num_annoucements = events_info.getNumAnnoucementsMade(gift_event, playerName);
                    String annoucement = gift_event.getAnnouncement(player.getName());
                    if(annoucement != null && num_annoucements < max_announcements) {
                        server.broadcastMessage(annoucement);
                        events_info.setNumAnnoucementsMade(gift_event, playerName, num_annoucements + 1);
                    }
                }

                // Check if player can recieve a gift, allow belated gifts if configured
                if(doesDateMatch(event_date, gift_event.canGiveBelated()) &&
                        !events_info.hasGiftBeenGiven(gift_event, playerName)) {
                    gift_event.giveGifts(player);
                    events_info.setGiftGiven(gift_event, playerName, true);
                }

            }
        }
    }
    
}
