import type { UserDto } from './dto';
import type { UserProfile } from './model';

export function mapUserDtoToUserProfile(dto: UserDto): UserProfile {
  return {
    id: dto.id,
    login: dto.login,
    name: dto.name,
    userType: dto.userType,
    age: dto.age,
    budgetMin: dto.budgetMin,
    budgetMax: dto.budgetMax,
    lastLocationLat: dto.lastLocationLat,
    lastLocationLng: dto.lastLocationLng,
    additionalInfo: dto.additionalInfo,
  };
}



