package com.massivecraft.creativegates;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.massivecraft.creativegates.entity.UConf;
import com.massivecraft.creativegates.entity.UGate;
import com.massivecraft.creativegates.entity.UGateColls;
import com.massivecraft.mcore.ps.PS;
import com.massivecraft.mcore.util.MUtil;
import com.massivecraft.mcore.util.Txt;

public class MainListener implements Listener
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static MainListener i = new MainListener();
	public static MainListener get() { return i; }
	public MainListener() {}
	
	// -------------------------------------------- //
	// ACTIVATE & DEACTIVATE
	// -------------------------------------------- //
	
	public void activate()
	{
		Bukkit.getPluginManager().registerEvents(this, CreativeGates.get());
	}
	
	public void deactivate()
	{
		HandlerList.unregisterAll(this);
	}
	
	// -------------------------------------------- //
	// IS X NEARBY (UTIL)
	// -------------------------------------------- //
	
	public static boolean isPortalNearby(Block block)
	{
		final int radius = 2; 
		for (int dx = -radius; dx <= radius; dx++)
		{
			for (int dy = -radius; dy <= radius; dy++)
			{
				for (int dz = -radius; dz <= radius; dz++)
				{
					if (block.getRelative(dx, dy, dz).getType() == Material.PORTAL) return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isGateNearby(Block block)
	{
		final int radius = 2; 
		for (int dx = -radius; dx <= radius; dx++)
		{
			for (int dy = -radius; dy <= radius; dy++)
			{
				for (int dz = -radius; dz <= radius; dz++)
				{
					if (CreativeGates.get().getIndex().get(PS.valueOf(block)) != null) return true;
				}
			}
		}
		return false;
	}
	
	// -------------------------------------------- //
	// STABILIZE PORTAL CONENT
	// -------------------------------------------- //
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void stabilizePortalContent(BlockPhysicsEvent event)
    {
		// If a portal block is running physics ...
		Block block = event.getBlock();
		if (block.getType() != Material.PORTAL) return;
				
		// ... and we are filling or that block is stable according to our algorithm ...
		if (!(CreativeGates.get().isFilling() || isPortalBlockStable(block))) return;
		
		// ... then block the physics to stop the portal from disappearing.
		event.setCancelled(true);
    }
	
	public static boolean isPortalBlockStable(Block block)
	{
		if (CreativeGates.isVoid(block.getRelative(+0, +1, +0)) == true) return false;
		if (CreativeGates.isVoid(block.getRelative(+0, -1, +0)) == true) return false;
		
		if (CreativeGates.isVoid(block.getRelative(+1, +0, +0)) == false && CreativeGates.isVoid(block.getRelative(-1, +0, +0)) == false) return true;
		if (CreativeGates.isVoid(block.getRelative(+0, +0, +1)) == false && CreativeGates.isVoid(block.getRelative(+0, +0, -1)) == false) return true;
		
		return false;
	}
	
	// -------------------------------------------- //
	// DISABLE VANILLA PORTAL BEHAVIOR
	// -------------------------------------------- //
	
	public static void disableVanillaGates(Location location, Cancellable cancellable)
	{
		if (isGateNearby(location.getBlock()))
		{
			cancellable.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void disableVanillaGates(PlayerPortalEvent event)
	{
		disableVanillaGates(event.getFrom(), event);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void disableVanillaGates(EntityPortalEvent event)
	{
		disableVanillaGates(event.getFrom(), event);
	}
	
	// -------------------------------------------- //
	// USE GATE
	// -------------------------------------------- //
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void useGate(PlayerMoveEvent event)
	{
		// If a player is moving from one block to another ...
		if (MUtil.isSameBlock(event)) return;
		
		// ... and there is a gate in the new block ...
		UGate ugate = UGate.get(event.getTo());
		if (ugate == null) return;
		
		// ... and we have permisison to use gates ...
		Player player = event.getPlayer();
		if (!Perm.USE.has(player, true)) return;
		
		// ... and if the gate is intact ...
		if (!ugate.isIntact())
		{
			// We try to detect that a gate was destroyed once it happens by listening to a few events.
			// However there will always be cases we miss and by checking at use we catch those as well.
			// Examples could be map corruption or use of WorldEdit.
			ugate.destroy();
			return;
		}
		
		// ... and the gate has enter enabled ...
		if (!ugate.isEnterEnabled())
		{
			String message = Txt.parse("<i>This gate has enter disabled.");
			player.sendMessage(message);
			return;
		}
		
		// ... then transport the player.
		ugate.transport(player);
	}
	
	// -------------------------------------------- //
	// DESTROY GATE
	// -------------------------------------------- //
	
	public static void destroyGate(Block block)
	{
		UGate ugate = UGate.get(block);
		if (ugate == null) return;
		ugate.destroy();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(BlockBreakEvent event)
	{
		destroyGate(event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(EntityChangeBlockEvent event)
	{
		destroyGate(event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(EntityExplodeEvent event)
	{
		for (Block block : event.blockList())
		{
			destroyGate(block);
		}
	}
	
	// This one looks weird since it needs to handle beds as well
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(BlockPistonExtendEvent event)
	{
		Set<Block> blocks = new HashSet<Block>();
		
		Block piston = event.getBlock();
		Block extension = piston.getRelative(event.getDirection());
		blocks.add(extension);
		
		for (Block block : event.getBlocks())
		{
			blocks.add(block);
			blocks.add(block.getRelative(event.getDirection()));
		}
		
		for (Block block : blocks)
		{
			destroyGate(block);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(BlockPistonRetractEvent event)
	{
		destroyGate(event.getBlock().getRelative(event.getDirection(), 1));
		if (event.isSticky())
		{
			destroyGate(event.getBlock().getRelative(event.getDirection(), 2));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(BlockFadeEvent event)
	{
		destroyGate(event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void destroyGate(BlockBurnEvent event)
	{
		destroyGate(event.getBlock());
	}
	
	// -------------------------------------------- //
	// TOOLS
	// -------------------------------------------- //
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tools(PlayerInteractEvent event)
    {
		// If a player ...
		final Player player = event.getPlayer();
		
		// ... is clicking a block ...
		final Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return;
		
		// ... and the item in hand ...
		final ItemStack item = event.getItem();
		if (item == null) return;
		final Material material = item.getType();
		
		// ... is in any way an interesting material ...
		final UConf uconf = UConf.get(player);
		if
		(
			material != uconf.getMaterialInspect()
			&& 
			material != uconf.getMaterialMode()
			&& 
			material != uconf.getMaterialSecret()
			&&
			material != uconf.getMaterialCreate()
		)
		{
			return;
		}
		
		// ... then find the current gate ...
		final UGate currentGate = UGate.get(clickedBlock);
		
		String message = null;
		
		// ... and if ...
		if (material == uconf.getMaterialCreate())
		{
			// ... we are trying to create ...
			
			// ... check permission node ...
			if (!Perm.CREATE.has(player, true)) return;
			
			// ... check if the place is occupied ...
			if (currentGate != null)
			{
				message = Txt.parse("<b>There is no room for a new gate since there already is one here.");
				player.sendMessage(message);
				return;
			}
			
			// ... check if the item is named ...
			ItemMeta meta = item.getItemMeta();
			if (!meta.hasDisplayName())
			{
				message = Txt.parse("<b>You must name the %s before creating a gate with it.", Txt.getMaterialName(material));
				player.sendMessage(message);
				return;
			}
			String newNetworkId = ChatColor.stripColor(meta.getDisplayName());
			
			// ... perform the flood fill ...
			Block startBlock = clickedBlock.getRelative(event.getBlockFace());
			Entry<GateOrientation, Set<Block>> gateFloodInfo = FloodUtil.getGateFloodInfo(startBlock);
			if (gateFloodInfo == null)
			{
				message = Txt.parse("<b>There is no frame for the gate, or it's to big.", Txt.getMaterialName(material));
				player.sendMessage(message);
				return;
			}
			
			GateOrientation gateOrientation = gateFloodInfo.getKey();
			Set<Block> blocks = gateFloodInfo.getValue();
			
			// ... ensure the required blocks are present ...
			Map<Material, Integer> materialCounts = MaterialCountUtil.count(blocks);
			if (!MaterialCountUtil.has(materialCounts, uconf.getBlocksrequired()))
			{
				message = Txt.parse("<b>The frame must contain %s<b>.", MaterialCountUtil.desc(uconf.getBlocksrequired()));
				player.sendMessage(message);
				return;
			}
			
			// ... calculate the exit location ...
			PS exit = PS.valueOf(player.getLocation());
			exit = exit.withPitch(0F);
			exit = exit.withYaw(gateOrientation.getExitYaw(exit, PS.valueOf(blocks.iterator().next())));
			
			// ... calculate the coords ...
			Set<PS> coords = new HashSet<PS>();
			for (Block block : blocks)
			{
				coords.add(PS.valueOf(block).withWorld(null));
			}
			
			// ... create the gate ...
			UGate newGate = UGateColls.get().get(startBlock).create();
			newGate.setCreatorId(player.getName());
			newGate.setNetworkId(newNetworkId);
			newGate.setExit(exit);
			newGate.setCoords(coords);
			
			// ... set the air blocks to portal material ...
			newGate.fill();
			
			// ... un-enchant item ...
			ItemStack newItem = new ItemStack(item);
			ItemMeta newItemMeta = newItem.getItemMeta();
			newItemMeta.setDisplayName(null);
			newItem.setItemMeta(newItemMeta);
			player.setItemInHand(newItem);
			
			// ... run fx ...
			newGate.fxKitCreate(player);
			
			// ... inform the player.
			message = Txt.parse("<g>A \"<h>%s<g>\" gate takes form in front of you.", newNetworkId);
			player.sendMessage(message);
			
			message = Txt.parse("<i>The %s seems to have lost it's power.", Txt.getMaterialName(material));
			player.sendMessage(message);
		}
		else
		{
			// ... we are trying to do something else that create ...
			
			// ... and there is a gate ...
			if (currentGate == null)
			{
				// ... and there is no gate ...
				if (isPortalNearby(clickedBlock))
				{
					// ... but there is portal nearby.
					
					// ... exit with a message.
					player.sendMessage(Txt.parse("<i>You use the %s on the %s but there seem to be no gate.", Txt.getMaterialName(material), Txt.getMaterialName(clickedBlock.getType())));
					return;
				}
				else
				{
					// ... and there is no portal nearby ...
					
					// ... exit quietly.
					return;
				}
			}
			
			// ... send use action description ...
			message = Txt.parse("<i>You use the %s on the %s...", Txt.getMaterialName(material), Txt.getMaterialName(clickedBlock.getType()));
			player.sendMessage(message);
			
			if (material == uconf.getMaterialInspect())
			{
				// ... we are trying to inspect ...
				message = Txt.parse("<i>Some gate inscriptions are revealed:");
				player.sendMessage(message);
				
				boolean creator = currentGate.isCreator(player);
				boolean secret = currentGate.isNetworkSecret();
				boolean hidden = (secret && !creator);
				
				message = Txt.parse("<k>network: <v>") + (hidden ? Txt.parse("<magic>asdfasdfasdf") : currentGate.getNetworkId());
				if (secret) message += Txt.parse(" <i>(secret)");
				player.sendMessage(message);
				
				message = Txt.parse("<k>gates: <v>%d", currentGate.getGateChain().size());
			}
			else if (material == uconf.getMaterialSecret())
			{
				// ... we are trying to change secret state ...
				boolean creator = currentGate.isCreator(player);
				if (creator)
				{
					boolean secret = !currentGate.isNetworkSecret();
					currentGate.setNetworkSecret(secret);
					
					message = (secret ? Txt.parse("<h>Only you <i>can read the gate inscriptions now.") : Txt.parse("<h>Anyone <i>can read the gate inscriptions now."));
					player.sendMessage(message);
				}
				else
				{
					message = Txt.parse("<i>It seems <h>only the gate creator <i>can change inscription readability.", Txt.getMaterialName(material), Txt.getMaterialName(clickedBlock.getType()));
					player.sendMessage(message);
				}
			}
			else if (material == uconf.getMaterialMode())
			{
				// ... we are trying to change mode ...
				
				currentGate.toggleMode();
				
				String enter = currentGate.isEnterEnabled() ? Txt.parse("<g>enter enabled") : Txt.parse("<b>enter disabled");
				String exit = currentGate.isExitEnabled() ? Txt.parse("<g>exit enabled") : Txt.parse("<b>exit disabled");
				
				message = Txt.parse("<i>The gate now has %s <i>and %s<i>.", enter, exit);
				player.sendMessage(message);
			}
			
		}
		
    }
	
}
