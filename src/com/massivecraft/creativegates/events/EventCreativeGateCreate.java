package com.massivecraft.creativegates.events;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.massivecraft.massivecore.event.EventMassiveCore;
import com.massivecraft.massivecore.ps.PS;

public class EventCreativeGateCreate extends EventMassiveCore
{
	// -------------------------------------------- //
	// REQUIRED EVENT CODE
	// -------------------------------------------- //
	
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //
	
	private final Player player;
	public final Player getPlayer() { return this.player; }
	
	private Block startBlock;
	public final Block getStartBlock() { return this.startBlock; }
	public final void setStartBlock(Block startBlock) { this.startBlock = startBlock; }
	
	private String networkId;
	public final String getNetworkId() { return this.networkId; }
	public final void setNetworkId(String networkId) { this.networkId = networkId; }
	
	private PS exit;
	public final PS getExit() { return this.exit; }
	public final void setExit(PS exit) { this.exit = exit; }
	
	private Set<PS> coords;
	public final Set<PS> getCoords() { return this.coords; }
	public final void setCoords(Set<PS> coords) { this.coords = coords; }
	
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public EventCreativeGateCreate(Player player, Block startBlock, String networkId, PS exit, Set<PS> coords)
	{
		this.player = player;
		this.startBlock = startBlock;
		this.networkId = networkId;
		this.exit = exit;
		this.coords = coords;
	}
	
}
