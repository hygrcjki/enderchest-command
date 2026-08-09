package com.enderchestcommand;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CommandAccessConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("enderchest-utility.json");
	private static Data data = new Data();

	private CommandAccessConfig() {
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				Data loaded = GSON.fromJson(Files.readString(FILE), Data.class);
				if (loaded != null) {
					data = loaded;
				}
			}
			data.normalise();
			save();
		} catch (IOException exception) {
			EnderChestCommandMod.LOGGER.error("Could not load command access settings", exception);
		}
	}

	public static boolean canUse(ServerPlayer player) {
		return !data.whitelistEnabled || data.whitelist.contains(player.getUUID().toString());
	}

	public static boolean isWhitelistEnabled() {
		return data.whitelistEnabled;
	}

	public static void toggleWhitelist() {
		data.whitelistEnabled = !data.whitelistEnabled;
		save();
	}

	public static boolean isWhitelisted(UUID playerId) {
		return data.whitelist.contains(playerId.toString());
	}

	public static void togglePlayer(UUID playerId) {
		String id = playerId.toString();
		if (!data.whitelist.add(id)) {
			data.whitelist.remove(id);
		}
		save();
	}

	public static void addPlayer(UUID playerId) {
		data.whitelist.add(playerId.toString());
		save();
	}

	public static void removePlayer(UUID playerId) {
		data.whitelist.remove(playerId.toString());
		save();
	}

	public static int whitelistSize() {
		return data.whitelist.size();
	}

	private static void save() {
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(data));
		} catch (IOException exception) {
			EnderChestCommandMod.LOGGER.error("Could not save command access settings", exception);
		}
	}

	private static final class Data {
		private boolean whitelistEnabled = false;
		private Set<String> whitelist = new HashSet<>();

		private void normalise() {
			if (whitelist == null) {
				whitelist = new HashSet<>();
			}
		}
	}
}
