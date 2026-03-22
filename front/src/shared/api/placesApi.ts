import { http } from '@/shared/api/http';
import type { PlaceDto } from '@/entities/place/dto';

export const placesApi = {
  async getPlaceById(placeId: string): Promise<PlaceDto> {
    const { data } = await http.get<PlaceDto>(`/api/route-steps/places/${placeId}`);
    return data;
  },
};


