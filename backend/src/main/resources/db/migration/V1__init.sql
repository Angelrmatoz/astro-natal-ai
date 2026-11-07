CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE birth_charts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    fecha_nac DATE NOT NULL,
    hora_nac TIME,
    time_known BOOLEAN NOT NULL,
    ciudad VARCHAR(160) NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    datos_carta JSONB NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_birth_charts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_birth_charts_user UNIQUE (user_id),
    CONSTRAINT ck_birth_charts_hora CHECK (time_known = (hora_nac IS NOT NULL))
);

CREATE TABLE readings (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    rango_inicio DATE NOT NULL,
    rango_fin DATE NOT NULL,
    transitos_snapshot JSONB NOT NULL,
    contenido_texto TEXT NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_readings_chart FOREIGN KEY (chart_id) REFERENCES birth_charts (id) ON DELETE CASCADE,
    CONSTRAINT ck_readings_tipo CHECK (tipo IN ('day', 'week', 'month')),
    CONSTRAINT ck_readings_rango CHECK (rango_fin >= rango_inicio),
    CONSTRAINT uq_readings_chart_tipo_rango UNIQUE (chart_id, tipo, rango_inicio)
);

CREATE INDEX idx_readings_chart_rango ON readings (chart_id, rango_inicio DESC);
