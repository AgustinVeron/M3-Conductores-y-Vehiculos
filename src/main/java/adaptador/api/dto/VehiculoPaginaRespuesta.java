package m3.adaptador.api.dto;

import java.util.List;

/**
 * Envoltorio para listas de vehículos según el esquema VehiclePage del contrato OpenAPI.
 */
public record VehiculoPaginaRespuesta(
    List<VehiculoRespuesta> items,
    String nextCursor
) {
}