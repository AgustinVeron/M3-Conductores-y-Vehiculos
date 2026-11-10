package m3.adaptador.web;

import m3.aplicacion.casodeuso.RegistrarConductorLicenciaComando;
import m3.aplicacion.casodeuso.RegistrarConductorLicenciaUsoCaso;
import m3.dominio.Documento;
import m3.dominio.excepcion.DominioValidacionExcepcion;
import m3.dominio.excepcion.VersionInconsistenciaExcepcion;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConductorLicenciaWebControlador {
    private static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final RegistrarConductorLicenciaUsoCaso licenses;

    public ConductorLicenciaWebControlador(RegistrarConductorLicenciaUsoCaso licenses) {
        this.licenses = licenses;
    }

    @GetMapping("/ui/drivers/{driverId}/documents")
    public String page(@PathVariable UUID driverId, Model model) {
        if (!model.containsAttribute("form")) {
            ConductorLicenciaFormulario form = new ConductorLicenciaFormulario();
            form.setExpectedVersion(licenses.driver(driverId).getVersion());
            model.addAttribute("form", form);
        }
        populate(driverId, model);
        return "documentos-conductor";
    }

    @PostMapping("/ui/drivers/{driverId}/documents")
    public String submit(@PathVariable UUID driverId,
            @Valid @ModelAttribute("form") ConductorLicenciaFormulario form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors() && !form.getIssuedAt().isBefore(form.getValidUntil())) {
            errors.rejectValue("validUntil", "period", "El vencimiento debe ser posterior a la emisión.");
        }
        if (errors.hasErrors()) {
            populate(driverId, model);
            return "documentos-conductor";
        }
        try {
            Instant issuedAt = form.getIssuedAt().atStartOfDay(ARGENTINA).toInstant();
            Instant validUntil = form.getValidUntil().plusDays(1).atStartOfDay(ARGENTINA).toInstant();
            licenses.register(new RegistrarConductorLicenciaComando(driverId, form.getDocumentNumber(),
                    issuedAt, validUntil, form.getServiceTypes(), null, form.getExpectedVersion()));
        } catch (VersionInconsistenciaExcepcion ex) {
            errors.reject("version", "Los datos cambiaron. Actualizá la página y volvé a enviar.");
            populate(driverId, model);
            return "documentos-conductor";
        } catch (DominioValidacionExcepcion ex) {
            errors.reject("domain", ex.getMessage());
            populate(driverId, model);
            return "documentos-conductor";
        }
        redirect.addFlashAttribute("success", "Licencia registrada. Quedó pendiente de revisión.");
        return "redirect:/ui/drivers/" + driverId + "/documents";
    }

    private void populate(UUID driverId, Model model) {
        model.addAttribute("driver", licenses.driver(driverId));
        List<DocumentRow> history = licenses.history(driverId).stream()
                .map(document -> new DocumentRow(document.getRevision(),
                        DATE_TIME.format(document.getIssuedAt().atZone(ARGENTINA)),
                        DATE_TIME.format(document.getValidUntil().atZone(ARGENTINA)),
                        document.getStatus().name(), document.isCurrent(),
                        document.getServiceTypes().toString()))
                .toList();
        model.addAttribute("history", history);
    }

    public record DocumentRow(int revision, String issuedAt, String validUntil,
                              String status, boolean current, String services) {}
}
