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
    UNIQUE (deck_name, player_id)
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

CREATE OR REPLACE FUNCTION modified_has_card()
    RETURNS TRIGGER AS $$
DECLARE
    is_collectible BOOLEAN;
BEGIN
    IF (TG_OP = 'INSERT')
    THEN
        SELECT collectible
        FROM cards
        WHERE card_id = NEW.card_id
        INTO is_collectible;

        IF (NOT is_collectible)
        THEN
            RAISE EXCEPTION 'card % is not collectible', NEW.card_id;
        END IF;
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE')
    THEN
        IF (OLD.quantity > NEW.quantity)
        THEN
            UPDATE in_deck
            SET quantity = NEW.quantity
            WHERE card_id = NEW.card_id AND deck_id IN
                                            (SELECT deck_id
                                             FROM decks
                                             WHERE player_id = NEW.player_id);
        END IF;
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE')
    THEN
        DELETE FROM in_deck
        WHERE card_id = OLD.card_id AND deck_id IN
                                        (SELECT deck_id
                                         FROM decks
                                         WHERE player_id = OLD.player_id);
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS has_card_modified ON has_card;
CREATE TRIGGER has_card_modified
BEFORE INSERT OR UPDATE OR DELETE
ON has_card FOR EACH ROW
EXECUTE PROCEDURE modified_has_card();


CREATE OR REPLACE FUNCTION add_card_into_deck()
    RETURNS TRIGGER AS $$
DECLARE
    _player_id    INTEGER;
    _hero_id      INTEGER;
    _quantity     INTEGER;
    _rarity       rarity_type;
    _card_hero_id INTEGER;
BEGIN
    SELECT
        player_id,
        hero_id
    FROM decks
    WHERE deck_id = NEW.deck_id
    INTO _player_id, _hero_id;
    SELECT
        quantity,
        rarity
    FROM has_card
        NATURAL JOIN cards
    WHERE player_id = _player_id
    INTO _quantity, _rarity;
    IF (TG_OP = 'INSERT')
    THEN
        SELECT hero_id
        FROM hero_cards
        WHERE card_id = NEW.card_id
        INTO _card_hero_id;
        IF (_card_hero_id IS NOT NULL AND _card_hero_id <> _hero_id)
        THEN
            RAISE EXCEPTION E'deck\'s hero is %. card\'s hero is %', _hero_id, _card_hero_id;
        END IF;
    END IF;
    IF (_quantity IS NULL OR NEW.quantity > _quantity)
    THEN
        RAISE EXCEPTION E'player % hasn\'t enough card %', _player_id, NEW.card_id;
    END IF;
    IF (NEW.quantity > 1 AND _rarity = 'Legendary')
    THEN
        RAISE EXCEPTION 'Only one legendary card can be in deck';
    END IF;

    RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS card_in_deck ON in_deck;
CREATE TRIGGER card_in_deck
BEFORE INSERT OR UPDATE
ON in_deck FOR EACH ROW
EXECUTE PROCEDURE add_card_into_deck();


CREATE OR REPLACE FUNCTION get_player_id(_player_name CITEXT) RETURNS INTEGER AS $$
DECLARE
    id INTEGER;
BEGIN
    SELECT player_id from players WHERE player_name = _player_name INTO id;
    RETURN id;
END;
$$ LANGUAGE 'plpgsql';

