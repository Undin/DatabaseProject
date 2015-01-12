CREATE TYPE rarity_type AS ENUM (
    'Free',
    'Common',
    'Rare',
    'Epic',
    'Legendary');
CREATE TYPE card_type AS ENUM (
    'Minion',
    'Spell',
    'Weapon');
CREATE TYPE race_type AS ENUM (
    'Totem',
    'Demon',
    'Mech',
    'Pirate',
    'Murloc',
    'Beast',
    'Dragon');

CREATE DOMAIN nickname AS VARCHAR(12) CHECK (VALUE ~ E'^[a-zA-Z][a-zA-Z0-9]{2,11}$');
CREATE DOMAIN uint AS INTEGER CHECK (VALUE >= 0);
CREATE DOMAIN qnt AS INTEGER CHECK (VALUE > 0);
CREATE DOMAIN deck_qnt AS INTEGER CHECK (VALUE > 0 AND VALUE <= 2);

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS heroes (
    hero_id     INTEGER PRIMARY KEY,
    class       TEXT        NOT NULL,
    hero_name   TEXT UNIQUE NOT NULL,
    hero_health uint        NOT NULL DEFAULT 30
);

CREATE TABLE IF NOT EXISTS effects (
    effect_id   INTEGER PRIMARY KEY,
    effect_name TEXT UNIQUE NOT NULL
);

CREATE TABLE players (
    player_id   SERIAL PRIMARY KEY,
    player_name nickname UNIQUE NOT NULL,
    money       uint            NOT NULL DEFAULT 0,
    dust        uint            NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cards (
    card_id     SERIAL PRIMARY KEY,
    card_name   TEXT        NOT NULL,
    description TEXT,
    rarity      rarity_type NOT NULL,
    type        card_type   NOT NULL,
    set         TEXT        NOT NULL,
    collectible BOOLEAN     NOT NULL DEFAULT TRUE,
    cost        uint        NOT NULL,
    health      uint,
    attack      uint,
    durability  uint,
    race        race_type
);

CREATE TABLE IF NOT EXISTS decks (
    deck_id   SERIAL PRIMARY KEY,
    deck_name TEXT    NOT NULL,
    player_id INTEGER NOT NULL REFERENCES players ON DELETE CASCADE,
    hero_id   INTEGER NOT NULL REFERENCES heroes ON DELETE CASCADE,
    UNIQUE (player_id, deck_name)
);

CREATE TABLE IF NOT EXISTS has_effect (
    card_id   INTEGER NOT NULL REFERENCES cards ON DELETE CASCADE,
    effect_id INTEGER NOT NULL REFERENCES effects ON DELETE CASCADE,
    PRIMARY KEY (effect_id, card_id)
);

CREATE TABLE IF NOT EXISTS hero_cards (
    card_id INTEGER PRIMARY KEY REFERENCES cards ON DELETE CASCADE,
    hero_id INTEGER NOT NULL REFERENCES heroes ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS has_card (
    card_id   INTEGER NOT NULL REFERENCES cards ON DELETE CASCADE,
    player_id INTEGER NOT NULL REFERENCES players ON DELETE CASCADE,
    quantity  qnt    NOT NULL,
    PRIMARY KEY (player_id, card_id)
);

CREATE TABLE IF NOT EXISTS in_deck (
    card_id  INTEGER NOT NULL REFERENCES cards ON DELETE CASCADE,
    deck_id  INTEGER NOT NULL REFERENCES decks ON DELETE CASCADE,
    quantity deck_qnt     NOT NULL,
    PRIMARY KEY (deck_id, card_id)
);

CREATE TABLE hero_statistics (
    player_id INTEGER NOT NULL REFERENCES players ON DELETE CASCADE,
    hero_id   INTEGER NOT NULL REFERENCES heroes ON DELETE CASCADE,
    expr      uint    NOT NULL DEFAULT 0,
    wins      uint    NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, hero_id)
);


