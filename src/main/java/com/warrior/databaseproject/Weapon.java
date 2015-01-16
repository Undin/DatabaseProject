package com.warrior.databaseproject;

/**
 * Created by warrior on 16.01.15.
 */
public class Weapon implements Values {

    public static final String INSERT_FIRST_LINE = "INSERT INTO weapons (card_id, attack, durability) VALUES";

    private final Card card;

    public Weapon(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    @Override
    public String toString() {
        return Utils.toString(card.getId(), card.getAttack(), card.getDurability());
    }
}
