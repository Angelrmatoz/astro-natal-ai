package org.astronatalai.backend.dto;

public record GeocodingResult(String ciudad, double latitud, double longitud, String timezone) {
}
