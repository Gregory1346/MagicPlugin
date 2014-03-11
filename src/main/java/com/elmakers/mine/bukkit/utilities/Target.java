package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Target implements Comparable<Target>
{
	protected int    maxDistance = 512;
	protected double maxAngle    = 0.3;

	private Entity   entity;
	private Location   source;
	private Block    block;

	private double   distance    = 100000;
	private double   angle       = 10000;
	private int      score       = 0;

	public Target(Location sourceLocation)
	{
		this.source = sourceLocation;
	}
	
	public Target(Location sourceLocation, Block block)
	{
		this.source = sourceLocation;
		this.block = block;
		calculateScore();
	}

	public Target(Location sourceLocation, Entity entity, int range)
	{
		this.maxDistance = range;
		this.source = sourceLocation;
		this.entity = entity;
		calculateScore();
	}
	
	public Target(Location sourceLocation, Entity entity)
	{
		this.maxDistance = 0;
		this.source = sourceLocation;
		this.entity = entity;
	}
	
	public Target(Location sourceLocation, Entity entity, Block block)
	{
		this.maxDistance = 0;
		this.source = sourceLocation;
		this.entity = entity;
		this.block = block;
	}

	public int getScore()
	{
		return score;
	}

	protected void calculateScore()
	{
		Vector playerFacing = source.getDirection();
		Vector playerLoc = new Vector(source.getBlockX(), source.getBlockY(), source.getBlockZ());

		Location targetLocation = getLocation();
		if (targetLocation == null) return;

		Vector targetLoc = new Vector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		Vector targetDirection = new Vector(targetLoc.getBlockX() - playerLoc.getBlockX(), targetLoc.getBlockY() - playerLoc.getBlockY(), targetLoc.getBlockZ() - playerLoc.getBlockZ());
		angle = targetDirection.angle(playerFacing);
		distance = targetDirection.length();

		score = 0;
		if (angle > maxAngle) return;
		if (distance > maxDistance) return;

		score = (int)((maxDistance - distance) + (3 - angle) * 4);

		// Favor targeting monsters, a bit
		if (entity instanceof Player)
		{
			score = score + 3;
		}
		else  if (entity instanceof LivingEntity)
		{
			score = score + 5;
		}
		else
		{
			score = score + 1;
		}
	}

	public int compareTo(Target other) 
	{
		return other.score - this.score;
	}

	public boolean isEntity()
	{
		return entity != null;
	}

	public boolean isBlock()
	{
		return block != null;
	}

	public boolean hasTarget()
	{
		return isEntity() || isBlock();
	}

	public Entity getEntity()
	{
		return entity;
	}

	public Block getBlock()
	{
		if (block == null && entity != null)
		{
			return entity.getLocation().getBlock();
		}
		return block;
	}

	public double getDistance()
	{
		return distance;
	}

	public Location getLocation()
	{
		if (entity != null)
		{
			return entity.getLocation();
		}

		if (block != null)
		{
			return block.getLocation();
		}

		return null;
	}
}