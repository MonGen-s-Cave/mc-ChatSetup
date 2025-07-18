package com.mongenscave.mcchatsetup.model;

import com.mongenscave.mcchatsetup.identifiers.InputType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a chat session configuration.
 * This class holds all the configuration data for a chat input session.
 */
public final class ChatSession {
    private final Set<Player> players = Collections.synchronizedSet(new HashSet<>());
    @Getter @Setter private String message = "";
    @Getter @Setter private Duration timeLimit = Duration.ofSeconds(30);
    @Getter @Setter private String cancelCommand = "cancel";
    @Getter @Setter private Collection<?> listenerCollection = null;
    @Getter @Setter private Runnable onSuccess = () -> {};
    @Getter @Setter private Runnable onFail = () -> {};
    @Getter @Setter private Runnable onStart = () -> {};
    @Getter @Setter private Consumer<String> onInput = null;
    @Getter @Setter private Predicate<String> validator = null;
    @Getter @Setter private InputType inputType = InputType.CHAT;

    @Contract(value = " -> new", pure = true)
    public @NotNull Set<Player> getPlayers() {
        return new HashSet<>(players);
    }

    public void addPlayer(@NotNull Player player) {
        players.add(player);
    }

    public void removePlayer(@NotNull Player player) {
        players.remove(player);
    }

    public void clearPlayers() {
        players.clear();
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }
}