Версия базы данных - PostgreSQL 9.4.0

models.pdf содержит ER и PD модели.

Для создания и заполнения данными базы данных запустить SQL скрипты из директории sql в следующем порядке:

    creation.sql
    indexes.sql
    functions.sql
    cards.sql
    effects.sql
    players.sql
    heroes.sql
    has_effect.sql
    has_card.sql
    decks.sql
    in_deck.sql
    hero_statistics.sql

где:
    creation.sql - создание таблиц
    indexes.sql - создание индексов
    functions.sql - создание триггеров и функций
    остальные скрипты заполняют базу данных данными соответствующие им таблицы.