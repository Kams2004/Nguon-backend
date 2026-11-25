package com.example.ngoun.model;

import java.text.Normalizer;

public enum VoteCategory {
    MISS("Miss"),
    MISTER("Mister");

    private final String label;

    VoteCategory(String label) { this.label = label; }

    public String getLabel() { return label; }

    /**
     * Best guess for profiles created before categories existed: their
     * presentations start with "Candidate …" (Miss) or "Candidat …" (Mister),
     * often in Unicode bold letters, hence the NFKC folding.
     */
    public static VoteCategory guessFrom(String description) {
        if (description == null) return MISS;
        String d = Normalizer.normalize(description, Normalizer.Form.NFKC)
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "").trim().toLowerCase();
        if (d.startsWith("candidate")) return MISS;
        if (d.startsWith("candidat")) return MISTER;
        return MISS;
    }
}
