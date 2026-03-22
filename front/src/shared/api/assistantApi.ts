import { http } from '@/shared/api/http';
import type {
  AssistantMessageRequest,
  AssistantMessageResponse,
  SaveRouteFromAiRequest,
  SaveRouteFromAiResponse,
} from '@/entities/assistant/model';

export const assistantApi = {
  async sendMessage(payload: AssistantMessageRequest): Promise<AssistantMessageResponse> {
    const { data } = await http.post<AssistantMessageResponse>('/api/assistant/messages', payload);
    return data;
  },

  async saveRouteFromAi(payload: SaveRouteFromAiRequest): Promise<SaveRouteFromAiResponse> {
    const { data } = await http.post<SaveRouteFromAiResponse>('/api/routes/save-from-ai', payload);
    return data;
  },
};
