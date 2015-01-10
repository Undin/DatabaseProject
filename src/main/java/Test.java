import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by warrior on 08.01.15.
 */
public class Test {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String RACE = "race";
    public static final String RARITY = "rarity";
    public static final String TYPE = "type";
    public static final String MECHANICS = "mechanics";
    public static final String HEALTH = "health";
    public static final String COST = "cost";
    public static final String PLAYER_CLASS = "playerClass";

    public static final List<String> EFFECTS = Arrays.asList("Taunt", "HealTarget", "Windfury", "Poisonous", "Charge", "Combo", "Battlecry", "AffectedBySpellPower", "Secret", "Deathrattle", "Silence", "Enrage", "Stealth", "ImmuneToSpellpower", "Spellpower", "Aura", "AdjacentBuff", "Divine Shield", "Freeze");
    public static final List<String> TYPES = Arrays.asList("Minion", "Weapon", "Spell");

    public static final String SQL_DIRECTORY = "./sql/";
    public static final String CARDS_FILE = "cards.sql";
    public static final String EFFECTS_FILE = "effects.sql";
    public static final String HEROES_FILE = "heroes.sql";
    public static final String HERO_CARDS_FILE = "hero_cards.sql";
    public static final String HAS_EFFECT_FILE = "has_effect.sql";

    public static final String EFFECTS_FIRST_LINE = "INSERT INTO effects (effect_id, effect_name) VALUES";
    public static final String HEROES_FIRST_LINE = "INSERT INTO heroes (hero_id, class, hero_name, hero_health) VALUES";
    public static final String CARDS_FIRST_LINE = "INSERT INTO cards (card_id, card_name, description, rarity, type, collectible, cost, health, attack, durability, race) VALUES";
    public static final String HERO_CARDS_FIRST_LINE = "INSERT INTO hero_cards (card_id, hero_id) VALUES";
    public static final String HAS_EFFECT_FIRST_LINE = "INSERT INTO has_effect (card_id, effect_id) VALUES";

    public static void main(String[] args) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader("./AllSets.enUS.json"));
        String allSets = reader.lines().collect(Collectors.joining());
        JSONObject object = new JSONObject(allSets);

        List<Pair<String, String>> heroes = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        List<Pair<Integer, Integer>> heroCards = new ArrayList<>();
        List<Pair<Integer, Integer>> hasEffect = new ArrayList<>();

        Map<String, Integer> classToIndex = new HashMap<>();

        JSONArray basicSet = object.getJSONArray("Basic");
        for (int i = 0; i < basicSet.length(); i++) {
            JSONObject card = basicSet.getJSONObject(i);
            if (card.getString(ID).startsWith("HERO")) {
                heroes.add(Pair.of(card.getString(PLAYER_CLASS), card.getString(NAME)));
                classToIndex.put(card.getString(PLAYER_CLASS), heroes.size());
            }
        }

        for (String setKey : object.keySet()) {
            JSONArray set = object.getJSONArray(setKey);
            for (int i = 0; i < set.length(); i++) {
                JSONObject cardObject = set.getJSONObject(i);
                if (cardObject.has(TYPE) && TYPES.contains(cardObject.getString(TYPE)) && cardObject.has(RARITY) && cardObject.has(NAME) && cardObject.has(COST)) {
                    Card card = new Card(cardObject);
                    cards.add(card);

                    if (cardObject.has(PLAYER_CLASS)) {
                        heroCards.add(Pair.of(card.getId(), classToIndex.get(cardObject.getString(PLAYER_CLASS))));
                    }
                    if (cardObject.has(MECHANICS)) {
                        JSONArray mechanics = cardObject.getJSONArray(MECHANICS);
                        for (int j = 0; j < mechanics.length(); j++) {
                            String effect = mechanics.getString(j);
                            hasEffect.add(Pair.of(card.getId(), EFFECTS.indexOf(effect) + 1));
                        }
                    }
                }
            }
        }

        printEffectInsertion(EFFECTS);
        printHeroesInsertion(heroes);
        printCardsInsertion(cards);
        printHeroCards(heroCards);
        printHasEffect(hasEffect);
    }

    public static void printEffectInsertion(List<String> effects) throws FileNotFoundException {
        List<String> effectsValues = new ArrayList<>();
        for (int i = 0; i < effects.size(); i++) {
            effectsValues.add("    (" + (i + 1) + ", " + toSqlString(effects.get(i)) + ")");
        }
        printInsertion(EFFECTS_FILE, EFFECTS_FIRST_LINE, effectsValues);
    }

    public static void printHeroesInsertion(List<Pair<String, String>> heroes) throws FileNotFoundException {
        List<String> heroesValues = new ArrayList<>();
        for (int i = 0; i < heroes.size(); i++) {
            heroesValues.add(String.format("    (%d, %s, %s, %d)", i + 1, toSqlString(heroes.get(i).first), toSqlString(heroes.get(i).second), 30));
        }
        printInsertion(HEROES_FILE, HEROES_FIRST_LINE, heroesValues);
    }

    public static void printCardsInsertion(List<Card> cards) throws FileNotFoundException {
        printInsertion(CARDS_FILE, CARDS_FIRST_LINE, cards.stream().map(i -> "    " + i.toString()).collect(Collectors.toList()));
    }

    public static void printHeroCards(List<Pair<Integer, Integer>> pairs) throws FileNotFoundException {
        printInsertion(HERO_CARDS_FILE, HERO_CARDS_FIRST_LINE, pairs.stream().map(p -> "    (" + p.first + ", " + p.second + ")").collect(Collectors.toList()));
    }

    public static void printHasEffect(List<Pair<Integer, Integer>> pairs) throws FileNotFoundException {
        printInsertion(HAS_EFFECT_FILE, HAS_EFFECT_FIRST_LINE, pairs.stream().map(p -> "    (" + p.first + ", " + p.second + ")").collect(Collectors.toList()));
    }

    private static void printInsertion(String filename, String firstLine, List<String> values) throws FileNotFoundException {
        try(PrintWriter writer = new PrintWriter(new File(SQL_DIRECTORY, filename))) {
            writer.println(firstLine);
            writer.print(String.join(",\n", values));
            writer.println(";");
        }
    }

    private static String toSqlString(String str) {
        if (str.contains("'")) {
            return "E'" + str.replaceAll("'", "\\\\'") + "'";
        } else {
            return "'" + str + "'";
        }
    }

    private static class Pair<T, S> {
        public T first;
        public S second;

        public Pair(T first, S second) {
            this.first = first;
            this.second = second;
        }

        public static <T, S> Pair<T, S> of(T first, S second) {
            return new Pair<>(first, second);
        }
    }
}
