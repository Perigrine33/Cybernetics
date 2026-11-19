package com.perigrine3.createcybernetics.common.quickhack;

/** Exercises naming rules without bootstrapping Minecraft. */
public final class QuickhackNameChecks {
    private static int checks;

    private static void expect(String expected, String input) {
        String actual = QuickhackNameSanitizer.sanitize(input);
        checks++;
        if (!expected.equals(actual)) throw new AssertionError("Expected <" + expected + ">, got <" + actual + ">");
        checks++;
        if (!actual.equals(QuickhackNameSanitizer.sanitize(actual))) throw new AssertionError("Name sanitization must be idempotent");
    }

    public static void main(String[] args) {
        expect("", null);
        expect("", " \t\n ");
        expect("Short_Circuit-2", "  Short  Circuit-2  ");
        expect("a_b", "a___b");
        expect("Перегрев", "Перегрев");
        expect("Короткое_замыкание", "Короткое замыкание");
        expect("Ηλεκτροπληξία", "Ηλεκτροπληξία");
        expect("短路", "短路");
        expect("ショート", "ショート");
        expect("과열", "과열");
        expect("سخونة", "سخونة");
        expect("תקלה", "תקלה");
        expect("अतिताप", "अतिताप");
        expect("cafe\u0301", "cafe\u0301");
        expect("می\u200Cشود", "می\u200Cشود");
        expect("क्\u200Dष", "क्\u200Dष");
        expect("Программа_٢", "Программа ٢");
        expect("AB", "A\u0000\n\t\u001BB");
        expect("AB", "A\u202EB");
        expect("Имя", "§Имя!./\\");
        expect("", "!😀");
        expect("Я".repeat(32), "Я".repeat(40));
        String supplementaryLetter = "\uD801\uDC00";
        expect(supplementaryLetter.repeat(16), supplementaryLetter.repeat(20));
        expect("a".repeat(31), "a".repeat(31) + supplementaryLetter);
        expect("a".repeat(30) + supplementaryLetter, "a".repeat(30) + supplementaryLetter + "z");
        System.out.println("Passed " + checks + " quickhack name checks");
    }
}
