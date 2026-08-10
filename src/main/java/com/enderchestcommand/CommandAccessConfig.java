package com.enderchestcommand;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("null")
public final class CommandAccessConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("enderchest-utility.json");
	private static Data data = new Data();

	private CommandAccessConfig() {
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				String raw = Files.readString(FILE);
				JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
				Data loaded = root.has("whitelist") && root.get("whitelist").isJsonArray() ? migrateLegacyData(root) : GSON.fromJson(root, Data.class);
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

	private static Data migrateLegacyData(JsonObject root) {
		Data migrated = new Data();
		if (root.has("whitelistEnabled")) migrated.whitelistEnabled = root.get("whitelistEnabled").getAsBoolean();
		for (JsonElement id : root.getAsJsonArray("whitelist")) migrated.whitelist.put(id.getAsString(), id.getAsString());
		return migrated;
	}

	public static boolean canUse(ServerPlayer player) {
		recordPlayer(player);
		return !data.whitelistEnabled || data.whitelist.containsKey(player.getUUID().toString());
	}

	public static boolean isWhitelistEnabled() {
		return data.whitelistEnabled;
	}

	public static void toggleWhitelist() {
		data.whitelistEnabled = !data.whitelistEnabled;
		save();
	}

	public static boolean isWhitelisted(UUID playerId) {
		return data.whitelist.containsKey(playerId.toString());
	}

	public static void togglePlayer(UUID playerId) {
		String id = playerId.toString();
		if (!data.whitelist.containsKey(id)) {
			data.whitelist.put(id, id);
		} else {
			data.whitelist.remove(id);
		}
		save();
	}

	public static void addPlayer(UUID playerId, String name) {
		data.knownPlayers.put(playerId.toString(), name);
		data.whitelist.put(playerId.toString(), name);
		save();
	}

	public static void removePlayer(UUID playerId) {
		data.whitelist.remove(playerId.toString());
		save();
	}

	public static int whitelistSize() {
		return data.whitelist.size();
	}

	public static void recordPlayer(ServerPlayer player) {
		String id = player.getUUID().toString();
		String name = player.getGameProfile().name();
		if (!name.equals(data.knownPlayers.put(id, name))) {
			save();
		}
	}

	public static List<PlayerEntry> whitelistedPlayers() {
		return entries(data.whitelist);
	}

	public static List<PlayerEntry> knownPlayers() {
		return entries(data.knownPlayers);
	}

	private static List<PlayerEntry> entries(Map<String, String> players) {
		return players.entrySet().stream().map(entry -> new PlayerEntry(UUID.fromString(entry.getKey()), entry.getValue())).sorted(java.util.Comparator.comparing(PlayerEntry::name, String.CASE_INSENSITIVE_ORDER)).toList();
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
		private Map<String, String> whitelist = new HashMap<>();
		private Map<String, String> knownPlayers = new HashMap<>();

		private void normalise() {
			if (whitelist == null) {
				whitelist = new HashMap<>();
			}
			if (knownPlayers == null) {
				knownPlayers = new HashMap<>();
			}
		}
	}

	public record PlayerEntry(UUID id, String name) {
	}
}
