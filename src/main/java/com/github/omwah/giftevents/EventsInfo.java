package com.github.omwah.giftevents;

import com.github.omwah.giftevents.gevent.GiftEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.PatPeter.SQLibrary.DatabaseException;
import lib.PatPeter.SQLibrary.SQLite;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Keeps track of past events using a SQLite database
 */
public class EventsInfo {
    private final JavaPlugin plugin;
    private final SQLite db_conn;
    private final Logger logger;
    private final boolean first_join_gift;

    /*
     * Creates a new instance at the given filename, prefix should be the Plugin's name  
     */
    public EventsInfo(JavaPlugin plugin, String prefix, File filename, boolean firstJoinGift) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.first_join_gift = firstJoinGift;

        try {
            db_conn = new SQLite(logger, prefix, filename.getParent(), filename.getName());
            db_conn.open();
        } catch (DatabaseException ex) {
            logger.log(Level.SEVERE, "Could not create EventsInfo database file: {0}", filename);
            throw ex;
        }

        initializeTables();
    }

    /*
     * Create database tables if they do not already exist
     */
    private void initializeTables() {

        try {
            if(!db_conn.isTable("past_events")) {
                db_conn.query("CREATE TABLE past_events (event_name STRING, year INT, player STRING, gift_given INT, announcements_made INT, PRIMARY KEY(event_name, year, player));");
            }

            if(!db_conn.isTable("birthdays")) {
                db_conn.query("CREATE TABLE birthdays (player STRING PRIMARY KEY, month INT, day INT);");
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create all necessary tables.");
            logger.log(Level.SEVERE, ex.toString());
        }
    }

    /*
     * Close database connection
     */
    public void close() {
        db_conn.close();
    }

    /*
     * Gets the birthday for a player, for the current year
     */
    public Calendar getBirthday(OfflinePlayer playerObj) {
        return getBirthday(playerObj.getName());
    }
    
    /*
     * Gets the birthday for a player, for the current year
     */
    public Calendar getBirthday(String playerName) {
        // Return null if no player name is specified
        if(playerName == null) {
            return null;
        }
        
        Calendar now = Calendar.getInstance();
        try {
            ResultSet rs = db_conn.query("SELECT month, day FROM birthdays WHERE player = \"" + playerName.toLowerCase() + "\";");
            if (rs.next()) {
                return new GregorianCalendar(now.get(Calendar.YEAR), rs.getInt(1), rs.getInt(2));
            }
            rs.close();
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not retrieve birthday for {0} due to a SQLException:", playerName);
            logger.log(Level.SEVERE, ex.toString());
        }

        return null;
    }

    /*
     * Set the birthday for a user using a Calendar object
     */
    public boolean setBirthday(Calendar calDate, String playerName) {
        try {
            // Insert new entry
            db_conn.query("INSERT OR REPLACE INTO birthdays VALUES (\"" + playerName.toLowerCase() + "\", " + calDate.get(Calendar.MONTH) + ", " + calDate.get(Calendar.DAY_OF_MONTH) + ");");
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not set birthday for {0} due to a SQLException:", playerName);
            logger.log(Level.SEVERE, ex.toString());
            return false;
        }

        // Success
        return true;
    }
    
    /*
     * Gets the player first played date
     */
    public Calendar getFirstPlayedDate(OfflinePlayer playerObj) {
        Calendar fp_cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        fp_cal.setTime(new Date(playerObj.getFirstPlayed()));
        
        // Don't return date if the player has just joined the server
        // prevents them from getting gifts when they first join
        if(!first_join_gift &&
           now.get(Calendar.MONTH) ==  fp_cal.get(Calendar.MONTH) &&
           now.get(Calendar.DAY_OF_MONTH) ==  fp_cal.get(Calendar.DAY_OF_MONTH) &&
           now.get(Calendar.YEAR) == fp_cal.get(Calendar.YEAR)) {
            return null;
        } else {
            return fp_cal;
        }
    }

    /*
     * Get the first played date for a player based on their player name
     */
    public Calendar getFirstPlayedDate(String playerName) {
        // Return null if no player name is specified
        if(playerName == null) {
            return null;
        }

        OfflinePlayer player_obj = plugin.getServer().getOfflinePlayer(playerName.toLowerCase());
        if (player_obj != null) {
            return getFirstPlayedDate(player_obj);
        } else {
            return null;
        }
    }
    
    /*
     * Helper to retrieve the proper past events row
     */
    private ResultSet getPastEvent(GiftEvent event, String playerName) throws SQLException {
        Calendar now = Calendar.getInstance();
        
        String count_query = 
                "SELECT COUNT(*) FROM past_events WHERE " +
                    "event_name = \"" + event.getName().toLowerCase() + "\" AND " +
                    "player = \"" + playerName.toLowerCase() + "\" AND " +
                    "year = " + now.get(Calendar.YEAR) + ";";
        ResultSet count_res = db_conn.query(count_query);
        
        count_res.next();
        if (count_res.getInt(1) == 0) {
            String insert_query = 
                "INSERT INTO past_events " +
                    "(event_name, year, player, gift_given, announcements_made) " +
                    "VALUES (" +
                    "\"" + event.getName().toLowerCase() + "\", " +
                    now.get(Calendar.YEAR) + ", " +
                    "\"" + playerName.toLowerCase() + "\", " +
                    "0, 0);";
            ResultSet insert_res = db_conn.query(insert_query);
        }

        String select_query = 
                "SELECT * FROM past_events WHERE " +
                    "event_name = \"" + event.getName() + "\" AND " +
                    "player = \"" + playerName.toLowerCase() + "\" AND " +
                    "year = " + now.get(Calendar.YEAR) + ";";
        ResultSet select_res = db_conn.query(select_query);      
        
        return select_res;
    }
    
    /*
     * Queries database to see if a specific gift has been given to a player
     */
    public boolean hasGiftBeenGiven(GiftEvent event, String playerName) {       
        try {
            ResultSet rs = getPastEvent(event, playerName);

            if(rs != null && rs.next() && rs.getBoolean("gift_given")) {
                rs.close();
                return true;
            }
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not query if gift given for {0} due to a SQLException:", playerName);
            logger.log(Level.SEVERE, ex.toString());
        }
        
        return false;       
    }

    /*
     * Sets in the database that a gift has been given to a player
     * 
     * Returns true of the query was succesful
     */
    public boolean setGiftGiven(GiftEvent event, String playerName, boolean given) {
        Calendar now = Calendar.getInstance();

        try {
            int given_int = given ? 1 : 0;
            String update_query = 
                    "UPDATE past_events " +
                        "SET gift_given = " + given_int + " " +
                        "WHERE " +
                        "event_name = \"" + event.getName() + "\" AND " +
                        "player = \"" + playerName.toLowerCase() + "\" AND " +
                        "year = " + now.get(Calendar.YEAR) + ";";
            ResultSet update_res = db_conn.query(update_query);      
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not update gift given for {0} due to a SQLException:", playerName);
            logger.log(Level.SEVERE, ex.toString());
        }        

        return false;        
    }
    
    /*
     * Gets how many annoucements have been made about this player for this event
     */
    public int getNumAnnoucementsMade(GiftEvent event, String playerName) {       
        try {
            ResultSet rs = getPastEvent(event, playerName);
            
            if(rs != null && rs.next()) {
                int num_announcements = rs.getInt("announcements_made");
                rs.close();
                return num_announcements;
            }
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not query number of announcements made for {0} due to a SQLException:", playerName);
            logger.log(Level.SEVERE, ex.toString());
        }
        
        return 0;       
    }
    
    /*
     * Sets how many annoucements have been made about this player for this event
     */
    public boolean setNumAnnoucementsMade(GiftEvent event, String playerName, int numMade) {
        Calendar now = Calendar.getInstance();

        try {
            String update_query = 
                    "UPDATE past_events " +
                        "SET announcements_made = " + numMade + " " +
                        "WHERE " +
                        "event_name = \"" + event.getName() + "\" AND " +
                        "player = \"" + playerName.toLowerCase() + "\" AND " +
                        "year = " + now.get(Calendar.YEAR) + ";";
            ResultSet update_res = db_conn.query(update_query);      
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not set number of announcements made for {0} due to a SQLException:", playerName);
            logger.log(Level.SEVERE, ex.toString());
        }
        
        return false;       
    }
}
