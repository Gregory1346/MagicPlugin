package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class ThrowItemAction extends BaseProjectileAction {
    private double itemSpeedMin;
    private double itemSpeedMax;
    private int ageItems;
    private boolean unbreakable;
    private SourceLocation sourceLocation;
    private ItemData item;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double itemSpeed = parameters.getDouble("speed", 1);
        itemSpeedMin = parameters.getDouble("speed_min", itemSpeed);
        itemSpeedMax = parameters.getDouble("speed_max", itemSpeed);
        ageItems = parameters.getInt("age_items", 5500);
        unbreakable = parameters.getBoolean("unbreakable", false);
        sourceLocation = new SourceLocation(parameters);

        String itemName = parameters.getString("item");
        if (itemName != null && !itemName.isEmpty()) {
            item = context.getController().getItem(itemName);
        }
    }

    @Override
    public SpellResult start(CastContext context)
    {
        Location spawnLocation = sourceLocation.getLocation(context);
        if (spawnLocation == null)
        {
            return SpellResult.NO_TARGET;
        }
        ItemStack itemStack = null;
        if (item != null) {
            itemStack = item.getItemStack(1);
        } else {
            MaterialAndData material = context.getBrush();
            if (material != null) {
                itemStack = new ItemStack(material.getMaterial(), 1, material.getData());
            }
        }

        if (itemStack == null)
        {
            return SpellResult.NO_TARGET;
        }
        double itemSpeed = context.getRandom().nextDouble() * (itemSpeedMax - itemSpeedMin) + itemSpeedMin;
        Vector velocity = spawnLocation.getDirection().normalize().multiply(itemSpeed);
        String removedMessage = context.getMessage("removed");
        if (removedMessage != null) {
            String name = context.getController().describeItem(itemStack);
            if (name == null) {
                name = "";
            }
            removedMessage = removedMessage.replace("$material", name);
        }
        NMSUtils.makeTemporary(itemStack, removedMessage);
        if (unbreakable) {
            itemStack = InventoryUtils.makeReal(itemStack);
            InventoryUtils.makeUnbreakable(itemStack);
        }
        Item droppedItem = null;
        try {
            droppedItem = spawnLocation.getWorld().dropItem(spawnLocation, itemStack);
        } catch (Exception ex) {

        }
        if (droppedItem == null) {
            context.getMage().sendDebugMessage("Failed to spawn item of type " + itemStack.getType());
            return SpellResult.FAIL;
        }
        droppedItem.setMetadata("temporary", new FixedMetadataValue(context.getController().getPlugin(), true));
        CompatibilityUtils.ageItem(droppedItem, ageItems);
        SafetyUtils.setVelocity(droppedItem, velocity);

        track(context, droppedItem);
        return checkTracking(context);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean usesBrush() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("speed");
        parameters.add("speed_min");
        parameters.add("speed_max");
        parameters.add("age_items");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("speed") || parameterKey.equals("age_items")
        || parameterKey.equals("speed_max") || parameterKey.equals("speed_min"))
        {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
