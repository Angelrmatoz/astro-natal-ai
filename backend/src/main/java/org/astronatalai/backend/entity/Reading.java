package org.astronatalai.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "readings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reading {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chart_id", nullable = false)
    private BirthChart chart;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(name = "rango_inicio", nullable = false)
    private LocalDate rangoInicio;

    @Column(name = "rango_fin", nullable = false)
    private LocalDate rangoFin;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transitos_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode transitosSnapshot;

    @Column(name = "contenido_texto", nullable = false, columnDefinition = "text")
    private String contenidoTexto;

    @Column(nullable = false, length = 80)
    private String modelo;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    public Reading(BirthChart chart,
                   String tipo,
                   LocalDate rangoInicio,
                   LocalDate rangoFin,
                   JsonNode transitosSnapshot,
                   String contenidoTexto,
                   String modelo) {
        this.chart = chart;
        this.tipo = tipo;
        this.rangoInicio = rangoInicio;
        this.rangoFin = rangoFin;
        this.transitosSnapshot = transitosSnapshot;
        this.contenidoTexto = contenidoTexto;
        this.modelo = modelo;
    }
}
