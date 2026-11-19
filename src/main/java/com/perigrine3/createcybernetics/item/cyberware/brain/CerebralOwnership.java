package com.perigrine3.createcybernetics.item.cyberware.brain;

import java.util.UUID;

/** Player names are display labels; a completed engram belongs to its saved UUID. */
final class CerebralOwnership {
    private CerebralOwnership() {}

    static boolean matches(boolean completedBinding, String storedOwnerUuid, UUID playerUuid) {
        if (!completedBinding || storedOwnerUuid == null || playerUuid == null) return false;
        if (storedOwnerUuid.length() != 36) return false;

        try {
            UUID owner = UUID.fromString(storedOwnerUuid);
            // UUID.fromString accepts some abbreviated forms. Native biochips store
            // canonical UUIDs; reject malformed data rather than inferring an owner.
            return owner.toString().equalsIgnoreCase(storedOwnerUuid) && owner.equals(playerUuid);
        } catch (IllegalArgumentException malformedUuid) {
            return false;
        }
    }
}
