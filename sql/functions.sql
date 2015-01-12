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


CREATE OR REPLACE FUNCTION get_player_id(_player_name CITEXT)
    RETURNS INTEGER AS $$
DECLARE
    id INTEGER;
BEGIN
    SELECT player_id
    FROM players
    WHERE player_name = _player_name
    INTO id;
    RETURN id;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION create_player_statistics()
    RETURNS TRIGGER AS $$
DECLARE
    hero RECORD;
BEGIN
    FOR hero IN SELECT (hero_id)
                FROM heroes LOOP
        INSERT INTO hero_statistics (player_id, hero_id) VALUES (NEW.player_id, hero.hero_id);
    END LOOP;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS player_insertion ON players;
CREATE TRIGGER player_insertion
AFTER INSERT
ON players FOR EACH ROW
EXECUTE PROCEDURE create_player_statistics();