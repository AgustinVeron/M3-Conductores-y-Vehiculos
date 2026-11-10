package m3.adaptador.web;

import m3.dominio.Documento;
import m3.dominio.DocumentoTipo;
import m3.dominio.Vehiculo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * ViewModel de sólo presentación para la revisión administrativa de documentos.
 *
 * <p>Agrupa la licencia del conductor y los documentos de cada vehículo, y expone
 * nombres, estados y fechas ya formateados para Thymeleaf. No modifica reglas de
 * negocio: se construye a partir de los objetos de dominio que ya entrega
 * {@code ConductorEspacioTrabajoServicio}.</p>
 */
public record OperadorRevisionVistaModelo(
        List<DocumentCard> driverDocuments,
        List<VehicleGroup> vehicleGroups,
        List<HistoryRow> history
) {

    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static OperadorRevisionVistaModelo from(List<Vehiculo> vehicles, List<Documento> documents) {
        List<Vehiculo> safeVehicles = vehicles != null ? vehicles : List.of();
        List<Documento> safeDocuments = documents != null ? documents : List.of();

        Map<UUID, Vehiculo> vehiclesById = new LinkedHashMap<>();
        for (Vehiculo vehicle : safeVehicles) {
            vehiclesById.put(vehicle.getId(), vehicle);
        }

        List<DocumentCard> driverDocuments = new ArrayList<>();
        List<HistoryRow> history = new ArrayList<>();
        Map<UUID, List<DocumentCard>> cardsByVehicle = new LinkedHashMap<>();
        Map<UUID, Vehiculo> unknownVehicles = new LinkedHashMap<>();

        for (Documento document : safeDocuments) {
            UUID vehicleId = document.getVehicleId();
            Vehiculo vehicle = vehicleId != null ? vehiclesById.get(vehicleId) : null;
            if (vehicleId != null && vehicle == null) {
                unknownVehicles.putIfAbsent(vehicleId, null);
            }
            history.add(historyRow(document, vehicle));

            if (!document.isCurrent()) {
                continue;
            }
            if (document.getDocumentType() == DocumentoTipo.LICENSE || vehicleId == null) {
                driverDocuments.add(card(document, null));
                continue;
            }
            cardsByVehicle.computeIfAbsent(vehicleId, key -> new ArrayList<>())
                    .add(card(document, vehicleContext(vehicle)));
        }

        List<VehicleGroup> groups = new ArrayList<>();
        for (Vehiculo vehicle : safeVehicles) {
            groups.add(group(vehicle, cardsByVehicle.getOrDefault(vehicle.getId(), List.of())));
        }
        for (UUID unknownId : unknownVehicles.keySet()) {
            groups.add(new VehicleGroup(
                    unknownId,
                    "Vehículo no identificado",
                    null,
                    null,
                    null,
                    "PENDING",
                    statusLabel("PENDING"),
                    List.copyOf(cardsByVehicle.getOrDefault(unknownId, List.of()))
            ));
        }

        return new OperadorRevisionVistaModelo(
                List.copyOf(driverDocuments), List.copyOf(groups), List.copyOf(history));
    }

    private static HistoryRow historyRow(Documento document, Vehiculo vehicle) {
        Instant validUntil = document.getValidUntil();
        return new HistoryRow(
                document.getId(),
                document.getDocumentType().name(),
                typeLabel(document.getDocumentType()),
                document.getRevision(),
                document.getStatus() != null ? document.getStatus().name() : "PENDING",
                statusLabel(document.getStatus() != null ? document.getStatus().name() : "PENDING"),
                validUntil != null ? DATE.format(validUntil.atZone(ZONE)) : "-",
                document.isCurrent(),
                vehicle != null ? vehicleTitle(vehicle) + " · " + vehicle.getPlate() : "Conductor"
        );
    }

    private static VehicleGroup group(Vehiculo vehicle, List<DocumentCard> documents) {
        return new VehicleGroup(
                vehicle.getId(),
                vehicleTitle(vehicle),
                vehicle.getPlate(),
                vehicleTypeLabel(vehicle.getVehicleType()),
                vehicle.getModelYear(),
                vehicle.getQualificationStatus(),
                statusLabel(vehicle.getQualificationStatus()),
                List.copyOf(documents)
        );
    }

    private static DocumentCard card(Documento document, String vehicleContext) {
        Instant validUntil = document.getValidUntil();
        Instant reviewedAt = document.getReviewedAt();
        return new DocumentCard(
                document.getId(),
                document.getDocumentType().name(),
                typeLabel(document.getDocumentType()),
                document.getRevision(),
                document.isCurrent(),
                document.getStatus() != null ? document.getStatus().name() : "PENDING",
                statusLabel(document.getStatus() != null ? document.getStatus().name() : "PENDING"),
                validUntil != null ? DATE.format(validUntil.atZone(ZONE)) : "-",
                vehicleContext,
                blankToNull(document.getRejectionReason()),
                reviewedAt != null ? DATE_TIME.format(reviewedAt.atZone(ZONE)) : null,
                blankToNull(document.getReviewedBy())
        );
    }

    private static String vehicleTitle(Vehiculo vehicle) {
        if (vehicle == null) {
            return "Vehículo";
        }
        String make = vehicle.getMake() != null ? vehicle.getMake().trim() : "";
        String model = vehicle.getModel() != null ? vehicle.getModel().trim() : "";
        String title = (make + " " + model).trim();
        return title.isBlank() ? "Vehículo" : title;
    }

    private static String vehicleContext(Vehiculo vehicle) {
        if (vehicle == null) {
            return null;
        }
        String plate = vehicle.getPlate() != null ? vehicle.getPlate() : "";
        return (vehicleTitle(vehicle) + " · " + plate).trim();
    }

    public static String typeLabel(DocumentoTipo type) {
        if (type == null) {
            return "Documento";
        }
        return switch (type) {
            case LICENSE -> "Licencia de conducir";
            case INSURANCE -> "Seguro";
            case VEHICLE_REGISTRATION -> "Documentación del vehículo";
        };
    }

    public static String statusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Pendiente";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "APPROVED" -> "Aprobado";
            case "PENDING" -> "Pendiente";
            case "OBSERVED" -> "Observado";
            case "REJECTED" -> "Rechazado";
            case "SUSPENDED" -> "Suspendido";
            default -> status;
        };
    }

    private static String vehicleTypeLabel(String type) {
        if (type == null || type.isBlank()) {
            return "Vehículo";
        }
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "AUTO" -> "Auto";
            case "MOTO" -> "Moto";
            default -> type;
        };
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record DocumentCard(
            UUID id,
            String typeCode,
            String typeLabel,
            int revision,
            boolean current,
            String status,
            String statusLabel,
            String validUntil,
            String vehicleContext,
            String rejectionReason,
            String reviewedAt,
            String reviewedBy
    ) {
        // Accesores JavaBean para Thymeleaf (mismo criterio que ConductorPerfilVistaModelo).
        public UUID getId() { return id; }
        public String getTypeCode() { return typeCode; }
        public String getTypeLabel() { return typeLabel; }
        public int getRevision() { return revision; }
        public boolean isCurrent() { return current; }
        public String getStatus() { return status; }
        public String getStatusLabel() { return statusLabel; }
        public String getValidUntil() { return validUntil; }
        public String getVehicleContext() { return vehicleContext; }
        public String getRejectionReason() { return rejectionReason; }
        public String getReviewedAt() { return reviewedAt; }
        public String getReviewedBy() { return reviewedBy; }
    }

    public record VehicleGroup(
            UUID id,
            String title,
            String plate,
            String typeLabel,
            Integer modelYear,
            String qualificationStatus,
            String qualificationStatusLabel,
            List<DocumentCard> documents
    ) {
        // Accesores JavaBean para Thymeleaf.
        public UUID getId() { return id; }
        public String getTitle() { return title; }
        public String getPlate() { return plate; }
        public String getTypeLabel() { return typeLabel; }
        public Integer getModelYear() { return modelYear; }
        public String getQualificationStatus() { return qualificationStatus; }
        public String getQualificationStatusLabel() { return qualificationStatusLabel; }
        public List<DocumentCard> getDocuments() { return documents; }

        public String getMetaLine() {
            StringBuilder meta = new StringBuilder();
            if (plate != null && !plate.isBlank()) {
                meta.append(plate);
            }
            if (typeLabel != null && !typeLabel.isBlank()) {
                if (!meta.isEmpty()) meta.append(" · ");
                meta.append(typeLabel);
            }
            if (modelYear != null) {
                if (!meta.isEmpty()) meta.append(" · ");
                meta.append(modelYear);
            }
            return meta.toString();
        }
    }

    public record HistoryRow(
            UUID id,
            String typeCode,
            String typeLabel,
            int revision,
            String status,
            String statusLabel,
            String validUntil,
            boolean current,
            String ownerLabel
    ) {
        // Accesores JavaBean para Thymeleaf.
        public UUID getId() { return id; }
        public String getTypeCode() { return typeCode; }
        public String getTypeLabel() { return typeLabel; }
        public int getRevision() { return revision; }
        public String getStatus() { return status; }
        public String getStatusLabel() { return statusLabel; }
        public String getValidUntil() { return validUntil; }
        public boolean isCurrent() { return current; }
        public String getOwnerLabel() { return ownerLabel; }
    }
}
