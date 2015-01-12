package com.warrior.databaseproject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by warrior on 12.01.15.
 */
public class Deck implements Values {

    public static final String DECK_FIRST_LINE = "INSERT INTO decks (deck_id, deck_name, player_id, hero_id) VALUES";

    private static int globalId = 1;

    private final int id;
    private final String name;
    private final Hero hero;
    private final Map<Card, Integer> cards = new HashMap<>();

    private Player player;

    public Deck(String name, Hero hero) {
        this.id = globalId++;
        this.name = name;
        this.hero = hero;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Hero getHero() {
        return hero;
    }

    public Map<Card, Integer> getCards() {
        return cards;
    }

    public void addCard(Card card, int quantity) {
        int qnt = cards.getOrDefault(card, 0);
        cards.put(card, Math.max(qnt, quantity));
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return Utils.toString(id, name, player.getId(), hero.getId());
    }
}
