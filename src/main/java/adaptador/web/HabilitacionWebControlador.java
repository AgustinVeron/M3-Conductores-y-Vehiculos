package m3.adaptador.web;

import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.entrada.VerificarConductorAptitudUsoCaso;
import m3.dominio.elegibilidad.ElegibilidadResultado;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/ui/qualification")
public class HabilitacionWebControlador {

    private final VerificarConductorAptitudUsoCaso checkDriverAptitudeUseCase;

    public HabilitacionWebControlador(VerificarConductorAptitudUsoCaso checkDriverAptitudeUseCase) {
        this.checkDriverAptitudeUseCase = Objects.requireNonNull(checkDriverAptitudeUseCase);
    }

    private static final Map<String, String> REASON_DESCRIPTIONS = Map.ofEntries(
        Map.entry("REGISTRATION_MISSING","No tenés la cédula del vehículo registrada"),
        Map.entry("REGISTRATION_NOT_APPROVED","La cédula del vehículo no está aprobada"),
        Map.entry("REGISTRATION_EXPIRED","La cédula del vehículo está vencida"),
        Map.entry("REGISTRATION_NOT_YET_VALID","La cédula del vehículo todavía no está vigente"),
        Map.entry("LICENSE_NOT_YET_VALID","La licencia todavía no está vigente"),
        Map.entry("INSURANCE_NOT_YET_VALID","El seguro todavía no está vigente"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.DRIVER_NOT_APPROVED, "Tu habilitación como conductor no está aprobada"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.DRIVER_SUSPENDED, "Tu cuenta de conductor está suspendida"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.NO_VEHICLE_SELECTED, "No tenés ningún vehículo registrado"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.VEHICLE_NOT_APPROVED, "Tu vehículo no está aprobado"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.VEHICLE_SUSPENDED, "Tu vehículo está suspendido"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.LICENSE_MISSING, "No tenés una licencia registrada"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.LICENSE_NOT_APPROVED, "Tu licencia no está aprobada"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.LICENSE_EXPIRED, "Tu licencia está vencida"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.INSURANCE_MISSING, "No tenés seguro registrado para tu vehículo"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.INSURANCE_NOT_APPROVED, "Tu seguro no está aprobado"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.INSURANCE_EXPIRED, "Tu seguro está vencido"),
        Map.entry(m3.dominio.elegibilidad.ElegibilidadMotivo.SERVICE_TYPE_NOT_PERMITTED, "El servicio solicitado no está permitido")
    );

    @GetMapping({"", "/"})
    public String aptitude(@RequestParam(name = "driverId", required = false) String driverIdParam, Model model) {
        model.addAttribute("reasonDescriptions", REASON_DESCRIPTIONS);
        model.addAttribute("eligible", false);
        model.addAttribute("noProfile", false);

        try {
            ElegibilidadResultado result;
            if (driverIdParam != null && !driverIdParam.isBlank()) {
                result = checkDriverAptitudeUseCase.execute(UUID.fromString(driverIdParam.trim()));
            } else {
                result = checkDriverAptitudeUseCase.executeForSelf();
            }
            model.addAttribute("eligible", result.eligible());
            model.addAttribute("reasons", result.reasons());
            model.addAttribute("checkedAt", result.checkedAt());
            model.addAttribute("validUntil", result.validUntil());
        } catch (ConductorNoEncontradoExcepcion e) {
            model.addAttribute("noProfile", true);
        } catch (ConductorAccesoDenegadoExcepcion e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("errorType", "ACCESS_DENIED");
            return "error";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Identificador de conductor con formato inválido.");
            model.addAttribute("errorType", "BAD_REQUEST");
            return "error";
        }

        return "habilitacion/aptitud";
    }
}
