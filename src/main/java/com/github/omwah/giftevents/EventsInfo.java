package com.github.omwah.giftevents;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import lib.PatPeter.SQLibrary.SQLite;
import lib.PatPeter.SQLibrary.DatabaseException;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

/*
 * Keeps track of past events using a SQLite database:w
 */
public class EventsInfo {
    private final SQLite db_conn;
    private final Logger logger;
    private final DateFormat date_format;

    /*
     * Creates a new instance at the given filename, prefix should be the Plugin's name  
     */
    public EventsInfo(Logger logger, String prefix, File filename, DateFormat date_format) {
        this.logger = logger;
        this.date_format = date_format;

        try {
            db_conn = new SQLite(logger, prefix, filename.getAbsolutePath(), filename.getName());
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, "Could not create EventsInfo database file: " + filename);
            throw e;
        }

        initializeTables();
    }

    /*
     * Create database tables if they do not already exist
     */
    private void initializeTables() {
        if(!db_conn.checkTable("past_events")) {
            db_conn.createTable("CREATE TABLE past_events (event_name STRING, month INT, day INT, year INT, player STRING, gift_given INT, announcements_left INT);");
        }

        if(!db_conn.checkTable("birthdays")) {
            db_conn.createTable("CREATE TABLE birthdays (month INT, day INT, player STRING);");
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
    public Calendar getBirthday(String playerName) {
        Calendar now = new GregorianCalendar();
        try {
            ResultSet rs = db_conn.query("SELECT month, day FROM birthdays WHERE player = " + playerName.toLowerCase() + ";");
            if (rs.getRow() > 0) {
                return new GregorianCalendar(now.get(Calendar.YEAR), rs.getInt(1), rs.getInt(2));
            }
            rs.close();
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Could not retrieve birthday for " + playerName + " due to a SQLException");
        }

        return null;
    }

    /*
     * Set the birthday for a user using a string formatted according to 
     * the format code in the config file
     */
    public boolean setBirthday(String dateStr, String playerName) {
        Date date;
        try {
            date = date_format.parse(dateStr);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "Could not set birthday for " + playerName + " due to a ParseException when attempting to convert date string");
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return setBirthday(calendar, playerName);
    }

    /*
     * Set the birthday for a user using a Calendar object
     */
    public boolean setBirthday(Calendar calDate, String playerName) {
        try {
            // Insert new entry
            db_conn.query("INSERT OR REPLACE INTO birthdays (month, day) VALUES (" + calDate.get(Calendar.MONTH) + ", " + calDate.get(Calendar.DAY_OF_MONTH) + ") WHERE player = " + playerName.toLowerCase() + ";");
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Could not set birthday for " + playerName + " due to a SQLException");
            return false;
        }

        // Success
        return true;
    }

}
