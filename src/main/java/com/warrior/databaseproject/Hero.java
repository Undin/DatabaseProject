package com.warrior.databaseproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by warrior on 12.01.15.
 */
public class Hero implements Values {

    private static final String DELIMITER = ", ";

    public static final String HEROES_FIRST_LINE = "INSERT INTO heroes (hero_id, class, hero_name, hero_health) VALUES";

    private static int globalId = 1;

    private final String heroName;
    private final String className;
    private final int id;

    private final List<Card> cards = new ArrayList<>();

    public Hero(String heroName, String className) {
        this.heroName = heroName;
        this.className = className;
        this.id = globalId++;
    }

    public String getHeroName() {
        return heroName;
    }

    public String getClassName() {
        return className;
    }

    public int getId() {
        return id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    @Override
    public String toString() {
        return Utils.toString(id, className, heroName, 30);
    }
}
