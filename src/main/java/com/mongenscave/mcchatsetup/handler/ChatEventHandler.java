package com.mongenscave.mcchatsetup.handler;

import com.mongenscave.mcchatsetup.manager.ChatSessionManager;
import com.mongenscave.mcchatsetup.model.ChatSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Handles chat events for active chat sessions.
 * This class is responsible for processing player chat input and quit events.
 */
public final class ChatEventHandler implements Listener {
    private final JavaPlugin plugin;
    private final ChatSessionManager sessionManager;
    private final ChatSession session;

    public ChatEventHandler(@NotNull JavaPlugin plugin,
                            @NotNull ChatSessionManager sessionManager,
                            @NotNull ChatSession session) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
        this.session = session;
    }

    /**
     * Handles chat events from players in active sessions.
     *
     * @param event The chat event
     */
    @EventHandler
    public void onPlayerChat(final @NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!session.getPlayers().contains(player)) {
            return;
        }

        String message = event.getMessage();

        // Check for cancel command
        if (message.equalsIgnoreCase(session.getCancelCommand())) {
            event.setCancelled(true);
            handleSessionCancel();
            return;
        }

        // Validate input if validator exists
        Predicate<String> validator = session.getValidator();
        if (validator != null && !validator.test(message)) {
            return;
        }

        event.setCancelled(true);
        handleSuccessfulInput(message);
    }

    /**
     * Handles player quit events to clean up sessions.
     *
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!session.getPlayers().contains(player)) {
            return;
        }

        session.removePlayer(player);

        if (session.isEmpty()) {
            handleSessionFailure();
        }
    }

    /**
     * Handles successful input processing.
     *
     * @param input The player's input
     */
    private void handleSuccessfulInput(String input) {
        Consumer<String> onInput = session.getOnInput();
        if (onInput != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> onInput.accept(input));
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            sessionManager.endSession(session);
            session.getOnSuccess().run();
        });
    }

    /**
     * Handles session cancellation.
     */
    private void handleSessionCancel() {
        sessionManager.endSession(session);
        session.getOnFail().run();
    }

    /**
     * Handles session failure (timeout or player quit).
     */
    private void handleSessionFailure() {
        sessionManager.endSession(session);
        session.getOnFail().run();
    }
}