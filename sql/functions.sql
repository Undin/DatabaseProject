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

CREATE OR REPLACE FUNCTION create_player_statistics()
    RETURNS TRIGGER AS $$
DECLARE
    hero RECORD;
BEGIN
    FOR hero IN SELECT (hero_id)
                FROM heroes LOOP
        INSERT INTO hero_statistics (player_id, hero_id) VALUES (NEW.player_id, hero.hero_id);
    END LOOP;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS player_insertion ON players;
CREATE TRIGGER player_insertion
AFTER INSERT
ON players FOR EACH ROW
EXECUTE PROCEDURE create_player_statistics();

-- возвращает id игрока по его имени
CREATE OR REPLACE FUNCTION get_player_id(_player_name nickname)
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

-- возвращает все карты игрока по его имени
CREATE OR REPLACE FUNCTION get_all_player_cards(_player_name nickname)
    RETURNS TABLE(quantity qnt, card_id INTEGER, card_name TEXT, description TEXT, rarity rarity_type, set TEXT, collectible BOOLEAN, cost uint) AS $$
SELECT
    has_card.quantity,
    cards.card_id,
    cards.card_name,
    cards.description,
    cards.rarity,
    cards.set,
    cards.collectible,
    cards.cost
FROM cards
    NATURAL JOIN has_card
    NATURAL JOIN players
WHERE players.player_name = _player_name;
$$ LANGUAGE 'sql';

-- возращает все колоды игрока по его имени
CREATE OR REPLACE FUNCTION gel_all_player_decks(_player_name nickname)
    RETURNS TABLE(deck_id INTEGER, deck_name TEXT, class TEXT) AS $$
SELECT
    decks.deck_id,
    decks.deck_name,
    heroes.class
FROM decks
    NATURAL JOIN players
    NATURAL JOIN heroes
WHERE players.player_name = _player_name;
$$ LANGUAGE 'sql';

-- возвращает все карты, содержащиеся в колоде игрока
CREATE OR REPLACE FUNCTION gel_all_deck_cards(_player_name nickname, _deck_name TEXT)
    RETURNS TABLE(quantity deck_qnt, card_id INTEGER, card_name TEXT, description TEXT, rarity rarity_type, set TEXT, collectible BOOLEAN, cost uint) AS $$
SELECT
    in_deck.quantity,
    cards.card_id,
    cards.card_name,
    cards.description,
    cards.rarity,
    cards.set,
    cards.collectible,
    cards.cost
FROM cards
    NATURAL JOIN in_deck
    NATURAL JOIN decks
WHERE decks.player_id = get_player_id(_player_name) AND decks.deck_name = _deck_name
ORDER BY cards.cost ASC;
$$ LANGUAGE 'sql';

-- выдает информацию о игроке по его имени
CREATE OR REPLACE FUNCTION get_player(_player_name nickname)
    RETURNS TABLE(player_id INTEGER, player_name nickname, rank rank_type, stars uint, money uint, dust uint) AS $$
SELECT *
FROM players
WHERE players.player_name = _player_name;
$$ LANGUAGE 'sql';

-- выдает список игроков, отсортированных по их текущему рангу и количеству звезд
CREATE OR REPLACE FUNCTION get_rank_list()
    RETURNS TABLE(player_id INTEGER, player_name nickname, rank rank_type, stars uint) AS $$
SELECT
    players.player_id,
    players.player_name,
    players.rank,
    players.stars
FROM players
ORDER BY players.rank ASC, players.stars DESC;
$$ LANGUAGE 'sql';


