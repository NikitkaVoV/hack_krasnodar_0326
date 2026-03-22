package ru.fshs.tour.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import ru.fshs.tour.service.model.ImageCompatFields;

@Service
public class ImageCompatService {

    public ImageCompatFields fromPrimaryUrl(String url) {
        if (url == null || url.isBlank()) {
            return new ImageCompatFields(null, null, null, null, null);
        }
        return new ImageCompatFields(
                url,
                url,
                url,
                List.of(url, Map.of("url", url)),
                List.of(url, Map.of("src", url))
        );
    }
}
