package ru.fshs.tour.service.model;

import java.util.List;

public record ImageCompatFields(
        String imageUrl,
        String coverImage,
        String previewImage,
        List<Object> photos,
        List<Object> images
) {
}
