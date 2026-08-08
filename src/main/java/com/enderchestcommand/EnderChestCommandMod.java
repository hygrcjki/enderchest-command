package com.enderchestcommand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnderChestCommandMod implements ModInitializer {
	public static final String MOD_ID = "enderchestcommand";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			Commands.literal("enderchest")
				.executes(context -> openEnderChest(context.getSource().getPlayerOrException()))
		));

		LOGGER.info("Registered /enderchest command");
	}

	private static int openEnderChest(ServerPlayer player) {
		player.openMenu(new SimpleMenuProvider(
			(syncId, inventory, ignored) -> ChestMenu.threeRows(syncId, inventory, player.getEnderChestInventory()),
			Component.translatable("container.enderchest")
		));
		return 1;
	}
}
