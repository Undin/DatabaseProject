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


CREATE DOMAIN uint AS INTEGER CHECK (VALUE >= 0);
CREATE DOMAIN qnt AS INTEGER CHECK (VALUE >= 0 AND VALUE <= 2);

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE heroes (
    id     SERIAL PRIMARY KEY,
    name   TEXT UNIQUE NOT NULL,
    health uint        NOT NULL DEFAULT 30
);

CREATE TABLE effects (
    id   SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE players (
    id            SERIAL PRIMARY KEY,
    name          CITEXT UNIQUE NOT NULL,
    email         CITEXT UNIQUE NOT NULL,
    password_hash TEXT          NOT NULL,
    money         uint          NOT NULL DEFAULT 0,
    dust          uint          NOT NULL DEFAULT 0
);

CREATE TABLE cards (
    id          SERIAL PRIMARY KEY,
    name        TEXT UNIQUE NOT NULL,
    description TEXT,
    rarity      rarity_type NOT NULL,
    type        card_type   NOT NULL,
    collectible BOOLEAN     NOT NULL DEFAULT TRUE,
    cost        uint        NOT NULL,
    health      uint,
    attack      uint,
    durability  uint,
    race        race_type
);

CREATE TABLE decks (
    id        SERIAL PRIMARY KEY,
    name      TEXT    NOT NULL,
    player_id INTEGER NOT NULL REFERENCES players,
    hero_id   INTEGER NOT NULL REFERENCES heroes,
    UNIQUE (name, player_id)
);

CREATE TABLE has_effect (
    card_id   INTEGER NOT NULL REFERENCES cards,
    effect_id INTEGER NOT NULL REFERENCES effects,
    PRIMARY KEY (effect_id, card_id)
);

CREATE TABLE hero_cards (
    hero_id INTEGER PRIMARY KEY REFERENCES heroes,
    card_id INTEGER NOT NULL REFERENCES cards
);

CREATE TABLE has_card (
    card_id   INTEGER NOT NULL REFERENCES cards,
    player_id INTEGER NOT NULL REFERENCES players,
    quantity  uint    NOT NULL,
    PRIMARY KEY (player_id, card_id)
);

CREATE TABLE in_deck (
    card_id INTEGER NOT NULL REFERENCES cards,
    deck_id INTEGER NOT NULL REFERENCES decks,
    PRIMARY KEY (deck_id, card_id)
);

CREATE TABLE hero_statistics (
    player_id INTEGER NOT NULL REFERENCES players,
    hero_id   INTEGER NOT NULL REFERENCES heroes,
    expr      uint    NOT NULL DEFAULT 0,
    wins      uint    NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, hero_id)
);

