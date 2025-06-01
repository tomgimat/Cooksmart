package fr.tomgimat.cooksmart.data.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Représente une étape d'une recette.
 */
public class RecipeStep {
    private String instruction;
    private int duration; // en secondes
    private boolean isTimerStep;

    public RecipeStep(String instruction) {
        this.instruction = instruction;
        this.duration = extractDuration(instruction);
        this.isTimerStep = duration > 0;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isTimerStep() {
        return isTimerStep;
    }

    /**
     * Extrait la durée en secondes d'une instruction
     * todo Non fonctionnel
     */
    private int extractDuration(String instruction) {
        //todo le pattern n'est pas reconnu pour les différentes instructions disponibles dans les recettes
        // jamais de match malgré de nombreux tests sur différentes recettes
        Pattern pattern = Pattern.compile("(\\d+)\\s*(\\Qmin\\E|\\Qminutes\\E|\\Qh\\E|\\Qheures\\E|\\Qheure\\E)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(instruction);

        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            if (unit != null && unit.toLowerCase().startsWith("h")) {
                return value * 3600;
            } else if (unit != null && (unit.toLowerCase().startsWith("min") || unit.toLowerCase().equals("minutes"))) {
                return value * 60;
            }
        }
        return 0;
    }
} 