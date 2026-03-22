import { http } from '@/shared/api/http';
import type {
  UserConstraintDto,
  UserDto,
  UserPreferenceDto,
  UserTagDto,
} from '@/entities/user/dto';

export const usersApi = {
  async getUserById(userId: string): Promise<UserDto> {
    const { data } = await http.get<UserDto>(`/api/users/${userId}`);
    return data;
  },
  async getUserPreferences(userId: string): Promise<UserPreferenceDto[]> {
    const { data } = await http.get<UserPreferenceDto[]>('/api/user-preferences', {
      params: { userId },
    });
    return data;
  },
  async getUserTags(userId: string): Promise<UserTagDto[]> {
    const { data } = await http.get<UserTagDto[]>('/api/user-tags', {
      params: { userId, tagId: '00000000-0000-0000-0000-000000000000' },
    });
    return data;
  },
  async getUserConstraints(userId: string): Promise<UserConstraintDto[]> {
    const { data } = await http.get<UserConstraintDto[]>('/api/user-constraints', {
      params: { userId, constraintId: '00000000-0000-0000-0000-000000000000' },
    });
    return data;
  },
};


