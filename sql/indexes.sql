CREATE INDEX card_name_index ON cards USING btree (card_name); -- hash
CREATE INDEX card_rarity_index ON cards USING btree (rarity);
CREATE INDEX card_cost_index ON cards USING btree (cost);
CREATE INDEX card_set_index ON cards USING btree (set); -- hash
CREATE INDEX card_race_index ON cards USING btree (race); -- hash
CREATE INDEX card_type_index ON cards USING btree (type); -- hash

CREATE INDEX deck_name_index ON decks USING btree (deck_name); -- hash
CREATE INDEX deck_hero_id_index ON decks USING btree (hero_id); -- hash

CREATE INDEX has_card_card_id_index ON has_card USING btree (card_id);

CREATE INDEX has_effect_card_id_index ON has_effect USING btree (card_id);

CREATE INDEX hero_cards_hero_id_index ON hero_cards USING btree (hero_id);

CREATE INDEX heroes_class_index ON heroes USING btree (class); -- hash

CREATE INDEX in_deck_card_id_index ON in_deck USING btree (card_id);
