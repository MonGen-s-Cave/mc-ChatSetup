package com.mongenscave.mcchatsetup.builder;

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
 * Builder class for creating ChatSession instances with a fluent interface.
 */
public final class ChatSessionBuilder {
    private final ChatSession session;

    @Getter private final JavaPlugin plugin;

    private ChatSessionBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
        this.session = new ChatSession();
    }

    /**
     * Creates a new ChatSessionBuilder instance.
     *
     * @return A new builder instance
     */
    public static @NotNull ChatSessionBuilder create() {
        JavaPlugin mainPlugin = JavaPlugin.getProvidingPlugin(ChatSessionBuilder.class);
        return new ChatSessionBuilder(mainPlugin);
    }

    /**
     * Creates a new ChatSessionBuilder instance with explicit plugin reference.
     *
     * @param plugin The JavaPlugin instance
     * @return A new builder instance
     */
    @Contract("_ -> new")
    public static @NotNull ChatSessionBuilder create(JavaPlugin plugin) {
        return new ChatSessionBuilder(plugin);
    }

    /**
     * Adds a player to listen for chat inputs from.
     *
     * @param player The player to add
     */
    @Contract("_ -> this")
    public void addPlayer(Player player) {
        session.addPlayer(player);
    }

    /**
     * Sets the message to display to the player.
     * Supports MiniMessage format with placeholders:
     * {time} - time limit in seconds
     * {cancel} - cancel command
     *
     * @param message The message to display
     */
    public void withMessage(String message) {
        session.setMessage(message);
    }

    /**
     * Sets the time limit for the input in seconds.
     *
     * @param seconds The time limit in seconds
     */
    public void withTimeLimit(int seconds) {
        session.setTimeLimit(Duration.ofSeconds(seconds));
    }

    /**
     * Sets the time limit for the input.
     *
     * @param duration The time limit as a Duration
     */
    public void withTimeLimit(Duration duration) {
        session.setTimeLimit(duration);
    }

    /**
     * Sets the callback to execute on successful input.
     *
     * @param onSuccess The callback to execute
     */
    public void onSuccess(Runnable onSuccess) {
        session.setOnSuccess(onSuccess);
    }

    /**
     * Sets the callback to execute on input failure or timeout.
     *
     * @param onFail The callback to execute
     */
    public void onFail(Runnable onFail) {
        session.setOnFail(onFail);
    }

    /**
     * Sets the callback to execute when the chat setup starts.
     *
     * @param onStart The callback to execute at start
     */
    public void onStart(Runnable onStart) {
        session.setOnStart(onStart);
    }

    /**
     * Sets the callback to process the received input.
     *
     * @param onInput The callback to process input
     */
    public void onInput(Consumer<String> onInput) {
        session.setOnInput(onInput);
    }

    /**
     * Sets the validator to check if input is valid.
     *
     * @param validator The predicate to validate input
     */
    public void withValidator(Predicate<String> validator) {
        session.setValidator(validator);
    }

    /**
     * Sets the command to cancel the input process.
     *
     * @param cancelCommand The cancel command
     */
    public void withCancelCommand(String cancelCommand) {
        session.setCancelCommand(cancelCommand);
    }

    /**
     * Sets the collection to check if the players are contained within.
     * Can be any collection type like Set, List, or values of a Map.
     *
     * @param collection The collection to check
     */
    public void listenTo(Collection<?> collection) {
        session.setListenerCollection(collection);
    }

    /**
     * Builds the ChatSession instance.
     *
     * @return The configured ChatSession
     */
    public ChatSession build() {
        return session;
    }
}