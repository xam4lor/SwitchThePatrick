package me.xam4lor.main;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ShowConfig {
	private Logger log = Logger.getLogger("Minecraft");
	
	public MainClass m = null;
	
	public ShowConfig(MainClass m) {
		this.m = m;
	}
	
	public ShowConfig() {
		this.log.info("[KTP] Default config:");
		this.log.info("-----------------------------------");
		this.log.info("episodeLength: " + this.m.getConfig().getInt("episodeLength"));
		this.log.info("weather: " + this.m.getConfig().getBoolean("weather"));
		this.log.info("map:");
		this.log.info("      size:" + this.m.getConfig().getInt("map.size"));
		this.log.info("      wall:");
		this.log.info("            height:" + this.m.getConfig().getInt("map.wall.height"));
		this.log.info("            block:" + this.m.getConfig().getInt("map.wall.block"));
		this.log.info("daylightCycle:");
		this.log.info("      do:" + this.m.getConfig().getBoolean("daylightCycle.do"));
		this.log.info("      time:" + this.m.getConfig().getInt("daylightCycle.time"));
		this.log.info("scoreboard: " + this.m.getConfig().getString("scoreboard"));
		this.log.info("kick-on-death:");
		this.log.info("      kick:" + this.m.getConfig().getBoolean("kick-on-death.kick"));
		this.log.info("      time:" + this.m.getConfig().getInt("kick-on-death.time"));
		this.log.info("naturalRegen: " + this.m.getConfig().getBoolean("naturalRegen"));
		this.log.info("-----------------------------------");
	}
	
	public ShowConfig(Player pl) {
		pl.sendMessage(ChatColor.RED + "[KTP] Default config:");
		pl.sendMessage(ChatColor.RED + "-----------------------------------");
		pl.sendMessage(ChatColor.WHITE + "episodeLength: " + this.m.getConfig().getInt("episodeLength"));
		pl.sendMessage(ChatColor.WHITE + "weather: " + this.m.getConfig().getBoolean("weather"));
		pl.sendMessage(ChatColor.WHITE + "map:");
		pl.sendMessage(ChatColor.WHITE + "      size:" + this.m.getConfig().getInt("map.size"));
		pl.sendMessage(ChatColor.WHITE + "      wall:");
		pl.sendMessage(ChatColor.WHITE + "            height:" + this.m.getConfig().getInt("map.wall.height"));
		pl.sendMessage(ChatColor.WHITE + "            block:" + this.m.getConfig().getInt("map.wall.block"));
		pl.sendMessage(ChatColor.WHITE + "daylightCycle:");
		pl.sendMessage(ChatColor.WHITE + "      do:" + this.m.getConfig().getBoolean("daylightCycle.do"));
		pl.sendMessage(ChatColor.WHITE + "      time:" + this.m.getConfig().getInt("daylightCycle.time"));
		pl.sendMessage(ChatColor.WHITE + "scoreboard: " + this.m.getConfig().getString("scoreboard"));
		pl.sendMessage(ChatColor.WHITE + "kick-on-death:");
		pl.sendMessage(ChatColor.WHITE + "      kick:" + this.m.getConfig().getBoolean("kick-on-death.kick"));
		pl.sendMessage(ChatColor.WHITE + "      time:" + this.m.getConfig().getInt("kick-on-death.time"));
		pl.sendMessage(ChatColor.WHITE + "naturalRegen: " + this.m.getConfig().getBoolean("naturalRegen"));
		pl.sendMessage(ChatColor.RED + "-----------------------------------");
	}
}
