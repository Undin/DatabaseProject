package com.warrior.databaseproject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by warrior on 09.01.15.
 */
public class Card implements Values {

    public static final String INSERT_FIRST_LINE = "INSERT INTO cards (card_id, card_name, description, rarity, set, collectible, cost) VALUES";

    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String RARITY = "rarity";
    private static final String TYPE = "type";
    private static final String COLLECTIBLE = "collectible";
    private static final String COST = "cost";
    private static final String HEALTH = "health";
    private static final String ATTACK = "attack";
    private static final String DURABILITY = "durability";
    private static final String RACE = "race";

    private static final String DELIMITER = ", ";

    private static int globalID = 1;

    private final JSONObject object;
    private final String set;
    private final int id;

    private final List<Effect> effects = new ArrayList<>();

    public Card(JSONObject object, String set) {
        this.object = object;
        this.set = set;
        id = globalID++;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return object.getString(NAME);
    }

    public boolean isCollectible() {
        return object.has(COLLECTIBLE) && object.getBoolean(COLLECTIBLE);
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public JSONObject getObject() {
        return object;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;

        Card card = (Card) o;

        if (id != card.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(id).append(DELIMITER);
        builder.append(getSqlValue(object, NAME)).append(DELIMITER);
        builder.append(getSqlValue(object, TEXT)).append(DELIMITER);
        builder.append(getSqlValue(object, RARITY)).append(DELIMITER);
        builder.append(Utils.toSqlString(set)).append(DELIMITER);
        builder.append(object.optBoolean(COLLECTIBLE, false)).append(DELIMITER);
        builder.append(getSqlValue(object, COST));
        builder.append(")");
        return builder.toString();
    }

    public int getDurability() {
        return Integer.parseInt(getValue(object, DURABILITY));
    }

    public int getHealth() {
        return Integer.parseInt(getValue(object, HEALTH));
    }

    public int getAttack() {
        return Integer.parseInt(getValue(object, ATTACK));
    }

    public String getRace() {
        return getValue(object, RACE);
    }

    public String getType() {
        return getValue(object, TYPE);
    }

    private static String getSqlValue(JSONObject object, String key) {
        if (object.has(key)) {
            Object o = object.get(key);
            if (o instanceof String) {
                return Utils.toSqlString((String) o);
            } else {
                return o.toString();
            }
        } else {
            return "NULL";
        }
    }

    private static String getValue(JSONObject object, String key) {
        if (object.has(key)) {
            return object.get(key).toString();
        } else {
            return "NULL";
        }
    }
}
