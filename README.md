# Ender Chest Utility

A server-side Fabric utility mod for Minecraft 26.1.2. Open your personal Ender Chest with a command and manage access through an operator whitelist GUI.

## Commands

- `/enderchest` opens your personal Ender Chest.
- `/enderchest settings` opens an operator-only settings menu.

## Operator settings

The operator menu lets staff:

- Enable or disable the `/enderchest` whitelist.
- Add or remove online players.
- Add or remove offline players the server has seen before.
- View the whitelisted-player list.
- Open a read-only view of an online whitelisted player's Ender Chest.

Settings persist in `config/enderchest-utility.json` on the server.

## Client requirement

This is a server-side mod. Players do not need to install it on their client.

## Install

Install Fabric Loader 0.19.3 or newer and Fabric API on the server, then put the built mod JAR in the server's `mods` folder.

## Build

Run `./gradlew build`. The distributable JAR is created in `build/libs`.

## Downloads

**Github:** Navigate to the `build/libs` file and look for the .jar file.

**Modrinth:** https://modrinth.com/mod/enderchest-command
