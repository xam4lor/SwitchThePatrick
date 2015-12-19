package me.xam4lor.main;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.xam4lor.events.Events;

public class MainClass extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private boolean gameRunning = false;
	private HashSet<String> deadPlayers = new HashSet<String>();
	private Scoreboard sb = null;
	private String sbobjname = "STP";
	private Integer episode = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;
	private NumberFormat formatter = new DecimalFormat("00");
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new Events(this), this);
		new SwitchPlayers(this);
		new ShowConfig(this);
		this.log.info(this.getPluginName() + "Plugin launched");
		
		sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = sb.registerNewObjective("Vie", "health");
		obj.setDisplayName("Vie");
		obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		
		this.setPartyOptions();
		this.setMatchInfo();
		/*new SwitchPlayers();
		new ShowConfig();*/
	}
	
	@Override
	public void onDisable() {
		this.log.info(this.getPluginName() + "Plugin stopped");
	}
	
	private void setPartyOptions() {
		if(this.getConfig().getBoolean("daylightCycle.do")) {
			getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "true");
			getServer().getWorlds().get(0).setTime(6000L);
		}
		
		else {
			getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
			long time = this.getConfig().getInt("daylightCycle.time");
			getServer().getWorlds().get(0).setTime(time);
		}
		
		getServer().getWorlds().get(0).setStorm(false);
		getServer().getWorlds().get(0).setDifficulty(Difficulty.HARD);
	}
	
	@SuppressWarnings("deprecation")
	public void setMatchInfo() {
		Objective obj = null;
		try {
			obj = sb.getObjective(sbobjname);
			obj.setDisplaySlot(null);
			obj.unregister();
		} catch (Exception e) {
		}
		
		Random r = new Random();
		sbobjname = "STP" + r.nextInt(10000000);
		obj = sb.registerNewObjective(sbobjname, "dummy");
		obj = sb.getObjective(sbobjname);

		obj.setDisplayName(ChatColor.BOLD + "- " + this.getScoreboardName() + " -");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Episode: " + ChatColor.WHITE + episode)).setScore(4);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "" + getPlayerLength() + ChatColor.GRAY + " joueurs")).setScore(3);
		obj.getScore(Bukkit.getOfflinePlayer("---------")).setScore(2);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + formatter.format(this.minutesLeft) + ChatColor.GRAY + ":" + ChatColor.WHITE + formatter.format(this.secondsLeft))).setScore(1);
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender s, Command c, String l, String[] a) {
		if (c.getName().equalsIgnoreCase("sw")) {
			if (!(s instanceof Player)) {
				s.sendMessage(ChatColor.RED + "Vous devez être un joueur");
				return true;
			}
			Player pl = (Player)s;
			if (!pl.isOp()) {
				pl.sendMessage(ChatColor.RED + "Vous n'êtes pas un opérateur !");
				return true;
			}
			if (a.length == 0) {
				pl.sendMessage("Usage : /sw <start|generateWalls>");
				return true;
			}
			if (a[0].equalsIgnoreCase("start")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.setGameMode(GameMode.SURVIVAL);
					p.setHealth(20);
					p.setFoodLevel(20);
					p.setExhaustion(5F);
					p.getInventory().clear();
					p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
					p.setExp(0L + 0F);
					p.setLevel(0);
					p.closeInventory();
					p.getActivePotionEffects().clear();
					setLife(p, 20);
				}
				
				World w = Bukkit.getWorld("world");
				w.setTime(getConfig().getLong("daylightCycle.time"));
				w.setDifficulty(Difficulty.HARD);
				
				this.episode = 1;
				this.minutesLeft = getEpisodeLength();
				this.secondsLeft = 0;
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
					@Override
					public void run() {
						if(isGameRunning()) {
							setMatchInfo();
							secondsLeft--;
							if (secondsLeft == -1) {
								minutesLeft--;
								secondsLeft = 59;
							}
							if (minutesLeft == -1) {
								minutesLeft = getEpisodeLength();
								secondsLeft = 0;
								Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "-------- Fin episode " + episode + " --------");
								shiftEpisode();
							}
							//new SwitchPlayers(episode, minutesLeft, secondsLeft);
						}
					} 
				}, 20L, 20L);
				
				Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "--- GO ---");
				//new ShowConfig(pl);
				this.setGameRunning(true);
				return true;
			}
			
			else if (a[0].equalsIgnoreCase("generateWalls")) {
				pl.sendMessage(ChatColor.GRAY + "Génération en cours...");
				try {
					Integer halfMapSize = (int) Math.floor(this.getConfig().getInt("map.size") / 2);
					Integer wallHeight = this.getConfig().getInt("map.wall.height");
					Material wallBlock = Material.getMaterial(this.getConfig().getInt("map.wall.block"));
					World w = pl.getWorld();
					
					Location spawn = w.getSpawnLocation();
					Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();
					
					spawn = w.getSpawnLocation();
					Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();
					
					spawn = w.getSpawnLocation();
					Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();
					
					spawn = w.getSpawnLocation();
					Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();
					
					for (Integer x = limitXInf; x <= limitXSup; x++) {
						w.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
						w.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);
						for (Integer y = 2; y <= wallHeight; y++) {
							w.getBlockAt(x, y, limitZInf).setType(wallBlock);
							w.getBlockAt(x, y, limitZSup).setType(wallBlock);
						}
					} 
					
					for (Integer z = limitZInf; z <= limitZSup; z++) {
						w.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
						w.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);
						for (Integer y = 2; y <= wallHeight; y++) {
							w.getBlockAt(limitXInf, y, z).setType(wallBlock);
							w.getBlockAt(limitXSup, y, z).setType(wallBlock);
						}
					} 
				} catch (Exception e) {
					e.printStackTrace();
					pl.sendMessage(ChatColor.RED + "Echec génération. Voir console pour détails.");
					return true;
				}
				pl.sendMessage(ChatColor.GRAY + "Génération terminée.");
				return true;
			}
		}
		return false;
	}
	
	private String getScoreboardName() {
		return this.getConfig().getString("scoreboard");
	}
	
	public void shiftEpisode() {
		this.episode++;
	}
	
	public String getPluginName() {
		return "Switch The Patrick";
	}
	
	public boolean isGameRunning() {
		return gameRunning;
	}
	
	public void setGameRunning(boolean gameRunning) {
		this.gameRunning = gameRunning;
	}

	public HashSet<String> getDeadPlayers() {
		return deadPlayers;
	}
	
	public int getPlayerLength() {
		Collection<? extends Player> ps = Bukkit.getServer().getOnlinePlayers();
		int plLength = 0;
		
		for (Player pp : ps) {
			if(pp.getGameMode() == GameMode.SURVIVAL) {
				plLength++;
			}
		}
		return plLength;
	}
	
	public boolean isPlayerDead(String name) {
		return deadPlayers.contains(name);
	}

	public void setDeadPlayers(String deadPlayers) {
		this.deadPlayers.add(deadPlayers);
	}
	
	public Integer getEpisodeLength() {
		return this.getConfig().getInt("episodeLength");
	}
	
	@SuppressWarnings("deprecation")
	public void setLife(Player entity, int i) {
		entity.setScoreboard(sb);
		sb.getObjective("Vie").getScore(entity).setScore(i);
	}
	
	@SuppressWarnings("deprecation")
	public void updatePlayerListName(Player p) {
		p.setScoreboard(sb);
		Integer he = (int) Math.round(p.getHealth());
		sb.getObjective("Vie").getScore(p).setScore(he);
	}
	
	@SuppressWarnings("deprecation")
	public void addToScoreboard(Player player) {
		player.setScoreboard(sb);
		sb.getObjective("Vie").getScore(player).setScore(0);
		this.updatePlayerListName(player);
	}
}
