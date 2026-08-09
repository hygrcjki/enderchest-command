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
	private static final int MENU_SIZE = 27;
	private final ServerPlayer operator;
	private final Mode mode;

	private SettingsMenu(int containerId, Inventory inventory, ServerPlayer operator, Mode mode, SimpleContainer container) {
		super(MenuType.GENERIC_9x3, containerId, inventory, container, 3);
		this.operator = operator;
		this.mode = mode;
		populate();
	}

	public static void open(ServerPlayer operator) {
		operator.openMenu(new net.minecraft.world.SimpleMenuProvider(
			(containerId, inventory, ignored) -> new SettingsMenu(containerId, inventory, operator, Mode.HOME, new SimpleContainer(MENU_SIZE)),
			Component.literal("EnderChest Utility Settings")
		));
	}

	private static void openPlayerPicker(ServerPlayer operator, Mode mode) {
		operator.openMenu(new net.minecraft.world.SimpleMenuProvider(
			(containerId, inventory, ignored) -> new SettingsMenu(containerId, inventory, operator, mode, new SimpleContainer(MENU_SIZE)),
			Component.literal(mode == Mode.ADD ? "Add to EnderChest Whitelist" : "Remove from EnderChest Whitelist")
		));
	}

	private void populate() {
		for (int slot = 0; slot < MENU_SIZE; slot++) {
			getContainer().setItem(slot, named(Items.GRAY_STAINED_GLASS_PANE, " "));
		}

		if (mode == Mode.HOME) {
			boolean enabled = CommandAccessConfig.isWhitelistEnabled();
			getContainer().setItem(10, named(enabled ? Items.LIME_DYE : Items.GRAY_DYE,
				"Whitelist: " + (enabled ? "Enabled" : "Disabled")));
			getContainer().setItem(12, named(Items.EMERALD, "Add online player to whitelist"));
			getContainer().setItem(14, named(Items.REDSTONE, "Remove online player from whitelist"));
			getContainer().setItem(16, named(Items.NAME_TAG, "Whitelisted players: " + CommandAccessConfig.whitelistSize()));
			return;
		}

		List<ServerPlayer> players = operator.level().getServer().getPlayerList().getPlayers();
		for (int slot = 0; slot < Math.min(players.size(), MENU_SIZE); slot++) {
			ServerPlayer player = players.get(slot);
			boolean whitelisted = CommandAccessConfig.isWhitelisted(player.getUUID());
			getContainer().setItem(slot, named(Items.PLAYER_HEAD, player.getGameProfile().name() + (whitelisted ? " (whitelisted)" : "")));
		}
	}

	@Override
	public void clicked(int slot, int button, ContainerInput input, Player player) {
		if (slot >= 0 && slot < MENU_SIZE) {
			if (input == ContainerInput.PICKUP && player instanceof ServerPlayer serverPlayer && serverPlayer.getUUID().equals(operator.getUUID())) {
				handleMenuClick(slot);
			}
			return;
		}
		super.clicked(slot, button, input, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return player instanceof ServerPlayer serverPlayer && serverPlayer.getUUID().equals(operator.getUUID());
	}

	private void handleMenuClick(int slot) {
		if (mode == Mode.HOME) {
			switch (slot) {
				case 10 -> {
					CommandAccessConfig.toggleWhitelist();
					populate();
					broadcastChanges();
				}
				case 12 -> openPlayerPicker(operator, Mode.ADD);
				case 14 -> openPlayerPicker(operator, Mode.REMOVE);
				default -> {
				}
			}
			return;
		}

		List<ServerPlayer> players = operator.level().getServer().getPlayerList().getPlayers();
		if (slot >= players.size()) {
			return;
		}

		ServerPlayer selected = players.get(slot);
		if (mode == Mode.ADD) {
			CommandAccessConfig.addPlayer(selected.getUUID());
		} else {
			CommandAccessConfig.removePlayer(selected.getUUID());
		}
		openPlayerPicker(operator, mode);
	}

	private static ItemStack named(Item item, String name) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
		return stack;
	}

	private enum Mode {
		HOME,
		ADD,
		REMOVE
	}
}
