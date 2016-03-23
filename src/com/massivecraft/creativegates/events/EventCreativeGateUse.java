package com.massivecraft.creativegates.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.massivecraft.creativegates.entity.UGate;
import com.massivecraft.massivecore.event.EventMassiveCore;

public class EventCreativeGateUse extends EventMassiveCore
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
	
	private UGate gate;
	public final UGate getGate() { return this.gate; }
	public final void setGate(UGate gate) { this.gate = gate; }
	
	private final Player player;
	public final Player getPlayer() { return this.player; }
	
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public EventCreativeGateUse(UGate gate, Player player)
	{
		this.gate = gate;
		this.player = player;
	}

}
