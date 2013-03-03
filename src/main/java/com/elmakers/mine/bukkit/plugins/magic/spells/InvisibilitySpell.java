package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class InvisibilitySpell extends Spell
{
    protected ArrayList<Player> cloaked = new ArrayList<Player>();
    protected Location location;

    @Override
    public boolean onCast(ConfigurationNode parameters) 
    {
        this.location = player.getLocation();
       
        String typeString = parameters.getString("type", "");
        if (typeString.equals("decoy"))
        {
            decoy();
        }
       
        cloak();
        sendMessage(player, "You are invisible");
        
        spells.registerEvent(SpellEventType.PLAYER_MOVE, this);
        return true;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!hasMoved()) return;
        
        sendMessage(player, "You are visible again");
        uncloak();
        spells.unregisterEvent(SpellEventType.PLAYER_MOVE, this);
    }

    public boolean hasMoved()
    {
        Location current = player.getLocation();
        
        return (current.getBlockX() != location.getBlockX() || current.getBlockY() != location.getBlockY() || current.getBlockZ() != location.getBlockZ());
    }
    
    public void decoy()
    {
        uncloak();
        cloak();
    }
    
    public void cloak()
    {
        uncloak();
        
        List<Player> others = player.getWorld().getPlayers();
        for (Player other : others)
        {
            if (other != player && inRange(other))
            {
                cloaked.add(other);
                cloakFrom(other);
            }
        }
        
    }
    
    public void uncloak()
    {
        for (Player other : cloaked)
        {
            uncloakFrom(other); 
        }
        
        cloaked.clear();
    }
    
    protected boolean inRange(Player other) 
    {
        Location playerLocation = player.getLocation();
        Location otherLocation = other.getLocation();
        
        // hide from players under 256 blocks away
        int maxDistance = 256;
        return 
        (
            Math.pow(playerLocation.getX() - otherLocation.getX(), 2) 
        +   Math.pow(playerLocation.getY() - otherLocation.getY(), 2)
        +   Math.pow(playerLocation.getZ() - otherLocation.getZ(), 2)
        ) < maxDistance * maxDistance;
    }
    
    protected void cloakFrom(Player other)
    {
        // TODO: FIXME ... seems like there is built-in invisibility now?
        
        CraftPlayer hide = (CraftPlayer)player;
        CraftPlayer hideFrom = (CraftPlayer)other;
        
        EntityPlayer fromEntity = hideFrom.getHandle();
        
        // fromEntity.netServerHandler.sendPacket(new Packet29DestroyEntity(hide.getEntityId()));        
    }
    
    protected void uncloakFrom(Player other)
    {
        // TODO: FIXME ... seems like there is built-in invisibility now?
        
        CraftPlayer hide = (CraftPlayer)player;
        CraftPlayer hideFrom = (CraftPlayer)other;
        
        EntityPlayer hideEntity = hide.getHandle();
        EntityPlayer fromEntity = hideFrom.getHandle();
        
        // fromEntity.netServerHandler.sendPacket(new Packet20NamedEntitySpawn(hideEntity));
    }

    @Override
    public void onLoad(ConfigurationNode node)
    {
        disableTargeting();
    }
}
