package com.warrior.databaseproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by warrior on 12.01.15.
 */
public class Player implements Values {

    public static final String PLAYERS_FIRST_LINE = "INSERT INTO players VALUES";

    private static int globalId = 1;

    private final String name;
    private final int id;

    private final Map<Card, Integer> cards = new HashMap<>();
    private final List<Deck> decks = new ArrayList<>();


    public Player(String name) {
        this.name = name;
        id = globalId++;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Deck> getDecks() {
        return decks;
    }

    public Map<Card, Integer> getCards() {
        return cards;
    }

    public void addCard(Card card, int quantity) {
        int qnt = cards.getOrDefault(card, 0);
        cards.put(card, Math.max(qnt, quantity));
    }

    public void addDeck(Deck deck) {
        for (Card card : deck.getCards().keySet()) {
            addCard(card, deck.getCards().get(card));
        }
        decks.add(deck);
        deck.setPlayer(this);
    }

    @Override
    public String toString() {
        return Utils.toString(id, name);
    }
}
