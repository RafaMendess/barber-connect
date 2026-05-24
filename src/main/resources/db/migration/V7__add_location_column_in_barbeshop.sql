ALTER TABLE barbearia
DROP COLUMN latitude;

ALTER TABLE barbearia
DROP COLUMN longitude;

ALTER TABLE barbearia
    ADD COLUMN localizacao GEOGRAPHY(POINT, 4326);