package com.mongenscave.mcchatsetup;

import com.mongenscave.mcchatsetup.builder.ChatSessionBuilder;
import com.mongenscave.mcchatsetup.identifiers.InputType;
import com.mongenscave.mcchatsetup.manager.ChatSessionManager;
import com.mongenscave.mcchatsetup.model.ChatSession;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Main facade class for McChatSetup.
 * Provides a simplified interface for creating and managing chat sessions.
 * This class maintains backward compatibility with the original API while supporting
 * new input types like ANVIL and SIGN.
 */
public final class McChatSetup {

    @Getter private final JavaPlugin plugin;
    @Getter private final ChatSessionManager sessionManager;
    private final ChatSessionBuilder builder;

    /**
     * Creates a new McChatSetup instance with the specified plugin.
     *
     * @param plugin The JavaPlugin instance
     */
    public McChatSetup(JavaPlugin plugin) {
        this.plugin = plugin;
        this.sessionManager = new ChatSessionManager(plugin);
        this.builder = ChatSessionBuilder.create(plugin);
    }

    /**
     * Creates an empty McChatSetup instance.
     *
     * @return A new McChatSetup instance
     */
    public static @NotNull McChatSetup empty() {
        JavaPlugin mainPlugin = JavaPlugin.getProvidingPlugin(McChatSetup.class);
        return new McChatSetup(mainPlugin);
    }

    /**
     * Creates an empty McChatSetup instance with explicit plugin reference.
     *
     * @param plugin The JavaPlugin instance
     * @return A new McChatSetup instance
     */
    @Contract("_ -> new")
    public static @NotNull McChatSetup empty(JavaPlugin plugin) {
        return new McChatSetup(plugin);
    }

    /**
     * Adds a player to listen for chat inputs from.
     *
     * @param player The player to add
     * @return This instance for method chaining
     */
    @Contract("_ -> this")
    public McChatSetup addPlayer(Player player) {
        builder.addPlayer(player);
        return this;
    }

    /**
     * Sets the message to display to the player.
     * Supports MiniMessage format with placeholders.
     *
     * @param message The message to display
     * @return This instance for method chaining
     */
    public McChatSetup append(String message) {
        builder.withMessage(message);
        return this;
    }

    /**
     * Sets the time limit for the input in seconds.
     *
     * @param seconds The time limit in seconds
     * @return This instance for method chaining
     */
    public McChatSetup setTime(int seconds) {
        builder.withTimeLimit(seconds);
        return this;
    }

    /**
     * Sets the time limit for the input.
     *
     * @param duration The time limit as a Duration
     * @return This instance for method chaining
     */
    public McChatSetup setTime(Duration duration) {
        builder.withTimeLimit(duration);
        return this;
    }

    /**
     * Sets the callback to execute on successful input.
     *
     * @param onSuccess The callback to execute
     * @return This instance for method chaining
     */
    public McChatSetup onSuccess(Runnable onSuccess) {
        builder.onSuccess(onSuccess);
        return this;
    }

    /**
     * Sets the callback to execute on input failure or timeout.
     *
     * @param onFail The callback to execute
     * @return This instance for method chaining
     */
    public McChatSetup onFail(Runnable onFail) {
        builder.onFail(onFail);
        return this;
    }

    /**
     * Sets the callback to process the received input.
     *
     * @param onInput The callback to process input
     * @return This instance for method chaining
     */
    public McChatSetup onInput(Consumer<String> onInput) {
        builder.onInput(onInput);
        return this;
    }

    /**
     * Sets the validator to check if input is valid.
     *
     * @param validator The predicate to validate input
     * @return This instance for method chaining
     */
    public McChatSetup withValidator(Predicate<String> validator) {
        builder.withValidator(validator);
        return this;
    }

    /**
     * Sets the command to cancel the input process.
     *
     * @param cancelCommand The cancel command
     * @return This instance for method chaining
     */
    public McChatSetup setCancel(String cancelCommand) {
        builder.withCancelCommand(cancelCommand);
        return this;
    }

    /**
     * Sets the collection to check if the players are contained within.
     *
     * @param collection The collection to check
     * @return This instance for method chaining
     */
    public McChatSetup listenTo(Collection<?> collection) {
        builder.listenTo(collection);
        return this;
    }

    /**
     * Sets the callback to execute when the chat setup starts.
     *
     * @param onStart The callback to execute at start
     * @return This instance for method chaining
     */
    public McChatSetup onStart(Runnable onStart) {
        builder.onStart(onStart);
        return this;
    }

    /**
     * Sets the input type to CHAT (default).
     * Players will type their input in chat.
     *
     * @return This instance for method chaining
     */
    public McChatSetup useChatInput() {
        builder.withInputType(InputType.CHAT);
        return this;
    }

    /**
     * Sets the input type to SIGN.
     * Players will input text using a sign editor.
     *
     * @return This instance for method chaining
     */
    public McChatSetup useSignInput() {
        builder.withInputType(InputType.SIGN);
        return this;
    }

    /**
     * Sets the input type explicitly.
     *
     * @param inputType The input type to use
     * @return This instance for method chaining
     */
    public McChatSetup withInputType(InputType inputType) {
        builder.withInputType(inputType);
        return this;
    }

    /**
     * Starts a chat session with a specific player.
     * This is a convenient method for quick setups.
     *
     * @param player The player to start the session with
     * @return This instance for method chaining
     */
    public McChatSetup startSession(Player player) {
        builder.addPlayer(player);
        build();
        return this;
    }

    /**
     * Starts a quick chat input session with a player.
     * Equivalent to startSession() but with a more descriptive name.
     *
     * @param player The player to start the session with
     * @return This instance for method chaining
     */
    public McChatSetup startChatSession(Player player) {
        return startSession(player);
    }

    /**
     * Starts a quick sign input session with a player.
     *
     * @param player The player to start the session with
     * @return This instance for method chaining
     */
    public McChatSetup startSignSession(Player player) {
        return useSignInput().startSession(player);
    }

    /**
     * Builds and starts the chat input process.
     */
    public void build() {
        ChatSession session = builder.build();
        sessionManager.startSession(session);
    }

    /**
     * Gets the number of currently active sessions.
     *
     * @return The number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }

    /**
     * Ends all active sessions managed by this instance.
     */
    public void endAllSessions() {
        sessionManager.endAllSessions();
    }

    /**
     * Creates a new builder instance for advanced usage.
     *
     * @return A new ChatSessionBuilder
     */
    public static @NotNull ChatSessionBuilder builder() {
        return ChatSessionBuilder.create();
    }

    /**
     * Creates a new builder instance with explicit plugin reference.
     *
     * @param plugin The JavaPlugin instance
     * @return A new ChatSessionBuilder
     */
    @Contract("_ -> new")
    public static @NotNull ChatSessionBuilder builder(JavaPlugin plugin) {
        return ChatSessionBuilder.create(plugin);
    }

    /**
     * Creates a quick chat input setup.
     *
     * @param plugin The plugin instance
     * @return A new McChatSetup instance configured for chat input
     */
    public static McChatSetup forChat(JavaPlugin plugin) {
        return new McChatSetup(plugin).useChatInput();
    }

    /**
     * Creates a quick sign input setup.
     *
     * @param plugin The plugin instance
     * @return A new McChatSetup instance configured for sign input
     */
    public static McChatSetup forSign(JavaPlugin plugin) {
        return new McChatSetup(plugin).useSignInput();
    }
}