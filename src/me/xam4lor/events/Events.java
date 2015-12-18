package me.xam4lor.events;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.xam4lor.main.MainClass;

public class Events implements Listener {
	public MainClass m = null;
	
	public Events(MainClass m) {
		this.m = m;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent ev) {
		Location l = ev.getEntity().getLocation();
		Collection<? extends Player> ps = Bukkit.getServer().getOnlinePlayers();
		
		for (Player pp : ps) {
			pp.playSound(pp.getLocation(), Sound.WITHER_SPAWN, 1F, 1F);
		}
		
		this.m.setDeadPlayers(ev.getEntity().getName());
		
		Bukkit.getScheduler().runTaskLater(this.m, new BukkitRunnable() {
			@Override
			public void run() {
				m.setLife((Player) ev.getEntity(), 0);
			}
		}, 1L);
		
		if (this.m.getConfig().getBoolean("kick-on-death.kick", true)) {
			Bukkit.getScheduler().runTaskLater(this.m, new BukkitRunnable() {
				@Override
				public void run() {
					ev.getEntity().kickPlayer("Good Game !");
				}
			}, 20L * this.m.getConfig().getInt("kick-on-death.time", 30));
		}
		
		try { 
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(((Player)ev.getEntity()).getName());
			skullMeta.setDisplayName(ChatColor.BOLD + ((Player) ev.getEntity()).getName());
			skull.setItemMeta(skullMeta);
			l.getWorld().dropItem(l, skull);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		ev.setDeathMessage(ChatColor.YELLOW + ev.getEntity().getName() + ChatColor.GREEN + " est mort.");
		ev.getEntity().setGameMode(GameMode.SPECTATOR);
		
		if(m.getPlayerLength() == 1) {
			for (Player pp : ps) {
				if(pp.getGameMode() == GameMode.SURVIVAL) {
					Bukkit.broadcastMessage(ChatColor.YELLOW + ev.getEntity().getName() + ChatColor.GREEN + " gagne la partie !!!");
					m.setGameRunning(false);
					pp.setGameMode(GameMode.SPECTATOR);
					Bukkit.broadcastMessage(ChatColor.RED + "Si vous voulez recommencer une partie, pour plus de sécurité, veuillez relancer votre serveur. Merci !");
				}
			}
		}
		else if(m.getPlayerLength() == 0) {
			for (Player pp : ps) {
				m.setGameRunning(false);
				pp.setGameMode(GameMode.SPECTATOR);
			}
			
			Bukkit.broadcastMessage(ChatColor.YELLOW + "CrossKill, bravo à tous !!!");
			Bukkit.broadcastMessage(ChatColor.RED + "Si vous voulez recommencer une partie, pour plus de sécurité, veuillez relancer votre serveur. Merci !");
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent ev) {
		if (!this.m.isGameRunning()) {
			ev.getPlayer().setGameMode(GameMode.CREATIVE);
			Location l = ev.getPlayer().getWorld().getSpawnLocation();
			ev.getPlayer().teleport(l.add(0,1,0));
		}
		m.addToScoreboard(ev.getPlayer());
		Bukkit.getScheduler().runTaskLater(this.m, new BukkitRunnable() {
			
			@Override
			public void run() {
				m.updatePlayerListName(ev.getPlayer());
			}
		}, 1L);
	}
	
	@EventHandler
	public void onBlockBreakEvent(final BlockBreakEvent ev) {
		if (!this.m.isGameRunning()) {
			ev.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlaceEvent(final BlockPlaceEvent ev) {
		if (!this.m.isGameRunning()) {
			ev.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (!this.m.isGameRunning()) {
				ev.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(final EntityRegainHealthEvent ev) {
		if (ev.getRegainReason() == RegainReason.SATIATED && !m.getConfig().getBoolean("naturalRegen")) {
			ev.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent ev) {
		if (!m.getConfig().getBoolean("weather")) {
			ev.setCancelled(true);
		}
	}
}
