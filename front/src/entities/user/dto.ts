export interface UserDto {
  id: string;
  login: string;
  name: string;
  age?: number;
  userType: string;
  budgetMin?: number;
  budgetMax?: number;
  lastLocationLat?: number;
  lastLocationLng?: number;
  additionalInfo?: Record<string, unknown>;
}

export interface UserPreferenceDto {
  id: string;
  userId: string;
  preference: string;
}

export interface UserTagDto {
  id: string;
  userId: string;
  tagId?: string;
  value?: string;
}

export interface UserConstraintDto {
  id: string;
  userId: string;
  constraintId?: string;
  value?: string;
}


