package com.github.omwah.giftevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Encapsulates a set of items given to a player for an event
 */
public class GiftSet {
    private List<ItemStack> items;
    private String message;
    
    /*
     * Try and parse the item id from the item details map
     */
    private Integer parseItemId(Map<String, ?> itemDetails) {
        Object id_obj = itemDetails.get("id");
        
        // Try and parse the item id either as an integer or string
        Integer item_id = null;
        if(id_obj instanceof Integer) {
            item_id = (Integer) id_obj;
        } else if(id_obj instanceof String) {
            Material material_tmp = Material.valueOf((String) id_obj);
            if (material_tmp != null) {
                item_id = new Integer(material_tmp.getId());
            } else {
                item_id = null;
            }
        }
        
        return item_id;
    }
    
    /*
     * Try and parse the amount of the item
     */
    private Integer parseAmount(Map<String, ?> itemDetails) {
        // amount is optional, default amount is 1
        Integer amount = (Integer) itemDetails.get("amount");

        if(amount == null) {
            amount = new Integer(1);
        }
        return amount;
    }
    
    /*
     * Try and parse the damage of the item
     */
    private Integer parseDamage(Map<String, ?> itemDetails) {
        Integer damage = (Integer) itemDetails.get("damage");
        return damage;
    }
    
    /*
     * Try and parse and enchantment mapping
     */
    
    private Integer parseEnchantmentId(Object ench_obj) {
        Integer ench_id = null;
        if(ench_obj instanceof Integer) {
            ench_id = (Integer) ench_obj;
        } else if(ench_obj instanceof String) {
            Enchantment ench_tmp = Enchantment.getByName((String) ench_obj);
            if(ench_tmp != null) {
                ench_id = new Integer(ench_tmp.getId());
            } else {
                ench_id = null;
            }
        }

        return ench_id;
    }
    
    private Integer parseEnchantmentLevel(Object level_obj) {
        Integer level_int = null;
        if (level_obj instanceof Integer) {
            level_int = (Integer) level_obj;
        } else if(level_obj instanceof String) {
            level_int = new Integer((String) level_obj);
        }
        return level_int;
    }

    /*
     * Creates class from a gift: section of the config.yaml
     */
    
    public GiftSet(Logger logger, ConfigurationSection config_def) {
        String default_name = config_def.getString("name");
        
        this.message = config_def.getString("message");
        
        // Parse configuration to get items
        items = new ArrayList<ItemStack>();
        
        List<?> item_defs = config_def.getList("items");
        int item_count = 0;
        for(Object item : item_defs) {
            // Keep track of item count, for referencing in error messages
            item_count++;
            
            if (item instanceof Map) {
                Map<String, ?> item_details = (Map<String, ?>) item;

                // Try and parse the item id either as an integer or string
                Integer item_id = parseItemId(item_details);
                
                // Log an error below if item_id is null
                if (item_id != null) {
                    // amount is optional, default amount is 1
                    Integer amount = parseAmount(item_details);
                    
                    // Damage is optional, use correct constructor
                    // that includes damage
                    Integer damage = parseDamage(item_details);
                    
                    ItemStack new_item;
                    if(damage != null) {
                        new_item = new ItemStack(item_id, amount, damage.shortValue());
                    } else {
                        new_item = new ItemStack(item_id, amount);
                    }
                    
                    // Enchantments are optional
                    Map<Object, Object> enchantments = (Map<Object, Object>) item_details.get("enchantments");
                    
                    if (enchantments != null) {
                        for(Object ench_obj : enchantments.keySet()) {
                            Integer ench_id = parseEnchantmentId(ench_obj);
                            if(ench_id != null) {
                                Integer level = parseEnchantmentLevel(enchantments.get(ench_obj));
                                new_item.addUnsafeEnchantment(new EnchantmentWrapper(ench_id), level);
                            } else {
                                logger.log(Level.WARNING, "In {0} section enchantment id incorrect for item #{1}", new Object[]{config_def.getName(), item_count});
                            }
                        }
                    }
                    
                    // Use either name specified in item map, or default name, or none
                    // if neither are set
                    ItemMeta item_meta = new_item.getItemMeta();
                    String name = (String) item_details.get("name");
                    if(name != null) {
                        item_meta.setDisplayName(name);
                    } else if (default_name != null) {
                        item_meta.setDisplayName(default_name);
                    }
                    new_item.setItemMeta(item_meta);
                    
                    items.add(new_item);
                } else {
                    logger.log(Level.WARNING, "In {0} section id missing or incorrect for item #{1}", new Object[]{config_def.getName(), item_count});
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
