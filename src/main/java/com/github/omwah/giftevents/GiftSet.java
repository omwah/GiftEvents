package com.github.omwah.giftevents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Encapsulates a set of items given to a player for an event
 */
public class GiftSet {
    private ArrayList<ItemStack> items;
    private String message;
    
    /*
     * Creates class from a gift: section of the config.yaml
     */
    
    public GiftSet(Logger logger, ConfigurationSection config_def) {
        String default_name = config_def.getString("name");
        
        this.message = config_def.getString("message");
        
        // Parse configuration to get items
        List<?> item_defs = config_def.getList("items");
        int item_count = 0;
        for(Object item : item_defs) {
            // Keep track of item count, for referencing in error messages
            item_count++;
            
            if (item instanceof Map) {
                Map<String, ?> item_details = (Map<String, ?>) item;
                Integer item_id = (Integer) item_details.get("id");
                if (item_id != null) {
                    // amount is optional, default amount is 1
                    Integer amount = (Integer) item_details.get("amount");
                    
                    if(amount == null) {
                        amount = new Integer(1);
                    }
                    
                    ItemStack new_item = new ItemStack(item_id, amount);
                    
                    // Enchantments are optional
                    Map<Integer, Integer> enchantments = (Map<Integer, Integer>) item_details.get("enchantments");
                    
                    if (enchantments != null) {
                        for(Integer ench_id : enchantments.keySet()) {
                            Integer level = enchantments.get(ench_id);
                            new_item.addUnsafeEnchantment(new EnchantmentWrapper(ench_id), level);
                        }
                    }
                    
                    // Use either name specified in item map, or default name, or none
                    // if neither are set
                    String name = (String) item_details.get("name");
                    if(name != null) {
                        new_item.getItemMeta().setDisplayName(name);
                    } else if (default_name != null) {
                        new_item.getItemMeta().setDisplayName(default_name);
                    }
                    
                    items.add(new_item);
                } else {
                    logger.log(Level.WARNING, "In {0} section id missing for item #{1}", new Object[]{config_def.getName(), item_count});
                } // end if item_id != null
            } else {
                logger.log(Level.WARNING, "In {0} section expected Map instance for item #{1} instead of: {2}", new Object[]{config_def.getName(), item_count, item.getClass()});
            }
        } // end item loop
    }
    
    /*
     * Returns the message template to be sent to the player when they recieve 
     * their gifts
     */
    public String getMessageTemplate() {
        return message;
    }
    
    /*
     * Place the items represented by this instance into the player's inventory
     * Send them a message about their gift if it is enabled
     */
    public void giveToPlayer(Player player) {
        // Try and place items in player's inventory and drop them next to 
        // them if they can't fit
        for(ItemStack item : items) {
            HashMap<Integer,ItemStack> dropped_items;
            dropped_items = player.getInventory().addItem(item);
            
            // Check if they are online, just in case
            if(player.isOnline()) {
                World player_world = player.getLocation().getWorld();
                // Drop items next to player if they dont fit
                for(ItemStack drop_item : dropped_items.values()) {
                    player_world.dropItemNaturally(player.getLocation(), item);
                }
            }
        }

    }
}
