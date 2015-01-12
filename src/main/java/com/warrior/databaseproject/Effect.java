package com.warrior.databaseproject;

/**
 * Created by warrior on 12.01.15.
 */
public class Effect implements Values {

    public static final String EFFECT_FIRST_LINE = "INSERT INTO effects (effect_id, effect_name) VALUES";

    private static int globalId = 1;

    private final String effectName;
    private final int id;

    public Effect(String effectName) {
        this.effectName = effectName;
        this.id = globalId++;
    }

    public String getEffectName() {
        return effectName;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return Utils.toString(id, effectName);
    }
}
