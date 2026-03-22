export interface UserProfile {
  id: string;
  login: string;
  name: string;
  userType: string;
  age?: number;
  budgetMin?: number;
  budgetMax?: number;
  lastLocationLat?: number;
  lastLocationLng?: number;
  additionalInfo?: Record<string, unknown>;
}



