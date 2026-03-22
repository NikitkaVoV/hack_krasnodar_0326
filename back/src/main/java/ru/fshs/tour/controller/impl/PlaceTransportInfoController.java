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
import ru.fshs.tour.domain.content.PlaceTransportInfo;
import ru.fshs.tour.repository.PlaceTransportInfoRepository;

@RestController
@RequestMapping("/api/place-transport-info")
@Tag(name = "Place Transport Info")
@SecurityRequirement(name = "bearerAuth")
public class PlaceTransportInfoController extends AbstractCrudController<PlaceTransportInfo, PlaceTransportInfoRepository> {

    public PlaceTransportInfoController(PlaceTransportInfoRepository repository) {
        super(repository);
    }

    @GetMapping(params = "placeId")
    public List<PlaceTransportInfo> findAllByPlaceId(@RequestParam UUID placeId) {
        return repository.findAllByPlaceId(placeId);
    }
}
