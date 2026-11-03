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

import sn.isi.clinique_api.notifications.api.NotificationDto;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<NotificationDto> lister(
            @RequestParam(required = false) Long patientId,
            @RequestParam(defaultValue = "false") boolean nonLues,
            @ParameterObject @PageableDefault(size = 20, sort = "creeLe",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(service.lister(patientId, nonLues, pageable));
    }

    @PatchMapping("/{id}/lue")
    public NotificationDto marquerLue(@PathVariable Long id) {
        return service.marquerLue(id);
    }
}
