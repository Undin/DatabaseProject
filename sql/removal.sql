DROP TABLE IF EXISTS hero_statistics RESTRICT;
DROP TABLE IF EXISTS in_deck RESTRICT;
DROP TABLE IF EXISTS has_card RESTRICT;
DROP TABLE IF EXISTS hero_cards RESTRICT;
DROP TABLE IF EXISTS has_effect RESTRICT;
DROP TABLE IF EXISTS decks RESTRICT;
DROP TABLE IF EXISTS cards RESTRICT;
DROP TABLE IF EXISTS players RESTRICT;
DROP TABLE IF EXISTS effects RESTRICT;
DROP TABLE IF EXISTS heroes RESTRICT;

DROP TYPE IF EXISTS rarity_type;
DROP TYPE IF EXISTS card_type;
DROP TYPE IF EXISTS race_type;

DROP DOMAIN IF EXISTS qnt;
DROP DOMAIN IF EXISTS uint;

DROP EXTENSION IF EXISTS citext;
DROP EXTENSION IF EXISTS pgcrypto;