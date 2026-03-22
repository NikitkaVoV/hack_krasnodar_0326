import type { UserResponseDto } from './dto';
import type { SessionUser } from './model';

export function mapUserResponseDtoToSessionUser(dto: UserResponseDto): SessionUser {
  return {
    id: dto.id,
    login: dto.login,
    name: dto.name,
    userType: dto.userType,
  };
}


