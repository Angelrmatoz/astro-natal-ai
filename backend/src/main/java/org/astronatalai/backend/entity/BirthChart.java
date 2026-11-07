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
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "birth_charts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BirthChart {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNac;

    @Column(name = "hora_nac")
    private LocalTime horaNac;

    @Column(name = "time_known", nullable = false)
    private boolean timeKnown;

    @Column(nullable = false, length = 160)
    private String ciudad;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(nullable = false, length = 64)
    private String timezone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_carta", nullable = false, columnDefinition = "jsonb")
    private JsonNode datosCarta;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    public BirthChart(User user) {
        this.user = user;
    }

    public void update(String nombre,
                       LocalDate fechaNac,
                       LocalTime horaNac,
                       boolean timeKnown,
                       String ciudad,
                       double latitud,
                       double longitud,
                       String timezone,
                       JsonNode datosCarta) {
        this.nombre = nombre;
        this.fechaNac = fechaNac;
        this.horaNac = horaNac;
        this.timeKnown = timeKnown;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.timezone = timezone;
        this.datosCarta = datosCarta;
    }
}
