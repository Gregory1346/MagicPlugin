package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Fireball;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FireballSpell extends Spell 
{
    int defaultSize = 1;
    
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
	    CraftPlayer cp = (CraftPlayer)player;
	    cp.launchProjectile(Fireball.class);
	    return true;
	}

    @Override
    public void onLoad(ConfigurationNode node)
    {
        disableTargeting();
    }
}
