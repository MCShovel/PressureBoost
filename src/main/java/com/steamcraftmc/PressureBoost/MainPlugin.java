package com.steamcraftmc.PressureBoost;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MainPlugin extends JavaPlugin {
	private WorldEvents _listener;
	public final   Logger  _logger;
	public final MainConfig Config;
	
	public MainPlugin() {
		_logger = getLogger();
		_logger.setLevel(Level.ALL);
		_logger.log(Level.CONFIG, "Plugin initializing...");
		Config = new MainConfig(this);
		Config.load();
	}
	
	public void log(String text) {
		_logger.log(Level.INFO, text);
	}
	
    @Override
    public void onEnable() {
    	_listener = new WorldEvents(this);
        getServer().getPluginManager().registerEvents(_listener, this);
		_logger.log(Level.CONFIG, "Plugin listening for events.");
    }

    @Override
    public void onDisable() {
    	HandlerList.unregisterAll(_listener);
    }

}
