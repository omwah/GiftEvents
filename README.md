GiftEvents
==========

This plugin handles giving out Gifts to players when they join on certain types of days:

* Their birthday if registered
* Their first played anniversary
* Arbitrary defined dates

For birthdays and anniversarys an announcement can optionally be made to all other players.
Gifts can optionally be received after an event should the player miss logging on.
Safeguards are in place to keep a player from changing their birthday to get additional gifts and to keep announcements from occurring too frequently.


Compilation
-----------

This plugin has a Maven 3 pom.xml and uses Maven to compile. Dependencies are 
therefore managed by Maven. You should be able to build it with Maven by running

    mvn package

a jar will be generated in the target folder. For those unfa1milliar with Maven
it is a build system, see http://maven.apache.org/ for more information.
