package sn.isi.clinique_api.notifications.internal;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sn.isi.clinique_api.notifications.api.NotificationDto;
import sn.isi.clinique_api.securite.api.ContexteSecurite;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notifications créées automatiquement à partir des événements de rendez-vous")
public class NotificationController {

    private final NotificationService service;
    private final ContexteSecurite contexte;

    public NotificationController(NotificationService service, ContexteSecurite contexte) {
        this.service = service;
        this.contexte = contexte;
    }

    @GetMapping
    @Operation(summary = "Lister les notifications",
            description = "Notifications créées à partir des événements de rendez-vous. Les plus récentes d'abord. "
                    + "Un médecin ne voit que les siennes, quel que soit le filtre demandé.")
    @ApiResponse(responseCode = "200", description = "Page de notifications")
    @ApiResponse(responseCode = "400", description = "Critère de tri invalide")
    public PagedModel<NotificationDto> lister(
            @Parameter(description = "Identifiant du patient destinataire", example = "1")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "Identifiant du médecin destinataire", example = "1")
            @RequestParam(required = false) Long medecinId,
            @Parameter(description = "true : seulement les notifications non lues")
            @RequestParam(defaultValue = "false") boolean nonLues,
            @ParameterObject @PageableDefault(size = 20, sort = "creeLe",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Long medecinImpose = contexte.medecinImpose().orElse(null);
        if (medecinImpose != null) {
            // un médecin ne voit que ses notifications : on écrase les filtres reçus
            return new PagedModel<>(service.lister(null, medecinImpose, nonLues, pageable));
        }
        return new PagedModel<>(service.lister(patientId, medecinId, nonLues, pageable));
    }

    @PatchMapping("/{id}/lue")
    @Operation(summary = "Marquer une notification comme lue")
    @ApiResponse(responseCode = "200", description = "Notification marquée comme lue")
    @ApiResponse(responseCode = "404", description = "Notification introuvable (ou destinée à quelqu'un d'autre)")
    public NotificationDto marquerLue(@PathVariable Long id) {
        return service.marquerLue(id, contexte.medecinImpose().orElse(null));
    }
}