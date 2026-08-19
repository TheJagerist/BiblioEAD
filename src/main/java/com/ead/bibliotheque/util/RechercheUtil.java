package com.ead.bibliotheque.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitaire de recherche textuelle multi-champs (y compris dates).
 */
public final class RechercheUtil {

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private RechercheUtil() {}

    /** Vrai si {@code terme} est vide ou apparaît dans au moins une des valeurs. */
    public static boolean contient(String terme, Object... valeurs) {
        if (terme == null || terme.isBlank()) return true;
        String t = normaliser(terme);
        for (Object v : valeurs) {
            for (String fragment : fragments(v)) {
                if (fragment.contains(t)) return true;
            }
        }
        return false;
    }

    private static String normaliser(String s) {
        return s.toLowerCase(Locale.ROOT).trim();
    }

    private static String[] fragments(Object v) {
        if (v == null) return new String[0];
        if (v instanceof LocalDate d) {
            return new String[] {
                    normaliser(DATE_FR.format(d)),
                    normaliser(d.toString()),
                    normaliser(String.valueOf(d.getYear())),
                    normaliser(String.format("%02d/%d", d.getMonthValue(), d.getYear())),
                    normaliser(String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue()))
            };
        }
        if (v instanceof LocalDateTime dt) {
            LocalDate d = dt.toLocalDate();
            return new String[] {
                    normaliser(DATETIME_FR.format(dt)),
                    normaliser(DATE_FR.format(d)),
                    normaliser(d.toString()),
                    normaliser(String.valueOf(d.getYear())),
                    normaliser(String.format("%02d/%d", d.getMonthValue(), d.getYear()))
            };
        }
        if (v instanceof Enum<?> e) {
            String name = e.name();
            return new String[] { normaliser(name), normaliser(name.replace('_', ' ')) };
        }
        String s = String.valueOf(v);
        if (s.isBlank()) return new String[0];
        return new String[] { normaliser(s) };
    }
}
