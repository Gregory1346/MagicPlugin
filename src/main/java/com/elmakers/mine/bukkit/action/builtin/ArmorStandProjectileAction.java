package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.math.VectorTransform;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ArmorStandProjectileAction extends CustomProjectileAction {
    private boolean smallArmorStand = false;
    private boolean armorStandMarker = false;
    private boolean armorStandInvisible = false;
    private boolean noTarget = true;
    private ItemStack heldItem = null;
    private ItemStack helmetItem = null;
    private ItemStack chestplateItem = null;
    private ItemStack leggingsItem = null;
    private ItemStack bootsItem = null;
    private Vector armorStandOffset;
    private CreatureSpawnEvent.SpawnReason armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private VectorTransform leftArmTransform;
    private VectorTransform rightArmTransform;
    private VectorTransform leftLegTransform;
    private VectorTransform rightLegTransform;
    private VectorTransform bodyTransform;
    private VectorTransform headTransform;

    private ArmorStand armorStand = null;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        if (parameters.isConfigurationSection("left_arm_transform")) {
            leftArmTransform = new VectorTransform(parameters.getConfigurationSection("right_arm_transform"));
        }
        if (parameters.isConfigurationSection("right_arm_transform")) {
            rightArmTransform = new VectorTransform(parameters.getConfigurationSection("right_arm_transform"));
        }
        if (parameters.isConfigurationSection("right_leg_transform")) {
            rightLegTransform = new VectorTransform(parameters.getConfigurationSection("right_leg_transform"));
        }
        if (parameters.isConfigurationSection("left_leg_transform")) {
            leftLegTransform = new VectorTransform(parameters.getConfigurationSection("left_leg_transform"));
        }
        if (parameters.isConfigurationSection("head_transform")) {
            headTransform = new VectorTransform(parameters.getConfigurationSection("head_transform"));
        }
        if (parameters.isConfigurationSection("body_transform")) {
            bodyTransform = new VectorTransform(parameters.getConfigurationSection("body_transform"));
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        armorStandMarker = parameters.getBoolean("armor_stand_marker", true);
        armorStandInvisible = parameters.getBoolean("armor_stand_invisible", true);
        smallArmorStand = parameters.getBoolean("armor_stand_small", false);
        noTarget = parameters.getBoolean("no_target", true);
        armorStandOffset = ConfigurationUtils.getVector(parameters, "armor_stand_offset");
        MaterialAndData itemType = ConfigurationUtils.getMaterialAndData(parameters, "held_item");
        if (itemType != null) {
            heldItem = itemType.getItemStack(1);
        }
        itemType = ConfigurationUtils.getMaterialAndData(parameters, "helmet_item");
        if (itemType != null) {
            helmetItem = itemType.getItemStack(1);
        }
        itemType = ConfigurationUtils.getMaterialAndData(parameters, "chestplate_item");
        if (itemType != null) {
            chestplateItem = itemType.getItemStack(1);
        }
        itemType = ConfigurationUtils.getMaterialAndData(parameters, "leggings_item");
        if (itemType != null) {
            leggingsItem = itemType.getItemStack(1);
        }
        itemType = ConfigurationUtils.getMaterialAndData(parameters, "boots_item");
        if (itemType != null) {
            bootsItem = itemType.getItemStack(1);
        }
        if (parameters.contains("spawn_reason")) {
            String reasonText = parameters.getString("spawn_reason").toUpperCase();
            try {
                armorStandSpawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                context.getMage().sendMessage("Unknown spawn reason: " + reasonText);
            }
        }
    }

    @Override
    public SpellResult start(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        armorStand = CompatibilityUtils.spawnArmorStand(mage.getEyeLocation());
        Location location = mage.getEyeLocation();
        CompatibilityUtils.setYawPitch(armorStand, location.getYaw(), location.getPitch());
        armorStand.setItemInHand(heldItem);
        armorStand.setHelmet(helmetItem);
        armorStand.setChestplate(chestplateItem);
        armorStand.setLeggings(leggingsItem);
        armorStand.setBoots(bootsItem);
        armorStand.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        if (armorStandInvisible) {
            CompatibilityUtils.setInvisible(armorStand, true);
        }
        if (armorStandMarker) {
            CompatibilityUtils.setMarker(armorStand, true);
        }
        CompatibilityUtils.setGravity(armorStand, false);
        CompatibilityUtils.setDisabledSlots(armorStand, 2039552);
        if (smallArmorStand) {
            CompatibilityUtils.setSmall(armorStand, true);
        }
        if (noTarget) {
            armorStand.setMetadata("notarget", new FixedMetadataValue(controller.getPlugin(), true));
        }
        CompatibilityUtils.addToWorld(location.getWorld(), armorStand, armorStandSpawnReason);

        return super.start(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = super.step(context);
        Location target = actionContext.getTargetLocation();
        if (armorStandOffset != null) {
            target = target.clone().add(armorStandOffset);
        }
        armorStand.teleport(target);
        if (leftArmTransform != null) {
            Vector direction = leftArmTransform.get(launchLocation, flightTime);
            armorStand.setLeftArmPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (rightArmTransform != null) {
            Vector direction = rightArmTransform.get(launchLocation, flightTime);
            armorStand.setRightArmPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (leftLegTransform != null) {
            Vector direction = leftLegTransform.get(launchLocation, flightTime);
            armorStand.setLeftLegPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (rightLegTransform != null) {
            Vector direction = rightLegTransform.get(launchLocation, flightTime);
            armorStand.setRightLegPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (bodyTransform != null) {
            Vector direction = bodyTransform.get(launchLocation, flightTime);
            armorStand.setBodyPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        if (headTransform != null) {
            Vector direction = headTransform.get(launchLocation, flightTime);
            armorStand.setHeadPose(new EulerAngle(direction.getX(), direction.getY(), direction.getZ()));
        }
        return result;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        armorStand.remove();
    }
}