package com.warrior.databaseproject;

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
    public static final String COLLECTIBLE = "collectible";

    public static final List<String> EFFECTS = Arrays.asList("Taunt", "HealTarget", "Windfury", "Poisonous", "Charge", "Combo", "Battlecry", "AffectedBySpellPower", "Secret", "Deathrattle", "Silence", "Enrage", "Stealth", "ImmuneToSpellpower", "Spellpower", "Aura", "AdjacentBuff", "Divine Shield", "Freeze");
    public static final List<String> TYPES = Arrays.asList("Minion", "Weapon", "Spell");

    public static final String DECKS_DIR = "decks";
    public static final String STATISTICS_FILE = "statistics.txt";
    public static final String SQL_DIRECTORY = "./sql/";
    public static final String CARDS_FILE = "cards.sql";
    public static final String EFFECTS_FILE = "effects.sql";
    public static final String HEROES_FILE = "heroes.sql";
    public static final String HERO_CARDS_FILE = "hero_cards.sql";
    public static final String HAS_EFFECT_FILE = "has_effect.sql";
    public static final String PLAYERS_FILE = "players.sql";
    public static final String DECKS_FILE = "decks.sql";
    public static final String IN_DECK_FILE = "in_deck.sql";
    public static final String HAS_CARD_FILE = "has_card.sql";
    public static final String HERO_STATISTICS_FILE = "hero_statistics.sql";

    public static final String HERO_CARDS_FIRST_LINE = "INSERT INTO hero_cards (card_id, hero_id) VALUES";
    public static final String HAS_EFFECT_FIRST_LINE = "INSERT INTO has_effect (card_id, effect_id) VALUES";
    public static final String IN_DECK_FIRST_LINE = "INSERT INTO in_deck (card_id, deck_id, quantity) VALUES";
    public static final String HAS_CARD_FIRST_LINE = "INSERT INTO has_card (card_id, player_id, quantity) VALUES";

    private static Map<String, Card> cardNameToCollectibleCards = new HashMap<>();
    private static Map<String, Hero> classNameToHero = new HashMap<>();
    private static Map<String, Effect> effectNameToEffect = new HashMap<>();

    public static void main(String[] args) throws IOException {
        JSONObject object = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("./AllSets.enUS.json"))) {
            String allSets = reader.lines().collect(Collectors.joining());
            object = new JSONObject(allSets);
        }

        List<Effect> effects = new ArrayList<>();
        List<Hero> heroes = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        List<Player> players = new ArrayList<>();
        List<Deck> decks = new ArrayList<>();


        for (String effectName : EFFECTS) {
            Effect effect = new Effect(effectName);
            effects.add(effect);
            effectNameToEffect.put(effectName, effect);
        }

        JSONArray basicSet = object.getJSONArray("Basic");
        for (int i = 0; i < basicSet.length(); i++) {
            JSONObject card = basicSet.getJSONObject(i);
            if (card.getString(ID).startsWith("HERO")) {
                Hero hero = new Hero(card.getString(NAME), card.getString(PLAYER_CLASS));
                heroes.add(hero);
                classNameToHero.put(card.getString(PLAYER_CLASS), hero);
            }
        }

        for (String setKey : object.keySet()) {
            JSONArray set = object.getJSONArray(setKey);
            for (int i = 0; i < set.length(); i++) {
                JSONObject cardObject = set.getJSONObject(i);
                if (cardObject.has(TYPE) && TYPES.contains(cardObject.getString(TYPE)) && cardObject.has(RARITY) && cardObject.has(NAME) && cardObject.has(COST)) {
                    Card card = new Card(cardObject, setKey);
                    cards.add(card);
                    if (card.isCollectible()) {
                        cardNameToCollectibleCards.put(card.getName(), card);
                    }

                    if (cardObject.has(PLAYER_CLASS)) {
                        classNameToHero.get(cardObject.getString(PLAYER_CLASS)).addCard(card);
                    }
                    if (cardObject.has(MECHANICS)) {
                        JSONArray mechanics = cardObject.getJSONArray(MECHANICS);
                        for (int j = 0; j < mechanics.length(); j++) {
                            String effectName = mechanics.getString(j);
                            Effect effect = effectNameToEffect.get(effectName);
                            if (effect != null) {
                                card.addEffect(effect);
                            }
                        }
                    }
                }
            }
        }

        File playersDir = new File("./players");
        for (File playerDir : playersDir.listFiles()) {
            String playerName = playerDir.getName();
            Player player = new Player(playerName);
            players.add(player);
            extractStatistics(player, playerDir);
            File decksDir = new File(playerDir, DECKS_DIR);
            for (File deckFile : decksDir.listFiles()) {
                String deckName = deckFile.getName().replaceAll("(.*)\\..*", "$1");
                try (BufferedReader reader = new BufferedReader(new FileReader(deckFile))) {
                    String className = reader.readLine().trim();
                    Hero hero = classNameToHero.get(className);
                    Deck deck = new Deck(deckName, hero);
                    decks.add(deck);
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        int qnt = Integer.parseInt(line.substring(0, 1));
                        String cardName = line.substring(2).trim();
                        Card card = cardNameToCollectibleCards.get(cardName);
                        deck.addCard(card, qnt);
                    }
                    player.addDeck(deck);
                }
            }
        }

        // effects
        printInsertion(effects, EFFECTS_FILE, Effect.EFFECT_FIRST_LINE);
        // heroes
        printInsertion(heroes, HEROES_FILE, Hero.HEROES_FIRST_LINE);
        // cards
        printInsertion(cards, CARDS_FILE, Card.INSERT_FIRST_LINE);
        // players
        printInsertion(players, PLAYERS_FILE, Player.PLAYERS_FIRST_LINE);
        // decks
        printInsertion(decks, DECKS_FILE, Deck.DECK_FIRST_LINE);
        // has effect
        printHasEffect(cards);
        // hero cards
        printHeroCards(heroes);
        // has card
        printHasCard(players);
        // in deck
        printInDeck(decks);
        // hero statistics
        printHeroStatistics(players);
    }

    private static void extractStatistics(Player player, File playerDir) throws IOException {
        File statisticsFile = new File(playerDir, STATISTICS_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(statisticsFile))) {
            String[] line = reader.readLine().split(" "); // rank starts money dust
            int rank = Integer.parseInt(line[0]);
            int stars = Integer.parseInt(line[1]);
            int money = Integer.parseInt(line[2]);
            int dust = Integer.parseInt(line[3]);
            player.setRank(rank);
            player.setStars(stars);
            player.setMoney(money);
            player.setDust(dust);
            reader.lines().map(s -> s.split(" ")).forEach(arr -> {
                        Hero hero = classNameToHero.get(arr[0]);
                        int level = Integer.parseInt(arr[1]);
                        int wins = Integer.parseInt(arr[2]);
                        Statistics statistics = new Statistics(hero, level, wins);
                        player.addStatistics(statistics);
                    }
            );
        }
    }

    public static void printHeroCards(List<Hero> heroes) throws FileNotFoundException {
        List<String> pairs = new ArrayList<>();
        for (Hero hero : heroes) {
            pairs.addAll(hero.getCards().stream().map(card -> "    " + Utils.toString(card.getId(), hero.getId())).collect(Collectors.toList()));
        }
        printInsertion(HERO_CARDS_FILE, HERO_CARDS_FIRST_LINE, pairs);
    }

    public static void printHasEffect(List<Card> cards) throws FileNotFoundException {
        List<String> pairs = new ArrayList<>();
        for (Card card : cards) {
            pairs.addAll(card.getEffects().stream().map(effect -> "    " + Utils.toString(card.getId(), effect.getId())).collect(Collectors.toList()));
        }
        printInsertion(HAS_EFFECT_FILE, HAS_EFFECT_FIRST_LINE, pairs);
    }

    public static void printInDeck(List<Deck> decks) throws FileNotFoundException {
        List<String> pairs = new ArrayList<>();
        for (Deck deck : decks) {
            Map<Card, Integer> cards = deck.getCards();
            pairs.addAll(cards.keySet().stream().map(card -> "    " + Utils.toString(card.getId(), deck.getId(), cards.get(card))).collect(Collectors.toList()));
        }
        printInsertion(IN_DECK_FILE, IN_DECK_FIRST_LINE, pairs);
    }

    public static void printHasCard(List<Player> players) throws FileNotFoundException {
        List<String> pairs = new ArrayList<>();
        for (Player player : players) {
            Map<Card, Integer> cards = player.getCards();
            pairs.addAll(cards.keySet().stream().map(card -> "    " + Utils.toString(card.getId(), player.getId(), cards.get(card))).collect(Collectors.toList()));
        }
        printInsertion(HAS_CARD_FILE, HAS_CARD_FIRST_LINE, pairs);
    }

    public static void printHeroStatistics(List<Player> players) throws FileNotFoundException {
        List<String> pairs = new ArrayList<>();
        for (Player player : players) {
            List<Statistics> statistics = player.getStatistics();
            pairs.addAll(statistics.stream().map(st -> "    " + st).collect(Collectors.toList()));
        }
        printInsertion(HERO_STATISTICS_FILE, Statistics.HERO_STATISTICS_FIRST_LINE, pairs);
    }

    private static void printInsertion(List<? extends Values> values, String filename, String firstLine) throws FileNotFoundException {
        printInsertion(filename, firstLine, values.stream().map(v -> "    " + v).collect(Collectors.toList()));
    }

    private static void printInsertion(String filename, String firstLine, List<String> values) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(new File(SQL_DIRECTORY, filename))) {
            writer.println(firstLine);
            writer.print(String.join(",\n", values));
            writer.println(";");
        }
    }
}
