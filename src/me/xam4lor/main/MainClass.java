package me.xam4lor.main;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import me.xam4lor.events.Events;

public class MainClass  extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new Events(this), this);
		this.log.info(this.getPluginName() + "Plugin launched");
	}
	
	@Override
	public void onDisable() {
		this.log.info(this.getPluginName() + "Plugin stopped");
	}

	private String getPluginName() {
		return "Switch the Patrick";
	}
}
