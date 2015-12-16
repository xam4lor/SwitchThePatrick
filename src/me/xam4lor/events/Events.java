package me.xam4lor.events;

import org.bukkit.event.Listener;

import me.xam4lor.main.MainClass;

public class Events implements Listener {
	public MainClass m = null;
	
	public Events(MainClass m) {
		this.m = m;
	}
}
