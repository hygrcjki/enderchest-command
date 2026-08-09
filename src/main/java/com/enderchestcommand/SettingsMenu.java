package com.enderchestcommand;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SettingsMenu extends ChestMenu {
	private static final int SIZE = 27;
	private final ServerPlayer operator;
	private final Mode mode;

	private SettingsMenu(int id, Inventory inventory, ServerPlayer operator, Mode mode, SimpleContainer container) {
		super(MenuType.GENERIC_9x3, id, inventory, container, 3);
		this.operator = operator;
		this.mode = mode;
		populate();
	}

	public static void open(ServerPlayer operator) {
		open(operator, Mode.HOME);
	}

	private static void open(ServerPlayer operator, Mode mode) {
		String title = switch (mode) {
			case HOME -> "EnderChest Utility Settings";
			case WHITELIST_PLAYERS -> "Whitelisted Players";
			case ADD_CHOICE -> "Add Player to Whitelist";
			case REMOVE_CHOICE -> "Remove Player from Whitelist";
			case ADD_ONLINE, REMOVE_ONLINE -> "Online Players";
			case ADD_OFFLINE, REMOVE_OFFLINE -> "Offline Players";
		};
		operator.openMenu(new net.minecraft.world.SimpleMenuProvider(
			(id, inventory, ignored) -> new SettingsMenu(id, inventory, operator, mode, new SimpleContainer(SIZE)),
			Component.literal(title)
		));
	}

	private void populate() {
		for (ServerPlayer player : onlinePlayers()) CommandAccessConfig.recordPlayer(player);
		for (int slot = 0; slot < SIZE; slot++) getContainer().setItem(slot, named(Items.GRAY_STAINED_GLASS_PANE, " "));
		if (mode == Mode.HOME) {
			getContainer().setItem(10, named(CommandAccessConfig.isWhitelistEnabled() ? Items.LIME_DYE : Items.GRAY_DYE, "Whitelist: " + (CommandAccessConfig.isWhitelistEnabled() ? "Enabled" : "Disabled")));
			getContainer().setItem(12, named(Items.EMERALD, "Add player to whitelist"));
			getContainer().setItem(14, named(Items.REDSTONE, "Remove player from whitelist"));
			getContainer().setItem(16, named(Items.NAME_TAG, "View whitelisted players (" + CommandAccessConfig.whitelistSize() + ")"));
			return;
		}
		if (mode == Mode.WHITELIST_PLAYERS) {
			List<CommandAccessConfig.PlayerEntry> players = CommandAccessConfig.whitelistedPlayers();
			for (int slot = 0; slot < SIZE && slot < players.size(); slot++) getContainer().setItem(slot, named(Items.PLAYER_HEAD, players.get(slot).name()));
			return;
		}
		if (mode == Mode.ADD_CHOICE || mode == Mode.REMOVE_CHOICE) {
			getContainer().setItem(11, named(Items.LIME_CONCRETE, "Select online player"));
			getContainer().setItem(15, named(Items.LIGHT_BLUE_CONCRETE, "Select offline player"));
			return;
		}
		List<CommandAccessConfig.PlayerEntry> players = selectablePlayers();
		for (int slot = 0; slot < SIZE && slot < players.size(); slot++) getContainer().setItem(slot, named(Items.PLAYER_HEAD, players.get(slot).name()));
	}

	@Override public void clicked(int slot, int button, ContainerInput input, Player player) {
		if (slot >= 0 && slot < SIZE) {
			if (input == ContainerInput.PICKUP && player instanceof ServerPlayer serverPlayer && serverPlayer.getUUID().equals(operator.getUUID())) click(slot);
			return;
		}
		super.clicked(slot, button, input, player);
	}
	@Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
	@Override public boolean stillValid(Player player) { return player instanceof ServerPlayer serverPlayer && serverPlayer.getUUID().equals(operator.getUUID()); }

	private void click(int slot) {
		if (mode == Mode.HOME) {
			if (slot == 10) { CommandAccessConfig.toggleWhitelist(); populate(); broadcastChanges(); }
			else if (slot == 12) open(operator, Mode.ADD_CHOICE);
			else if (slot == 14) open(operator, Mode.REMOVE_CHOICE);
			else if (slot == 16) open(operator, Mode.WHITELIST_PLAYERS);
			return;
		}
		if (mode == Mode.WHITELIST_PLAYERS) { viewWhitelisted(slot); return; }
		if (mode == Mode.ADD_CHOICE) { if (slot == 11) open(operator, Mode.ADD_ONLINE); else if (slot == 15) open(operator, Mode.ADD_OFFLINE); return; }
		if (mode == Mode.REMOVE_CHOICE) { if (slot == 11) open(operator, Mode.REMOVE_ONLINE); else if (slot == 15) open(operator, Mode.REMOVE_OFFLINE); return; }
		List<CommandAccessConfig.PlayerEntry> players = selectablePlayers();
		if (slot >= players.size()) return;
		CommandAccessConfig.PlayerEntry entry = players.get(slot);
		if (mode == Mode.ADD_ONLINE || mode == Mode.ADD_OFFLINE) CommandAccessConfig.addPlayer(entry.id(), entry.name()); else CommandAccessConfig.removePlayer(entry.id());
		open(operator, mode);
	}

	private void viewWhitelisted(int index) {
		List<CommandAccessConfig.PlayerEntry> players = CommandAccessConfig.whitelistedPlayers();
		if (index >= players.size()) return;
		CommandAccessConfig.PlayerEntry entry = players.get(index);
		ServerPlayer target = onlinePlayers().stream().filter(player -> player.getUUID().equals(entry.id())).findFirst().orElse(null);
		if (target == null) operator.sendSystemMessage(Component.literal(entry.name() + " must be online to view their Ender Chest."));
		else operator.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inventory, ignored) -> new EnderChestViewMenu(id, inventory, target.getEnderChestInventory()), Component.literal(entry.name() + "'s Ender Chest")));
	}

	private List<CommandAccessConfig.PlayerEntry> selectablePlayers() {
		boolean online = mode == Mode.ADD_ONLINE || mode == Mode.REMOVE_ONLINE;
		List<CommandAccessConfig.PlayerEntry> players = onlinePlayers().stream().map(player -> new CommandAccessConfig.PlayerEntry(player.getUUID(), player.getGameProfile().name())).toList();
		if (!online) players = CommandAccessConfig.knownPlayers().stream().filter(entry -> onlinePlayers().stream().noneMatch(player -> player.getUUID().equals(entry.id()))).toList();
		boolean adding = mode == Mode.ADD_ONLINE || mode == Mode.ADD_OFFLINE;
		return players.stream().filter(entry -> adding != CommandAccessConfig.isWhitelisted(entry.id())).toList();
	}
	private List<ServerPlayer> onlinePlayers() { return operator.level().getServer().getPlayerList().getPlayers(); }
	private static ItemStack named(Item item, String name) { ItemStack stack = new ItemStack(item); stack.set(DataComponents.CUSTOM_NAME, Component.literal(name)); return stack; }
	private enum Mode { HOME, WHITELIST_PLAYERS, ADD_CHOICE, REMOVE_CHOICE, ADD_ONLINE, ADD_OFFLINE, REMOVE_ONLINE, REMOVE_OFFLINE }
}
