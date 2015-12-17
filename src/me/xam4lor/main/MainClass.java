package me.xam4lor.main;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.xam4lor.events.Events;

public class MainClass  extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private boolean gameRunning = false;
	private HashSet<String> deadPlayers = new HashSet<String>();
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new Events(this), this);
		this.log.info(this.getPluginName() + "Plugin launched");
		setGameRunning(true);
	}
	
	@Override
	public void onDisable() {
		this.log.info(this.getPluginName() + "Plugin stopped");
	}
	
	private String getPluginName() {
		return "Switch the Patrick";
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
	
	public boolean isPlayerDead(String name) {
		return deadPlayers.contains(name);
	}

	public void setDeadPlayers(String deadPlayers) {
		this.deadPlayers.add(deadPlayers);
	}
	
	public void setLife(Player entity, int i) {
	}
}
