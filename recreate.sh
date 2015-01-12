#!/bin/sh

sqlDir="/Users/warrior/Programming/DatabaseProject/sql"
psql HSDatabase -f "$sqlDir/removal.sql"
psql HSDatabase -f "$sqlDir/creation.sql"
psql HSDatabase -f "$sqlDir/functions.sql"
psql HSDatabase -f "$sqlDir/cards.sql"
psql HSDatabase -f "$sqlDir/effects.sql"
psql HSDatabase -f "$sqlDir/players.sql"
psql HSDatabase -f "$sqlDir/heroes.sql"
psql HSDatabase -f "$sqlDir/has_effect.sql"
psql HSDatabase -f "$sqlDir/has_card.sql"
psql HSDatabase -f "$sqlDir/decks.sql"
psql HSDatabase -f "$sqlDir/in_deck.sql"