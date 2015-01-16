package com.warrior.databaseproject;

/**
 * Created by warrior on 13.01.15.
 */
public class Statistics {

    public static final String HERO_STATISTICS_FIRST_LINE = "INSERT INTO hero_statistics(player_id, hero_id, level, expr, wins) VALUES";

    private final Hero hero;
    private final int level;
    private final int wins;

    private Player player;

    public Statistics(Hero hero, int level, int wins) {
        this.hero = hero;
        this.level = level;
        this.wins = wins;
    }

    public Hero getHero() {
        return hero;
    }

    public int getLevel() {
        return level;
    }

    public int getWins() {
        return wins;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return Utils.toString(player.getId(), hero.getId(), level, 0, wins);
    }
}
