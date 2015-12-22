package me.xam4lor.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
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
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.xam4lor.events.Events;
import me.xam4lor.teams.SwPrompts;
import me.xam4lor.teams.SwTeams;

public class MainClass extends JavaPlugin implements ConversationAbandonedListener {
	private Logger log = Logger.getLogger("Minecraft");
	private boolean gameRunning = false;
	private HashSet<String> deadPlayers = new HashSet<String>();
	private Scoreboard sb = null;
	private String sbobjname = "STP";
	private Integer episode = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;
	private NumberFormat formatter = new DecimalFormat("00");
	private ArrayList<SwTeams> teams = new ArrayList<SwTeams>();
	private HashMap<String, ConversationFactory> cfs = new HashMap<String, ConversationFactory>();
	private LinkedList<Location> loc = new LinkedList<Location>();
	private SwPrompts swp = null;
	private Random random = null;
	public boolean domageIsOn = false;
	
	@Override
	public void onEnable() {
		
		File positions = new File("positions.txt");
		if (positions.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(positions));
				String line;
				while ((line = br.readLine()) != null) {
					String[] l = line.split(",");
					getLogger().info("Adding position "+Integer.parseInt(l[0])+","+Integer.parseInt(l[1])+" from positions.txt");
					addLocation(Integer.parseInt(l[0]), Integer.parseInt(l[1]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try { if (br != null) br.close(); }
				catch (Exception e) { e.printStackTrace(); }
			}
			
		}
		
		else {
			this.log.info("Fichier positions.txt introuvable !");
		}
		
		random = new Random();
		
		swp = new SwPrompts(this);
		getServer().getPluginManager().registerEvents(new Events(this), this);
		this.log.info(this.getPluginName() + "Plugin launched");
		
		sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = sb.registerNewObjective("Vie", "health");
		obj.setDisplayName("Vie");
		obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		
		cfs.put("teamPrompt", new ConversationFactory(this)
		.withModality(true)
		.withFirstPrompt(swp.getTNP())
		.withEscapeSequence("/cancel")
		.thatExcludesNonPlayersWithMessage("Il faut être un joueur ingame.")
		.withLocalEcho(false)
		.addConversationAbandonedListener(this));
		
		cfs.put("playerPrompt", new ConversationFactory(this)
		.withModality(true)
		.withFirstPrompt(swp.getPP())
		.withEscapeSequence("/cancel")
		.thatExcludesNonPlayersWithMessage("Il faut être un joueur ingame.")
		.withLocalEcho(false)
		.addConversationAbandonedListener(this));
		
		this.setPartyOptions();
		this.setMatchInfo();
		this.ShowConfigLog();
		
		SwitchInit();
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
		} catch (Exception e) {}
		
		Random r = new Random();
		sbobjname = "STP" + r.nextInt(10000000);
		obj = sb.registerNewObjective(sbobjname, "dummy");
		obj = sb.getObjective(sbobjname);

		obj.setDisplayName(ChatColor.BOLD + "- " + this.getScoreboardName() + " -");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY+"Episode "+ChatColor.WHITE+episode)).setScore(5);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "" + Bukkit.getServer().getOnlinePlayers().toArray().length + ChatColor.GRAY + " joueurs")).setScore(4);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE+""+getAliveTeams().size()+ChatColor.GRAY+" teams")).setScore(3);
		obj.getScore(Bukkit.getOfflinePlayer("----")).setScore(2);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE+formatter.format(this.minutesLeft)+ChatColor.GRAY+":"+ChatColor.WHITE+formatter.format(this.secondsLeft))).setScore(1);
	}
	
	private ArrayList<SwTeams> getAliveTeams() {
		ArrayList<SwTeams> aliveTeams = new ArrayList<SwTeams>();
		for (SwTeams t : teams) {
			for (Player p : t.getPlayers()) {
				if (p.isOnline() && !aliveTeams.contains(t)) aliveTeams.add(t);
			}
		}
		return aliveTeams;
	}
	
	@SuppressWarnings("unused")
	private void Switch() {
		//le tableau switchs contiendra la liste des switchs pour toutes les teams
		LinkedList<SwTeams> teams = new LinkedList<SwTeams>();
		ArrayList<Integer> id = new ArrayList<Integer>();
		ArrayList<SwTeams> switchs = new ArrayList<SwTeams>();
		int nb = 0;
		
		for (SwTeams t : teams) {
			id.add(nb);
			nb++;
		}
		
		int teamLoop2 = 0;
		for(Integer teamLoop : id) {
			switchs.add(teams.get(teamLoop));
			switchs.add(teams.get(teamLoop++));
			teamLoop2 = teamLoop;
		}
		switchs.remove(teamLoop2++);
		switchs.add(teams.get(0));
		//--------
		
		//on fait les switchs
		SwTeams team_a = null;
		SwTeams team_b = null;
		int playerLoop = 0;
		int playerLoop2 = 0;
		for(SwTeams switchTeam : switchs) {
			if(playerLoop == 0) {
				team_a = switchs.get(playerLoop2);
			}
			else if(playerLoop == 1) {
				team_b = switchs.get(playerLoop2);
				playerLoop = 0;
			}
			
			SwitchPlayers(randomPlayer(team_a), randomPlayer(team_b));
			
			playerLoop++;
			playerLoop2++;
		}
	}
	
	private Player randomPlayer(SwTeams team_x) {
		ArrayList<Player> players = team_x.getPlayers();
		int randomPlayer = 0;
		
		return players.get(randomPlayer);
	}

	private void SwitchPlayers(Player a, Player b) {
		Location coord_a = a.getLocation();
		Location coord_b = a.getLocation();
		
		a.getLocation().setX(coord_a.getX());
		a.getLocation().setY(coord_a.getY());
		a.getLocation().setZ(coord_a.getZ());
		
		b.getLocation().setX(coord_b.getX());
		b.getLocation().setY(coord_b.getY());
		b.getLocation().setZ(coord_b.getZ());
	}
	
	private int switchs[][];
	
	public void SwitchInit() {
		//initialisation des switchs
		int switchs[][] = SwitchList();
		if(switchs != null) {
			this.switchs = switchs;
			this.log.info("Configuration des switchs terminée avec succès !");
		}
		else {
			this.log.info("Erreur dans la configuration des switchs.");
		}
	}
	
	public void SwitchTest(Integer episode, Integer minutesLeft, Integer secondsLeft) {
		for(int i = 0; i < switchs.length; i++) {
			if(episode == switchs[i][0]) {
				if(minutesLeft == switchs[i][1] && secondsLeft == 0) {
					Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Switch en cours !");
					Switch();
				}
			}
		}
	}
	
	private int[][] SwitchList() {
		try {
			VoidTbl tbl = new VoidTbl();
			int switchNumber = this.getConfig().getInt("switch.number");
			int switchs[][] = tbl.getTableau();
			
			for(int i = 0; i < switchNumber; i++) {
				switchs[i][0] = this.getConfig().getInt("switch." + (i + 1) + ".episode");
				switchs[i][1] = this.getConfig().getInt("switch." + (i + 1) + ".time");
			}
			return switchs;
		}
		catch(NullPointerException e) {
			e.printStackTrace();
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "Erreur lors du lancement de la partie, le nombre de switchs est trop élevé. Regarder la console pour plus de détails.");
			return null;
		}
	}
	
	class updateTimer extends TimerTask {
	    public void run() {
	    	if(isGameRunning()) {
				setMatchInfo();
				secondsLeft--;
				if(secondsLeft == -1) {
					minutesLeft--;
					secondsLeft = 59;
				}
				if(minutesLeft == -1) {
					minutesLeft = getEpisodeLength();
					secondsLeft = 0;
					Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "-------- Fin episode " + episode + " --------");
					shiftEpisode();
				}
				SwitchTest(episode, minutesLeft, secondsLeft);
			}
	    }
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
				pl.sendMessage("Usage : /sw <start|generateWalls|team|addspawn>");
				return true;
			}
			if (a[0].equalsIgnoreCase("start")) {
				if (teams.size() == 0) {
					for (Player p : getServer().getOnlinePlayers()) {
						SwTeams swt = new SwTeams(p.getName(), p.getName(), ChatColor.WHITE, this);
						swt.addPlayer(p);
						teams.add(swt);
					}
				}
				if (loc.size() < teams.size()) {
					s.sendMessage(ChatColor.RED+"Pas assez de positions de TP");
					return true;
				}
				else {
					LinkedList<Location> unusedTP = loc;
					for (final SwTeams t : teams) {
						final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
						t.teleportTo(lo);
						for (Player p : t.getPlayers()) {
							p.setGameMode(GameMode.SURVIVAL);
							p.setHealth(20);
							p.setFoodLevel(20);
							p.setExhaustion(5F);
							p.getInventory().clear();
							p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), 
									new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
							p.setExp(0L+0F);
							p.setLevel(0);
							p.closeInventory();
							p.getActivePotionEffects().clear();
							setLife(p, 20);
						}
						
						unusedTP.remove(lo);
					}
					
					Bukkit.getScheduler().runTaskLater(this, new BukkitRunnable() {
						@Override
						public void run() {
							domageIsOn = true;
						}
					}, 600L);
					
					World w = Bukkit.getWorld("world");
					w.setTime(getConfig().getLong("daylightCycle.time"));
					w.setDifficulty(Difficulty.HARD);
					
					this.episode = 1;
					this.minutesLeft = getEpisodeLength();
					this.secondsLeft = 0;
					
					Timer timer = new Timer();
					timer.schedule(new updateTimer(), 0, 1000);
					
					Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "--- GO ---");
					this.ShowConfigPlayer(pl);
					this.setGameRunning(true);
					return true;
				}
			}
			
			else if (a[0].equalsIgnoreCase("team")) {
				Inventory iv = this.getServer().createInventory(pl, 54, "- Teams -");
				Integer slot = 0;
				ItemStack is = null;
				
				for (SwTeams t : teams) {
					is = new ItemStack(Material.BEACON, t.getPlayers().size());
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(t.getChatColor() + t.getDisplayName());
					ArrayList<String> lore = new ArrayList<String>();
					for (Player p : t.getPlayers()) {
						lore.add("- " + p.getDisplayName());
					}
					im.setLore(lore);
					is.setItemMeta(im);
					iv.setItem(slot, is);
					slot++;
				}
				
				ItemStack is2 = new ItemStack(Material.DIAMOND);
				ItemMeta im2 = is2.getItemMeta();
				im2.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Créer une team");
				is2.setItemMeta(im2);
				iv.setItem(53, is2);
				
				pl.openInventory(iv);
				return true;
			}
			
			else if (a[0].equalsIgnoreCase("addspawn")) {
				addLocation(pl.getLocation().getBlockX(), pl.getLocation().getBlockZ());
				pl.sendMessage(ChatColor.DARK_GRAY + "Position ajoutée: " + ChatColor.GRAY + pl.getLocation().getBlockX() + "," + pl.getLocation().getBlockZ());
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
			else {
				pl.sendMessage("Usage : /sw <start|generateWalls|team|addspawn>");
				return true;
			}
		}
		return false;
	}

	public void ShowConfigLog() {
		this.log.info("[Switch-The-Patrick] Default config:");
		this.log.info("-----------------------------------");
		this.log.info("episodeLength: " + this.getConfig().getInt("episodeLength"));
		this.log.info("weather: " + this.getConfig().getBoolean("weather"));
		this.log.info("map:");
		this.log.info("      size:" + this.getConfig().getInt("map.size"));
		this.log.info("      wall:");
		this.log.info("            height:" + this.getConfig().getInt("map.wall.height"));
		this.log.info("            block:" + this.getConfig().getInt("map.wall.block"));
		this.log.info("daylightCycle:");
		this.log.info("      do:" + this.getConfig().getBoolean("daylightCycle.do"));
		this.log.info("      time:" + this.getConfig().getInt("daylightCycle.time"));
		this.log.info("scoreboard: " + this.getConfig().getString("scoreboard"));
		this.log.info("kick-on-death:");
		this.log.info("      kick:" + this.getConfig().getBoolean("kick-on-death.kick"));
		this.log.info("      time:" + this.getConfig().getInt("kick-on-death.time"));
		this.log.info("naturalRegen: " + this.getConfig().getBoolean("naturalRegen"));
		this.log.info("-----------------------------------");
	}
	
	public void ShowConfigPlayer(Player pl) {
		pl.sendMessage(ChatColor.RED + "[Switch-The-Patrick] Default config:");
		pl.sendMessage(ChatColor.RED + "-----------------------------------");
		pl.sendMessage(ChatColor.WHITE + "episodeLength: " + ChatColor.GRAY + this.getConfig().getInt("episodeLength"));
		pl.sendMessage(ChatColor.WHITE + "weather: " + ChatColor.GRAY + this.getConfig().getBoolean("weather"));
		pl.sendMessage(ChatColor.WHITE + "map:");
		pl.sendMessage(ChatColor.WHITE + "      size: " + ChatColor.GRAY + this.getConfig().getInt("map.size"));
		pl.sendMessage(ChatColor.WHITE + "      wall:");
		pl.sendMessage(ChatColor.WHITE + "            height: " + ChatColor.GRAY + this.getConfig().getInt("map.wall.height"));
		pl.sendMessage(ChatColor.WHITE + "            block: " + ChatColor.GRAY + this.getConfig().getInt("map.wall.block"));
		pl.sendMessage(ChatColor.WHITE + "daylightCycle:");
		pl.sendMessage(ChatColor.WHITE + "      do: " + ChatColor.GRAY + this.getConfig().getBoolean("daylightCycle.do"));
		pl.sendMessage(ChatColor.WHITE + "      time: " + ChatColor.GRAY + this.getConfig().getInt("daylightCycle.time"));
		pl.sendMessage(ChatColor.WHITE + "scoreboard: " + ChatColor.GRAY + this.getConfig().getString("scoreboard"));
		pl.sendMessage(ChatColor.WHITE + "kick-on-death: ");
		pl.sendMessage(ChatColor.WHITE + "      kick: " + ChatColor.GRAY + this.getConfig().getBoolean("kick-on-death.kick"));
		pl.sendMessage(ChatColor.WHITE + "      time: " + ChatColor.GRAY + this.getConfig().getInt("kick-on-death.time"));
		pl.sendMessage(ChatColor.WHITE + "naturalRegen: " + ChatColor.GRAY + this.getConfig().getBoolean("naturalRegen"));
		pl.sendMessage(ChatColor.RED + "-----------------------------------");
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
	
	public ConversationFactory getConversationFactory(String string) {
		if (cfs.containsKey(string)) return cfs.get(string);
		return null;
	}
	
	public Scoreboard getScoreboard() {
		return sb;
	}
	
	public void addLocation(int x, int z) {
		loc.add(new Location(getServer().getWorlds().get(0), x, getServer().getWorlds().get(0).getHighestBlockYAt(x,z)+120, z));
	}
	
	public boolean createTeam(String name, ChatColor color) {
		if (teams.size() <= 50) {
			teams.add(new SwTeams(name, name, color, this));
			return true;
		}
		return false;
	}
	
	public SwTeams getTeam(String name) {
		for(SwTeams t : teams) {
			if (t.getName().equalsIgnoreCase(name)) return t;
		}
		return null;
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
		if (!abandonedEvent.gracefulExit()) {
			abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.RED + "Abandonné par " + abandonedEvent.getCanceller().getClass().getName());
		}
	}
}
