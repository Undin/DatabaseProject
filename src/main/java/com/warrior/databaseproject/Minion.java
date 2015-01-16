package com.warrior.databaseproject;

/**
 * Created by warrior on 16.01.15.
 */
public class Minion implements Values {

    public static final String INSERT_FIRST_LINE = "INSERT INTO minions (card_id, health, attack, race) VALUES";

    private final Card card;

    public Minion(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    @Override
    public String toString() {
        return Utils.toString(card.getId(), card.getHealth(), card.getAttack(), card.getRace());
    }
}
