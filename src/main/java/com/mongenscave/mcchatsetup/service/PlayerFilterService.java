package com.mongenscave.mcchatsetup.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for filtering players based on various criteria.
 * Handles player validation and collection filtering.
 */
public final class PlayerFilterService {

    /**
     * Filters players based on a collection containing players, UUIDs, or names.
     *
     * @param players The set of players to filter
     * @param collection The collection to check against
     * @return A new set containing only players that match the collection criteria
     */
    public @NotNull Set<Player> filterByCollection(@NotNull Set<Player> players,
                                                   @NotNull Collection<?> collection) {
        Set<Player> filteredPlayers = new HashSet<>();

        for (Player player : players) {
            if (isPlayerInCollection(player, collection)) {
                filteredPlayers.add(player);
            }
        }

        return filteredPlayers;
    }

    /**
     * Checks if a player is contained in a collection by player object, UUID, or name.
     *
     * @param player The player to check
     * @param collection The collection to check against
     * @return true if the player is found in the collection
     */
    private boolean isPlayerInCollection(@NotNull Player player, @NotNull Collection<?> collection) {
        return collection.contains(player) ||
                collection.contains(player.getUniqueId()) ||
                collection.contains(player.getName());
    }

    /**
     * Filters players that are currently online.
     *
     * @param players The set of players to filter
     * @return A new set containing only online players
     */
    public @NotNull Set<Player> filterOnlinePlayers(@NotNull Set<Player> players) {
        Set<Player> onlinePlayers = new HashSet<>();

        for (Player player : players) {
            if (player.isOnline()) onlinePlayers.add(player);
        }

        return onlinePlayers;
    }

    /**
     * Filters players based on permission.
     *
     * @param players The set of players to filter
     * @param permission The permission to check
     * @return A new set containing only players with the specified permission
     */
    public @NotNull Set<Player> filterByPermission(@NotNull Set<Player> players, @NotNull String permission) {
        Set<Player> filteredPlayers = new HashSet<>();

        for (Player player : players) {
            if (player.hasPermission(permission)) filteredPlayers.add(player);
        }

        return filteredPlayers;
    }
}