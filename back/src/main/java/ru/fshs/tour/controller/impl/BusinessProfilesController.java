package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.user.BusinessProfile;
import ru.fshs.tour.repository.BusinessProfileRepository;

@RestController
@RequestMapping("/api/business-profiles")
@Tag(name = "Business Profiles")
@SecurityRequirement(name = "bearerAuth")
public class BusinessProfilesController extends AbstractCrudController<BusinessProfile, BusinessProfileRepository> {

    public BusinessProfilesController(BusinessProfileRepository repository) {
        super(repository);
    }

    @GetMapping(params = "userId")
    public BusinessProfile findByUserId(@RequestParam UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business profile not found"));
    }
}
