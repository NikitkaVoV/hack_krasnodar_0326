export interface PlaceDto {
  id: string;
  name: string;
  description?: string;
  location?: string;
  imageUrl?: string;
  coverImage?: string;
  previewImage?: string;
  photos?: Array<string | { url?: string; src?: string; imageUrl?: string }>;
  images?: Array<string | { url?: string; src?: string; imageUrl?: string }>;
}
