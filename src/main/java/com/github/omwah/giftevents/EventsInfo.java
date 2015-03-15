package com.github.omwah.giftevents;

import com.github.omwah.giftevents.gevent.GiftEvent;
import com.github.omwah.giftevents.gevent.IncrementalEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Keeps track of past events using a SQLite database
 */
public class EventsInfo {
    private final JavaPlugin plugin;
    private final Connection db_conn;
    private final Logger logger;
    private final boolean first_join_gift;

    /*
     * Creates a new instance at the given filename, prefix should be the Plugin's name  
     */
    public EventsInfo(JavaPlugin plugin, File filename, boolean firstJoinGift) throws SQLException, ClassNotFoundException {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.first_join_gift = firstJoinGift;
        
        Class.forName("org.sqlite.JDBC");
        this.db_conn = DriverManager.getConnection("jdbc:sqlite:" + filename.getAbsolutePath());
            
        initializeTables();
    }

    /*
     * Create database tables if they do not already exist
     */
    private void initializeTables() {

        try {
            Statement stmt = db_conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='past_events';");
            if(!rs.next()) {
                stmt.executeUpdate("CREATE TABLE past_events (event_name STRING, year INT, player STRING, gift_given INT, announcements_made INT, PRIMARY KEY(event_name, year, player));");
            }else {
            	convertToUUID("past_events");
            }
            rs.close();

            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='birthdays';");
            if(!rs.next()) {
                stmt.executeUpdate("CREATE TABLE birthdays (player STRING PRIMARY KEY, month INT, day INT);");
            }else {
            	convertToUUID("birthdays");
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='logins';");
            if(!rs.next()) {
                stmt.executeUpdate("CREATE TABLE logins (player STRING, month INT, day INT, year INT);");
            }
            rs.close();
            
            stmt.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create all necessary tables.");
            logger.log(Level.SEVERE, ex.toString());
        }
    }
    
    /*
     * Checks if all player entries are in the uuid format and converts it if necessary
     */    
    @SuppressWarnings("deprecation")
    private void convertToUUID(String table) {
	
	try {
	    Statement st = db_conn.createStatement();
	    ResultSet result = st.executeQuery("SELECT * FROM " + table + ";");
	    	
	    while (result.next()) {
		String playerEntry = result.getString("player");			
		try {
		    UUID.fromString(playerEntry);
		}catch(IllegalArgumentException e) {					
		    OfflinePlayer p = plugin.getServer().getOfflinePlayer(playerEntry);
		    logger.log(Level.INFO, "Converting GiftEvent database to UUIDs for Player " + playerEntry);
		    //	System.out.println("Converting " + playerEntry + " to " + p.getUniqueId() + " in table " + table);
		    st.executeUpdate("Update " + table + " SET player=\"" + p.getUniqueId() + "\" WHERE player=\"" + playerEntry + "\";");
		}
	    }
	    st.close();
	}catch (SQLException e) {
	    logger.log(Level.SEVERE, "Could not convert UUID's due to a SQLException");
	    logger.log(Level.SEVERE, e.toString());
	}
    }

    /*
     * Close database connection
     */
    public void close() {
        try {
            db_conn.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not close EventsInfo database");
        }            
    }

    /*
     * Gets the birthday for a player, for the current year
     */
    public Calendar getBirthday(OfflinePlayer playerObj) {
    	return playerObj!=null ? getBirthday(playerObj.getUniqueId()) : null;
    }
    
    /*
     * Gets the birthday for a player, for the current year
     */
    public Calendar getBirthday(UUID playerUUID) {
        // Return null if no player uuid is specified
        if(playerUUID == null) {
            return null;
        }
        
        Calendar now = Calendar.getInstance();
        try {
            Statement stmt = db_conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT month, day FROM birthdays WHERE player = \"" + playerUUID.toString() + "\";");
            if (rs.next()) {
                return new GregorianCalendar(now.get(Calendar.YEAR), rs.getInt(1), rs.getInt(2));
            }
            rs.close();
            stmt.close();
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not retrieve birthday for {0} due to a SQLException:", plugin.getServer().getPlayer(playerUUID).getName());
            logger.log(Level.SEVERE, ex.toString());
        }

        return null;
    }

