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

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notifications créées automatiquement à partir des événements de rendez-vous")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les notifications",
            description = "Les notifications sont créées en réponse aux événements : rendez-vous confirmé, annulé, "
                    + "ou rappel la veille. Les plus récentes sont renvoyées en premier.")
    @ApiResponse(responseCode = "200", description = "Page de notifications")
    @ApiResponse(responseCode = "400", description = "Critère de tri invalide")
    public PagedModel<NotificationDto> lister(
            @Parameter(description = "Identifiant du patient", example = "1")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "true : seulement les notifications non lues")
            @RequestParam(defaultValue = "false") boolean nonLues,
            @ParameterObject @PageableDefault(size = 20, sort = "creeLe",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(service.lister(patientId, nonLues, pageable));
    }

    @PatchMapping("/{id}/lue")
    @Operation(summary = "Marquer une notification comme lue")
    @ApiResponse(responseCode = "200", description = "Notification marquée comme lue")
    @ApiResponse(responseCode = "404", description = "Notification introuvable")
    public NotificationDto marquerLue(@PathVariable Long id) {
        return service.marquerLue(id);
    }
}