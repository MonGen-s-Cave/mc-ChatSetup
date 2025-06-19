package com.mongenscave.mcchatsetup.manager;

import com.mongenscave.mcchatsetup.handler.ChatEventHandler;
import com.mongenscave.mcchatsetup.model.ChatSession;
import com.mongenscave.mcchatsetup.service.MessageFormatter;
import com.mongenscave.mcchatsetup.service.PlayerFilterService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Main service class for managing chat sessions.
 * Handles session lifecycle, event registration, and timeout management.
 */
public final class ChatSessionManager {
    private final JavaPlugin plugin;
    private final MessageFormatter messageFormatter;
    private final PlayerFilterService playerFilterService;
    private final ConcurrentMap<ChatSession, SessionContext> activeSessions;

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
        if (session.isEmpty()) throw new IllegalStateException("No players added to ChatSession");

        filterPlayersIfNeeded(session);

        if (session.isEmpty()) {
            session.getOnFail().run();
            return;
        }

        session.getOnStart().run();

        ChatEventHandler eventHandler = new ChatEventHandler(plugin, this, session);
        plugin.getServer().getPluginManager().registerEvents(eventHandler, plugin);

        sendMessageToPlayers(session);

        BukkitTask timeoutTask = createTimeoutTask(session);

        SessionContext context = new SessionContext(eventHandler, timeoutTask);
        activeSessions.put(session, context);
    }

    /**
     * Ends an active chat session and cleans up resources.
     *
     * @param session The session to end
     */
    public void endSession(@NotNull ChatSession session) {
        SessionContext context = activeSessions.remove(session);
        if (context != null) {
            cleanup(context);
        }
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
     * @param session The session to check
     * @return true if the session is active
     */
    public boolean isSessionActive(@NotNull ChatSession session) {
        return activeSessions.containsKey(session);
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
        for (ChatSession session : new HashSet<>(activeSessions.keySet())) {
            endSession(session);
        }
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
     * @param session The session to create a timeout for
     * @return The created BukkitTask
     */
    private @NotNull BukkitTask createTimeoutTask(@NotNull ChatSession session) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                endSession(session);
                session.getOnFail().run();
            }
        }.runTaskLater(plugin, session.getTimeLimit().toSeconds() * 20L);
    }

    /**
     * Cleans up resources for a session context.
     *
     * @param context The session context to clean up
     */
    private void cleanup(@NotNull SessionContext context) {
        if (!context.timeoutTask().isCancelled()) context.timeoutTask().cancel();
        HandlerList.unregisterAll(context.eventHandler());
    }

    /**
     * Internal record to hold session context data.
     */
    private record SessionContext(@NotNull ChatEventHandler eventHandler,
                                  @NotNull BukkitTask timeoutTask) {
    }
}