    /*
     * Set the birthday for a user using a Calendar object
     */
    public boolean setBirthday(Calendar calDate, UUID playerUUID) {
        try {
            // Insert new entry
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate("INSERT OR REPLACE INTO birthdays VALUES (\"" + playerUUID.toString() + "\", " + calDate.get(Calendar.MONTH) + ", " + calDate.get(Calendar.DAY_OF_MONTH) + ");");
            stmt.close();
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not set birthday for {0} due to a SQLException:", plugin.getServer().getPlayer(playerUUID).getName());
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
    	if(playerObj!=null) {
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
    	}else {
    		return null;
    	}
    }

    /*
     * Get the first played date for a player based on their UUID
     */
    public Calendar getFirstPlayedDate(UUID playerUUID) {
        // Return null if no player uuid is specified
        if(playerUUID == null) {
            return null;
        }

        OfflinePlayer player_obj = plugin.getServer().getOfflinePlayer(playerUUID);
        if (player_obj != null) {
            return getFirstPlayedDate(player_obj);
        } else {
            return null;
        }
    }
    
    /*
     * Helper to retrieve the proper past events row
     */
    private ResultSet getPastEvent(GiftEvent event, UUID playerUUID) throws SQLException {
        Calendar now = Calendar.getInstance();

        Statement stmt = db_conn.createStatement();

        String eventName = (event instanceof IncrementalEvent) ? ((IncrementalEvent)event).getDateName(playerUUID).toLowerCase() : event.getName().toLowerCase();
        
        String count_query = 
                "SELECT COUNT(*) FROM past_events WHERE " +
                    "event_name = \"" + eventName + "\" AND " +
                    "player = \"" + playerUUID.toString() + "\" AND " +
                    "year = " + now.get(Calendar.YEAR) + ";";
        ResultSet count_res = stmt.executeQuery(count_query);
        
        count_res.next();
        if (count_res.getInt(1) == 0) {
            String insert_query = 
                "INSERT INTO past_events " +
                    "(event_name, year, player, gift_given, announcements_made) " +
                    "VALUES (" +
                    "\"" + eventName + "\", " +
                    now.get(Calendar.YEAR) + ", " +
                    "\"" + playerUUID.toString() + "\", " +
                    "0, 0);";
            stmt.executeUpdate(insert_query);
        }
        count_res.close();

        String select_query = 
                "SELECT * FROM past_events WHERE " +
                    "event_name = \"" + eventName + "\" AND " +
                    "player = \"" + playerUUID.toString() + "\" AND " +
                    "year = " + now.get(Calendar.YEAR) + ";";
        ResultSet select_res = stmt.executeQuery(select_query);
        
        return select_res;
    }
    
    /*
     * Queries database to see if a specific gift has been given to a player
     */
    public boolean hasGiftBeenGiven(GiftEvent event, UUID playerUUID) {       
        try {
            ResultSet rs = getPastEvent(event, playerUUID);

            if(rs != null && rs.next() && rs.getBoolean("gift_given")) {
                rs.close();
                return true;
            }
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not query if gift given for {0} due to a SQLException:", plugin.getServer().getPlayer(playerUUID).getName());
            logger.log(Level.SEVERE, ex.toString());
        }
        
        return false;       
    }

    /*
     * Sets in the database that a gift has been given to a player
     * 
     * Returns true of the query was succesful
     */
    public boolean setGiftGiven(GiftEvent event, UUID playerUUID, boolean given) {
        Calendar now = Calendar.getInstance();
                 
        try {
            int given_int = given ? 1 : 0;
            String eventName = (event instanceof IncrementalEvent) ? ((IncrementalEvent)event).getDateName(playerUUID).toLowerCase() : event.getName().toLowerCase();
            String update_query = "";
            // System.out.println("setGiftgiven for " + eventName + " to " + given_int + " with " + playerUUID.toString() + " ie: " + (event instanceof IncrementalEvent));
         
            if((event instanceof IncrementalEvent) && !given) {
        	update_query = 
        		"UPDATE past_events " + 
        		"SET gift_given = " + given_int + " " + 
        		"WHERE event_name LIKE \"" + event.getName().toLowerCase() + "-%\" " +
        		"AND player = \"" + playerUUID.toString() + "\" AND " +
                        "year = " + now.get(Calendar.YEAR) + ";";
            }else {
        	update_query = 
        		"UPDATE past_events " +
                        "SET gift_given = " + given_int + " " +
                        "WHERE " +
                        "event_name = \"" + eventName + "\" AND " +
                        "player = \"" + playerUUID.toString() + "\" AND " +
                        "year = " + now.get(Calendar.YEAR) + ";";
            }
            // System.out.println(update_query);
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate(update_query);
            stmt.close();
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not update gift given for {0} due to a SQLException:", plugin.getServer().getPlayer(playerUUID).getName());
            logger.log(Level.SEVERE, ex.toString());
        }        

        return false;        
    }
    
    /*
     * Gets how many annoucements have been made about this player for this event
     */
    public int getNumAnnoucementsMade(GiftEvent event, UUID playerUUID) {       
        try {
            ResultSet rs = getPastEvent(event, playerUUID);
            
            if(rs != null && rs.next()) {
                int num_announcements = rs.getInt("announcements_made");
                rs.close();
                return num_announcements;
            }
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not query number of announcements made for {0} due to a SQLException:", plugin.getServer().getPlayer(playerUUID).getName());
            logger.log(Level.SEVERE, ex.toString());
        }
        
        return 0;       
    }
    
    /*
     * Sets how many annoucements have been made about this player for this event
     */
    public boolean setNumAnnoucementsMade(GiftEvent event, UUID playerUUID, int numMade) {
        Calendar now = Calendar.getInstance();

        String eventName = (event instanceof IncrementalEvent) ? ((IncrementalEvent)event).getDateName(playerUUID) : event.getName();
                
        try {
            String update_query = 
                    "UPDATE past_events " +
                        "SET announcements_made = " + numMade + " " +
                        "WHERE " +
                        "event_name = \"" + eventName + "\" AND " +
                        "player = \"" + playerUUID.toString() + "\" AND " +
                        "year = " + now.get(Calendar.YEAR) + ";";
            Statement stmt = db_conn.createStatement();           
            stmt.executeUpdate(update_query);
            stmt.close();
            return true;
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "Could not set number of announcements made for {0} due to a SQLException:", plugin.getServer().getPlayer(playerUUID).getName());
            logger.log(Level.SEVERE, ex.toString());
        }
        
        return false;       
    }
   
