package com.mongenscave.mcchatsetup.service;

import com.mongenscave.mcchatsetup.model.ChatSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Service for formatting and processing chat messages.
 * Handles MiniMessage formatting and placeholder replacement.
 */
public final class MessageFormatter {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Formats a chat session message with placeholders replaced.
     *
     * @param session The chat session containing the message and configuration
     * @return The formatted Component ready to be sent
     */
    public @NotNull Component formatSessionMessage(@NotNull ChatSession session) {
        String formattedMessage = replacePlaceholders(session.getMessage(), session);
        return MINI_MESSAGE.deserialize(formattedMessage);
    }

    /**
     * Formats a raw message string with MiniMessage formatting.
     *
     * @param message The raw message string
     * @return The formatted Component
     */
    public @NotNull Component formatMessage(@NotNull String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Replaces placeholders in the message with actual values.
     *
     * @param message The message with placeholders
     * @param session The session containing the values
     * @return The message with replaced placeholders
     */
    private @NotNull String replacePlaceholders(@NotNull String message, @NotNull ChatSession session) {
        return message
                .replace("{time}", String.valueOf(session.getTimeLimit().toSeconds()))
                .replace("{cancel}", session.getCancelCommand());
    }

    /**
     * Adds a custom placeholder replacement to the message.
     *
     * @param message The message to process
     * @param placeholder The placeholder to replace (without braces)
     * @param value The value to replace with
     * @return The message with the placeholder replaced
     */
    public @NotNull String replaceCustomPlaceholder(@NotNull String message,
                                                    @NotNull String placeholder,
                                                    @NotNull String value) {
        return message.replace("{" + placeholder + "}", value);
    }
}