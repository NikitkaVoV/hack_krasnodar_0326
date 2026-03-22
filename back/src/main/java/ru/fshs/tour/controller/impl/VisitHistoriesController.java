package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.route.VisitHistory;
import ru.fshs.tour.repository.VisitHistoryRepository;

@RestController
@RequestMapping("/api/visit-history")
@Tag(name = "Visit History")
@SecurityRequirement(name = "bearerAuth")
public class VisitHistoriesController extends AbstractCrudController<VisitHistory, VisitHistoryRepository> {

    public VisitHistoriesController(VisitHistoryRepository repository) {
        super(repository);
    }

    @GetMapping(params = "userId")
    public List<VisitHistory> findAllByUserId(@RequestParam UUID userId) {
        return repository.findAllByUserId(userId);
    }

    @GetMapping(params = "routeId")
    public List<VisitHistory> findAllByRouteId(@RequestParam UUID routeId) {
        return repository.findAllByRouteId(routeId);
    }

    @GetMapping(params = {"targetId", "targetType"})
    public List<VisitHistory> findAllByTarget(
            @RequestParam UUID targetId,
            @RequestParam String targetType
    ) {
        return repository.findAllByTargetIdAndTargetType(targetId, targetType);
    }
}