    /*
     * Checks if a specific player was online on a specific date
     */
    private boolean logedInOn(UUID playerUUID, Calendar date) {
	
	try {
	    Statement stmt = db_conn.createStatement();
	    
	    String select_query =
		    "SELECT * FROM logins WHERE " +
			    "player = \"" + playerUUID.toString() + "\" AND " +
			    "month = \"" + (date.get(Calendar.MONTH)+1) + "\" AND " + 
			    "day = \"" + date.get(Calendar.DAY_OF_MONTH) + "\" AND " +
			    "year = \"" + date.get(Calendar.YEAR) + "\";";
	    ResultSet rs = stmt.executeQuery(select_query);
	    return rs.next();	    
	} catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to get last login date for player " + playerUUID.toString() + "!");
            logger.log(Level.SEVERE, ex.toString());
        }	
	return false;
    }
    
    /*
     * Adds the current date to the database
     */
    public boolean addLoginDate(UUID playerUUID) {
	Calendar now = Calendar.getInstance();
	
	if(logedInOn(playerUUID, now)) {
	    return false;
	}
	
	try {
	    Statement stmt = db_conn.createStatement();
	    
	    String insert_query = 
		    "INSERT INTO logins (player, month, day, year) VALUES(" +
		    "\"" + playerUUID.toString() + "\", " + (now.get(Calendar.MONTH)+1) + ", " + 
		    now.get(Calendar.DAY_OF_MONTH) + ", " + now.get(Calendar.YEAR) + ");";	    
	    
	    stmt.executeUpdate(insert_query);	    
	    return true;
	} catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to insert last login date for player " + playerUUID.toString() + "!");
            logger.log(Level.SEVERE, ex.toString());
        }
	return false;
    }
    
    /*
     * Get login dates as list for given userid
     */
    public List<Calendar> getLoginDates(UUID playerUUID) {
	
	List<Calendar> loginDates = new ArrayList<Calendar>(); 
	
	try {
	    Statement stmt = db_conn.createStatement();
	    
	    String select_query = "SELECT * FROM logins WHERE player=\"" + playerUUID.toString() + "\";";
	    ResultSet rs = stmt.executeQuery(select_query);
	    
	    if(rs!=null) {
		while(rs.next()) {
		    Calendar cal = new GregorianCalendar(rs.getInt("year"), rs.getInt("month"), rs.getInt("day"));
		    loginDates.add(cal);
		}
	    }
	    
	}catch(SQLException ex) {
	    logger.log(Level.SEVERE, "Failed to get login dates for player " + playerUUID.toString() + "!");
            logger.log(Level.SEVERE, ex.toString());
            return null;
	}	
	
	return loginDates;	
    }
    
    /*
     * Remove login dates for the given userid
     */
    public boolean resetLoginDates(UUID playerUUID) {
	
	try {
	    Statement stmt = db_conn.createStatement();
	    
	    String delete_query = "DELETE FROM logins WHERE player=\"" + playerUUID.toString() + "\";";
	    stmt.executeUpdate(delete_query);
	    return true;
	}catch(SQLException ex) {
	    logger.log(Level.SEVERE, "Failed to get login dates for player " + playerUUID.toString() + "!");
            logger.log(Level.SEVERE, ex.toString());          
	}
	return false;
    }
}
