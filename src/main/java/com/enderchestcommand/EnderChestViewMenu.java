package com.enderchestcommand;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public final class EnderChestViewMenu extends ChestMenu {
	public EnderChestViewMenu(int id, Inventory inventory, Container enderChest) {
		super(MenuType.GENERIC_9x3, id, inventory, enderChest, 3);
	}

	@Override
	public void clicked(int slot, int button, ContainerInput input, Player player) {
		if (slot >= 0 && slot < 27) return;
		super.clicked(slot, button, input, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}
}
