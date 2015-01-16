package com.warrior.databaseproject;

/**
 * Created by warrior on 16.01.15.
 */
public class Spell implements Values {

    public static final String INSERT_FIRST_LINE = "INSERT INTO spells (card_id) VALUES";

    private final Card card;

    public Spell(Card card) {
        this.card = card;
    }

    @Override
    public String toString() {
        return Utils.toString(card.getId());
    }
}
