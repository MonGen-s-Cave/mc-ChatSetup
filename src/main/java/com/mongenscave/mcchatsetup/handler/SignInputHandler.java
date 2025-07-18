package com.mongenscave.mcchatsetup.handler;

import com.mongenscave.mcchatsetup.manager.ChatSessionManager;
import com.mongenscave.mcchatsetup.model.ChatSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Handles sign input events for chat sessions.
 * Creates and manages sign interfaces for player input.
 */
public final class SignInputHandler implements Listener {
    private final JavaPlugin plugin;
    private final ChatSessionManager sessionManager;
    private final ChatSession session;
    private final UUID sessionId;
    private final Map<UUID, Block> playerSigns = new HashMap<>();

    public SignInputHandler(@NotNull JavaPlugin plugin,
                            @NotNull ChatSessionManager sessionManager,
                            @NotNull ChatSession session,
                            @NotNull UUID sessionId) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
        this.session = session;
        this.sessionId = sessionId;
    }

    /**
     * Opens sign editor for all players in the session.
     */
    public void openSignForPlayers() {
        for (Player player : session.getPlayers()) {
            openSignEditor(player);
        }
    }

    /**
     * Opens a sign editor for a specific player.
     *
     * @param player The player to open the sign editor for
     */
    private void openSignEditor(@NotNull Player player) {
        Block signBlock = player.getLocation().add(0, 3, 0).getBlock();

        if (!signBlock.getType().isAir()) signBlock = player.getWorld().getHighestBlockAt(player.getLocation()).getRelative(0, 1, 0);

        signBlock.setType(Material.OAK_SIGN);
        BlockState state = signBlock.getState();

        if (state instanceof Sign sign) {
            sign.line(0, Component.text("Enter your input"));
            sign.line(1, Component.text("on the lines below"));
            sign.line(2, Component.text(""));
            sign.line(3, Component.text(""));
            sign.update();

            playerSigns.put(player.getUniqueId(), signBlock);
            player.openSign(sign);
        }
    }

    /**
     * Handles sign change events.
     *
     * @param event The sign change event
     */
    @EventHandler
    public void onSignChange(@NotNull SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!sessionManager.isSessionActive(sessionId)) return;
        if (!session.getPlayers().contains(player)) return;

        Block signBlock = playerSigns.get(player.getUniqueId());
        if (signBlock == null || !signBlock.equals(event.getBlock())) return;

        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            Component line = event.line(i);
            if (line != null) {
                lines[i] = LegacyComponentSerializer.legacySection().serialize(line);
                lines[i] = lines[i].replaceAll("§[0-9a-fk-or]", "");
            } else lines[i] = "";
        }

        String input = Arrays.stream(lines)
                .filter(line -> line != null && !line.trim().isEmpty())
                .reduce((a, b) -> a + " " + b)
                .orElse("");

        removePlayerSign(player);

        if (input.equalsIgnoreCase(session.getCancelCommand())) {
            event.setCancelled(true);
            handleSessionCancel();
            return;
        }

        Predicate<String> validator = session.getValidator();
        if (validator != null && !validator.test(input)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        handleSuccessfulInput(input);
    }

    /**
     * Handles player quit events to clean up sessions.
     *
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!sessionManager.isSessionActive(sessionId)) return;
        if (!session.getPlayers().contains(player)) return;

        removePlayerSign(player);
        session.removePlayer(player);

        if (session.isEmpty()) {
            handleSessionFailure();
        }
    }

    /**
     * Removes a player's temporary sign.
     *
     * @param player The player whose sign to remove
     */
    private void removePlayerSign(@NotNull Player player) {
        Block signBlock = playerSigns.remove(player.getUniqueId());
        if (signBlock != null && signBlock.getType() == Material.OAK_SIGN) {
            signBlock.setType(Material.AIR);
        }
    }

    /**
     * Cleans up all temporary signs.
     */
    public void cleanup() {
        for (Map.Entry<UUID, Block> entry : playerSigns.entrySet()) {
            Block signBlock = entry.getValue();
            if (signBlock.getType() == Material.OAK_SIGN) signBlock.setType(Material.AIR);
        }
        playerSigns.clear();
    }

    /**
     * Handles successful input processing.
     *
     * @param input The player's input
     */
    private void handleSuccessfulInput(@NotNull String input) {
        Consumer<String> onInput = session.getOnInput();
        if (onInput != null) plugin.getServer().getScheduler().runTask(plugin, () -> onInput.accept(input));

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            sessionManager.endSession(sessionId);
            session.getOnSuccess().run();
        });
    }

    /**
     * Handles session cancellation.
     */
    private void handleSessionCancel() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            sessionManager.endSession(sessionId);
            session.getOnFail().run();
        });
    }

    /**
     * Handles session failure.
     */
    private void handleSessionFailure() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            sessionManager.endSession(sessionId);
            session.getOnFail().run();
        });
    }

    /**
     * Gets the session ID for this handler.
     *
     * @return The session ID
     */
    public @NotNull UUID getSessionId() {
        return sessionId;
    }
}