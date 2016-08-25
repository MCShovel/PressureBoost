package com.steamcraftmc.PressureBoost;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WorldEvents implements Listener {
	MainPlugin plugin;
	final double _yScale, _yBase;

	public WorldEvents(MainPlugin plugin) {
		this.plugin = plugin;
		this._yScale = 0.1;
		this._yBase = 0.5;
	}

	// Any plate on a Redstone Block:
	private boolean isValidPressureBoost(Block plate) {
		Material type = plate.getType();
		if (type == Material.STONE_PLATE || type == Material.IRON_PLATE
				|| type == Material.GOLD_PLATE) {
			Location below = new Location(plate.getWorld(), plate.getX(), plate.getY() - 1, plate.getZ());
			Material typeBelow = plate.getWorld().getBlockAt(below).getType();
			return (typeBelow == Material.REDSTONE_BLOCK);
		}
		return false;
	}

	private double getPressureBoostPower(Block plate) {
		int power = 0;
		Location below = new Location(plate.getWorld(), plate.getX(), plate.getY() - 2, plate.getZ());
		while (plate.getWorld().getBlockAt(below).getType() == Material.REDSTONE_BLOCK) {
			if (++power > 4)
				break;
			below = new Location(plate.getWorld(), plate.getX(), below.getY() - 1, plate.getZ());
		}
		return Math.pow(2, power);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e) {

		if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL) {
			Block block = e.getEntity().getWorld().getBlockAt(e.getEntity().getLocation());
			if (isValidPressureBoost(block)) {
				e.setCancelled(true);
				Player player = (Player) e.getEntity();
				if (!player.isSneaking()) {
					onTriggerPressureBoost(player, block, true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void launchPlayer(PlayerInteractEvent event) {
		if (event.isCancelled() || event.getAction() != Action.PHYSICAL) {
			return;
		}
		Player player = event.getPlayer();
		if (!player.isSneaking()) {
			onTriggerPressureBoost(player, event.getClickedBlock(), true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onToggleSneak(PlayerToggleSneakEvent e) {
		Player player = e.getPlayer();
		if (e.isSneaking() == false) {
			Location loc = player.getLocation();
			Block b = player.getWorld().getBlockAt(loc);
			onTriggerPressureBoost(player, b, false);
		}
	}

	private void onTriggerPressureBoost(Player player, Block block, boolean delay) {
		if (block == null || !isValidPressureBoost(block)) {
			return;
		}
		Location location = block.getLocation();
		double power = getPressureBoostPower(block);

		Material type = block.getType();
		if (type == Material.WOOD_PLATE) {
		} else if (type == Material.STONE_PLATE) {
			jumpAfterDelay(player, location, power, delay);
		} else if (type == Material.IRON_PLATE) {
			jumpNowFacing(player, location, power);
		} else if (type == Material.GOLD_PLATE) {
			jumpNow(player, location, power);
		}
	}

	private Location getBlockMidPoint(Location pos) {
		Location mid = new Location(pos.getWorld(), pos.getBlockX() + 0.5, pos.getBlockY() - 0.5,
				pos.getBlockZ() + 0.5);
		return mid;
	}

	private void jumpAfterDelay(final Player player, final Location plateLoc, final double power, final boolean delay) {
		player.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				Location ploc = player.getLocation();
				if (ploc.getBlockX() == plateLoc.getBlockX() && ploc.getBlockY() == plateLoc.getBlockY()
						&& ploc.getBlockZ() == plateLoc.getBlockZ() && !player.isSneaking()) {
					Location blockMid = getBlockMidPoint(plateLoc);
					Vector velocity = new Vector(power * (ploc.getX() - blockMid.getX()), _yBase + _yScale * power,
							power * (ploc.getZ() - blockMid.getZ()));
					launchPlayer(player, velocity);
				}
			}
		}, delay ? 30 : 1);
	}

	private void jumpNowFacing(final Player player, final Location plateLoc, final double power) {
		player.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				Vector velocity = player.getLocation().getDirection().multiply(power / 2).setY(_yBase + _yScale * power);
				launchPlayer(player, velocity);
			}
		}, 1);
	}

	private void jumpNow(final Player player, final Location plateLoc, final double power) {
		player.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				Vector old = player.getVelocity().clone();
				Vector velocity = new Vector(power * (old.getX()), _yBase + _yScale * power, power * (old.getZ()));
				launchPlayer(player, velocity);
			}
		}, 1);
	}

	private void launchPlayer(Player player, Vector velocity) {
		// TODO Auto-generated method stub
		Location ploc = player.getLocation();
		plugin.log("Set Velocity: " + velocity.getX() + ", " + velocity.getY() + ", " + velocity.getZ());
		player.playEffect(ploc, Effect.EXPLOSION_LARGE, 0);
		player.playSound(ploc, Sound.ENTITY_ENDERDRAGON_SHOOT, 10.0F, 2.0F);
		player.setVelocity(velocity);
	}
}
