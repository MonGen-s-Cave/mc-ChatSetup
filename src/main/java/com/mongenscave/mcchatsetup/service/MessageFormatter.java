package com.mongenscave.mcchatsetup.service;

import com.mongenscave.mcchatsetup.model.ChatSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

/**
 * Service for formatting and processing chat messages using MiniMessage.
 * Handles MiniMessage formatting with custom TagResolvers for placeholders.
 */
public final class MessageFormatter {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Formats a chat session message with custom tag resolvers.
     *
     * @param session The chat session containing the message and configuration
     * @return The formatted Component ready to be sent
     */
    public @NotNull Component formatSessionMessage(@NotNull ChatSession session) {
        TagResolver sessionResolver = createSessionTagResolver(session);
        return MINI_MESSAGE.deserialize(session.getMessage(), sessionResolver);
    }

    /**
     * Formats a raw message string with MiniMessage formatting and custom resolvers.
     *
     * @param message The raw message string
     * @param resolvers Additional tag resolvers
     * @return The formatted Component
     */
    public @NotNull Component formatMessage(@NotNull String message, TagResolver... resolvers) {
        TagResolver combined = TagResolver.resolver(
                StandardTags.defaults(),
                TagResolver.resolver(resolvers)
        );
        return MINI_MESSAGE.deserialize(message, combined);
    }

    /**
     * Creates a TagResolver for session-specific placeholders.
     *
     * @param session The session containing the values
     * @return TagResolver with session placeholders
     */
    private @NotNull TagResolver createSessionTagResolver(@NotNull ChatSession session) {
        return TagResolver.resolver(
                StandardTags.defaults(),
                TagResolver.resolver("time", Tag.inserting(Component.text(session.getTimeLimit().toSeconds()))),
                TagResolver.resolver("cancel", Tag.inserting(Component.text(session.getCancelCommand())))
        );
    }

    /**
     * Creates a custom TagResolver for additional placeholders.
     *
     * @param key The tag key (without angle brackets)
     * @param value The component value
     * @return A TagResolver for the custom placeholder
     */
    public @NotNull TagResolver createCustomResolver(@Subst("") @NotNull String key, @NotNull Component value) {
        return TagResolver.resolver(key, Tag.inserting(value));
    }

    /**
     * Creates a custom TagResolver for string-based placeholders.
     *
     * @param key The tag key (without angle brackets)
     * @param value The string value
     * @return A TagResolver for the custom placeholder
     */
    public @NotNull TagResolver createCustomResolver(@Subst("") @NotNull String key, @NotNull String value) {
        return TagResolver.resolver(key, Tag.inserting(Component.text(value)));
    }
}