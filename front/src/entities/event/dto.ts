export interface EventDto {
  id: string;
  name: string;
  description?: string;
  location?: string;
  startAt?: string;
  imageUrl?: string;
  coverImage?: string;
  previewImage?: string;
  photos?: Array<string | { url?: string; src?: string; imageUrl?: string }>;
  images?: Array<string | { url?: string; src?: string; imageUrl?: string }>;
}
