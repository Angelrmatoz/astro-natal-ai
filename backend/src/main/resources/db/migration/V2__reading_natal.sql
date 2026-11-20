ALTER TABLE readings DROP CONSTRAINT ck_readings_tipo;

ALTER TABLE readings ADD CONSTRAINT ck_readings_tipo
    CHECK (tipo IN ('day', 'week', 'month', 'natal'));
