package com.perigrine3.createcybernetics.item.cyberware.brain;

import java.util.Locale;
import java.util.UUID;

/** Checks ownership without constructing or modifying a Minecraft player. */
public final class CerebralOwnershipChecks {
    private static int checks;

    private static void expect(boolean expected, boolean complete, String stored, UUID player, String description) {
        checks++;
        if (CerebralOwnership.matches(complete, stored, player) != expected) throw new AssertionError(description);
    }

    public static void main(String[] args) {
        UUID owner = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
        UUID other = UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210");
        expect(true, true, owner.toString(), owner, "completed CPU accepts its owner");
        expect(true, true, owner.toString(), UUID.fromString(owner.toString()), "persisted UUID matches by value");
        expect(true, true, owner.toString().toUpperCase(Locale.ROOT), owner, "UUID case is not identity");
        expect(false, true, owner.toString(), other, "another player cannot use the owner's CPU");
        expect(false, true, other.toString(), owner, "a foreign CPU is rejected");
        expect(false, false, owner.toString(), owner, "incomplete biochip remains invalid");
        expect(false, true, null, owner, "missing UUID is rejected");
        expect(false, true, "", owner, "empty UUID is rejected");
        expect(false, true, " ".repeat(36), owner, "blank UUID is rejected");
        expect(false, true, "not-a-uuid", owner, "malformed UUID is rejected without throwing");
        expect(false, true, "g1234567-89ab-cdef-0123-456789abcdef", owner, "invalid hex is rejected");
        expect(false, true, "0123456789abcdef0123456789abcdef", owner, "missing separators are rejected");
        expect(false, true, owner + " ", owner, "extra characters do not silently change identity");
        expect(false, true, owner.toString(), null, "missing player identity is rejected");
        UUID abbreviated = UUID.fromString("1-1-1-1-1");
        expect(false, true, "1-1-1-1-1", abbreviated, "abbreviated UUID is rejected");
        expect(true, true, abbreviated.toString(), abbreviated, "canonical equivalent remains valid");
        System.out.println("Passed " + checks + " cerebral ownership checks");
    }
}
