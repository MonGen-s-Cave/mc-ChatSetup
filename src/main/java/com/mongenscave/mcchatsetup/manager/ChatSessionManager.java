package com.mongenscave.mcchatsetup.manager;

import com.mongenscave.mcchatsetup.handler.ChatEventHandler;
import com.mongenscave.mcchatsetup.handler.SignInputHandler;
import com.mongenscave.mcchatsetup.model.ChatSession;
import com.mongenscave.mcchatsetup.service.MessageFormatter;
import com.mongenscave.mcchatsetup.service.PlayerFilterService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Main service class for managing chat sessions.
 * Handles session lifecycle, event registration, and timeout management.
 * Uses UUID-based session tracking instead of ChatSession objects as keys.
 */
public final class ChatSessionManager {
    private final JavaPlugin plugin;
    private final MessageFormatter messageFormatter;
    private final PlayerFilterService playerFilterService;
    private final ConcurrentMap<UUID, ActiveSession> activeSessions;

    public ChatSessionManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.messageFormatter = new MessageFormatter();
        this.playerFilterService = new PlayerFilterService();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    /**
     * Starts a new chat session.
     *
     * @param session The chat session to start
     * @throws IllegalStateException if no players are added to the session
     */
    public void startSession(@NotNull ChatSession session) {
        if (session.isEmpty()) {
            throw new IllegalStateException("No players added to ChatSession");
        }

        filterPlayersIfNeeded(session);

        if (session.isEmpty()) {
            session.getOnFail().run();
            return;
        }

        session.getOnStart().run();

        UUID sessionId = UUID.randomUUID();
        Listener eventHandler = createEventHandler(session, sessionId);
        plugin.getServer().getPluginManager().registerEvents(eventHandler, plugin);

        handleSessionStart(session, eventHandler);
        BukkitTask timeoutTask = createTimeoutTask(sessionId, session);

        ActiveSession activeSession = new ActiveSession(session, eventHandler, timeoutTask);
        activeSessions.put(sessionId, activeSession);
    }

    /**
     * Creates the appropriate event handler based on input type.
     *
     * @param session The chat session
     * @param sessionId The session ID
     * @return The created event handler
     */
    private @NotNull Listener createEventHandler(@NotNull ChatSession session, @NotNull UUID sessionId) {
        return switch (session.getInputType()) {
            case CHAT -> new ChatEventHandler(plugin, this, session, sessionId);
            case SIGN -> new SignInputHandler(plugin, this, session, sessionId);
        };
    }

    /**
     * Handles session start based on input type.
     *
     * @param session The chat session
     * @param eventHandler The event handler
     */
    private void handleSessionStart(@NotNull ChatSession session, @NotNull Listener eventHandler) {
        switch (session.getInputType()) {
            case CHAT -> sendMessageToPlayers(session);
            case SIGN -> {
                if (eventHandler instanceof SignInputHandler signHandler) signHandler.openSignForPlayers();
            }
        }
    }

    /**
     * Ends an active chat session and cleans up resources.
     *
     * @param sessionId The session ID to end
     */
    public void endSession(@NotNull UUID sessionId) {
        ActiveSession activeSession = activeSessions.remove(sessionId);
        if (activeSession != null) cleanup(activeSession);
    }

    /**
     * Starts a quick session with a single player.
     *
     * @param session The session configuration
     * @param player The player to add to the session
     */
    public void startQuickSession(@NotNull ChatSession session, @NotNull Player player) {
        session.addPlayer(player);
        startSession(session);
    }

    /**
     * Checks if a session is currently active.
     *
     * @param sessionId The session ID to check
     * @return true if the session is active
     */
    public boolean isSessionActive(@NotNull UUID sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * Gets the number of active sessions.
     *
     * @return The number of active sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Ends all active sessions.
     */
    public void endAllSessions() {
        activeSessions.keySet().forEach(this::endSession);
    }

    /**
     * Gets an active session by ID.
     *
     * @param sessionId The session ID
     * @return The active session, or null if not found
     */
    public ActiveSession getActiveSession(@NotNull UUID sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Filters players in the session if a listener collection is provided.
     *
     * @param session The session to filter players for
     */
    private void filterPlayersIfNeeded(@NotNull ChatSession session) {
        Collection<?> listenerCollection = session.getListenerCollection();
        if (listenerCollection != null) {
            Set<Player> filteredPlayers = playerFilterService.filterByCollection(
                    session.getPlayers(), listenerCollection);

            session.clearPlayers();
            filteredPlayers.forEach(session::addPlayer);
        }
    }

    /**
     * Sends the formatted message to all players in the session.
     *
     * @param session The session containing the players and message
     */
    private void sendMessageToPlayers(@NotNull ChatSession session) {
        Component component = messageFormatter.formatSessionMessage(session);
        for (Player player : session.getPlayers()) {
            player.sendMessage(component);
        }
    }

    /**
     * Creates a timeout task for the session.
     *
     * @param sessionId The session ID
     * @param session The session configuration
     * @return The created BukkitTask
     */
    private @NotNull BukkitTask createTimeoutTask(@NotNull UUID sessionId, @NotNull ChatSession session) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                endSession(sessionId);
                session.getOnFail().run();
            }
        }.runTaskLater(plugin, session.getTimeLimit().toSeconds() * 20L);
    }

    /**
     * Cleans up resources for an active session.
     *
     * @param activeSession The active session to clean up
     */
    private void cleanup(@NotNull ActiveSession activeSession) {
        if (!activeSession.timeoutTask().isCancelled()) {
            activeSession.timeoutTask().cancel();
        }

        if (activeSession.eventHandler() instanceof SignInputHandler signHandler) {
            signHandler.cleanup();
        }

        HandlerList.unregisterAll(activeSession.eventHandler());
    }

    /**
     * Record representing an active session with all its components.
     */
    public record ActiveSession(@NotNull ChatSession session,
                                @NotNull Listener eventHandler,
                                @NotNull BukkitTask timeoutTask) {
    }
}