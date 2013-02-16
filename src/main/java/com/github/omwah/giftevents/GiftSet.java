package com.github.omwah.giftevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

/**
 * Encapsulates a set of items given to a player for an event
 */
public class GiftSet {
    private ArrayList<ItemStack> items;
    private String message;
    
    public GiftSet(ConfigurationSection config_def) {
        items = new ArrayList<ItemStack>();
        
        // Parse configuration to get items
        List<?> item_defs = config_def.getList("items");
        for(Object item : item_defs) {
            if (item instanceof Map) {
                Map<String, ?> item_details = (Map<String, ?>) item;
                Integer item_id = (Integer) item_details.get("id");
                if (item_id != null) {
                    Integer amount = (Integer) item_details.get("amount");
                    
                    if(amount == null) {
                        amount = new Integer(1);
                    }
                    
                    ItemStack new_item = new ItemStack(item_id, amount);
                    
                    Map<Integer, Integer> enchantments = (Map<Integer, Integer>) item_details.get("enchantments");
                    
                    if (enchantments != null) {
                        for(Integer ench_id : enchantments.keySet()) {
                            Integer level = enchantments.get(ench_id);
                            new_item.addUnsafeEnchantment(new EnchantmentWrapper(ench_id), level);
                        }
                    }
                    
                    items.add(new_item);
                }
            }
        }
    }
}
