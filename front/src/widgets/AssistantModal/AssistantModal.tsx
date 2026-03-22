import { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { useLocation } from 'react-router-dom';
import { createPortal } from 'react-dom';
import { useQueryClient } from '@tanstack/react-query';
import { assistantApi } from '@/shared/api/assistantApi';
import { Button } from '@/shared/ui/Button/Button';
import type { AssistantMessageResponse, AssistantRouteForSave, AssistantRouteStepForSave, UUID } from '@/entities/assistant/model';
import styles from './AssistantModal.module.css';

type SaveState = 'idle' | 'saving' | 'saved' | 'error';

type ChatMessage = {
  id: string;
  role: 'user' | 'assistant' | 'error';
  text: string;
  createdAt: number;
  raw?: unknown;
  routeForSave?: AssistantRouteForSave;
  saveState?: SaveState;
  saveStatusText?: string;
};

const STORAGE_KEY = 'assistant-session-id';
const ME_ROUTES_QUERY_KEY = ['me', 'routes'] as const;

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function readString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() ? value : undefined;
}

function readNumber(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isFinite(value) ? value : undefined;
}

function mapRouteStep(rawStep: unknown): AssistantRouteStepForSave | null {
  if (!isRecord(rawStep)) {
    return null;
  }

  const id = readString(rawStep.id);
  const type = rawStep.type === 'place' || rawStep.type === 'event' ? rawStep.type : null;

  if (!id || !type) {
    return null;
  }

  return {
    id,
    type,
    title: readString(rawStep.title),
    description: readString(rawStep.description),
    address: readString(rawStep.address),
    lat: readNumber(rawStep.lat),
    lng: readNumber(rawStep.lng),
    duration: readNumber(rawStep.duration),
    orderIndex: readNumber(rawStep.orderIndex),
    imageUrl: readString(rawStep.imageUrl),
    plannedTimeStart: readString(rawStep.plannedTimeStart),
    plannedTimeEnd: readString(rawStep.plannedTimeEnd),
  };
}

function extractRouteForSave(response: AssistantMessageResponse): AssistantRouteForSave | null {
  const payload = response.systemResponse?.payload;
  if (!isRecord(payload) || !isRecord(payload.route)) {
    return null;
  }

  const rawRoute = payload.route;
  const canBeSaved = rawRoute.canBeSaved === true;
  if (!canBeSaved) {
    return null;
  }

  const steps = Array.isArray(rawRoute.steps)
    ? rawRoute.steps.map(mapRouteStep).filter((step): step is AssistantRouteStepForSave => Boolean(step))
    : [];
  if (!steps.length) {
    return null;
  }

  return {
    summary: readString(rawRoute.summary),
    advice: readString(rawRoute.advice),
    totalDuration: readNumber(rawRoute.totalDuration),
    date: readString(rawRoute.date),
    imageUrl: readString(rawRoute.imageUrl),
    canBeSaved: true,
    steps,
  };
}

function uuidLikeFromPath(pathname: string, pattern: RegExp): string | null {
  const match = pathname.match(pattern);
  return match?.[1] ?? null;
}

function buildClientContext(pathname: string) {
  const routeId = uuidLikeFromPath(pathname, /^\/routes\/([a-zA-Z0-9-]{6,})$/);
  const placeId = uuidLikeFromPath(pathname, /^\/places\/([a-zA-Z0-9-]{6,})$/);
  const eventId = uuidLikeFromPath(pathname, /^\/events\/([a-zA-Z0-9-]{6,})$/);

  return {
    routeId,
    placeId,
    eventId,
  };
}

function summarizeAssistantReply(response: AssistantMessageResponse): string {
  const base = response.reply?.text?.trim() || 'Ответ без текста.';

  if (
    response.systemResponse?.status === 'NEEDS_CLARIFICATION' ||
    response.systemResponse?.nextActions?.includes('ASK_CLARIFICATION')
  ) {
    return `${base}\n\nНужно уточнение от пользователя.`;
  }

  return base;
}

function parseApiErrorMessage(error: unknown, fallback: string): string {
  const axiosError = axios.isAxiosError(error) ? error : null;
  const raw = axiosError?.response?.data;

  if (isRecord(raw) && typeof raw.message === 'string' && raw.message.trim()) {
    return raw.message;
  }

  return fallback;
}

export function AssistantModal() {
  const { pathname } = useLocation();
  const queryClient = useQueryClient();
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isSending, setIsSending] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<UUID | null>(() => localStorage.getItem(STORAGE_KEY));

  useEffect(() => {
    if (!copiedId) {
      return;
    }

    const timer = window.setTimeout(() => setCopiedId(null), 1200);
    return () => window.clearTimeout(timer);
  }, [copiedId]);

  const sessionLabel = useMemo(() => {
    if (!sessionId) {
      return 'Новая сессия';
    }

    return `Сессия: ${sessionId.slice(0, 8)}...`;
  }, [sessionId]);

  const clearSession = () => {
    setMessages([]);
    setSessionId(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  const sendMessage = async () => {
    const text = input.trim();
    if (!text || isSending) {
      return;
    }

    const userMessage: ChatMessage = {
      id: `u-${Date.now()}`,
      role: 'user',
      text,
      createdAt: Date.now(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsSending(true);

    try {
      const response = await assistantApi.sendMessage({
        sessionId,
        message: text,
        clientContext: buildClientContext(pathname),
      });

      setSessionId(response.sessionId);
      localStorage.setItem(STORAGE_KEY, response.sessionId);

      const routeForSave = extractRouteForSave(response);

      setMessages((prev) => [
        ...prev,
        {
          id: `a-${Date.now()}`,
          role: 'assistant',
          text: summarizeAssistantReply(response),
          createdAt: Date.now(),
          raw: response,
          routeForSave: routeForSave ?? undefined,
          saveState: routeForSave ? 'idle' : undefined,
        },
      ]);
    } catch (error) {
      const axiosError = axios.isAxiosError(error) ? error : null;
      const status = axiosError?.response?.status;
      const message = parseApiErrorMessage(error, 'Не удалось обработать запрос ассистента.');
      const raw = axiosError?.response?.data ?? { message: String(error) };

      setMessages((prev) => [
        ...prev,
        {
          id: `e-${Date.now()}`,
          role: 'error',
          text: status ? `Ошибка ${status}: ${message}` : message,
          createdAt: Date.now(),
          raw,
        },
      ]);
    } finally {
      setIsSending(false);
    }
  };

  const saveRouteFromMessage = async (messageId: string) => {
    const targetMessage = messages.find((message) => message.id === messageId);
    if (!targetMessage?.routeForSave || targetMessage.saveState === 'saving' || targetMessage.saveState === 'saved') {
      return;
    }

    setMessages((prev) =>
      prev.map((message) =>
        message.id === messageId
          ? {
              ...message,
              saveState: 'saving',
              saveStatusText: undefined,
            }
          : message,
      ),
    );

    try {
      const response = await assistantApi.saveRouteFromAi({ route: targetMessage.routeForSave });

      setMessages((prev) =>
        prev.map((message) =>
          message.id === messageId
            ? {
                ...message,
                saveState: 'saved',
                saveStatusText: response.message || 'Маршрут сохранён',
              }
            : message,
        ),
      );

      await queryClient.invalidateQueries({ queryKey: ME_ROUTES_QUERY_KEY });
    } catch (error) {
      const status = axios.isAxiosError(error) ? error.response?.status : null;
      const message = parseApiErrorMessage(error, 'Не удалось сохранить маршрут.');

      setMessages((prev) =>
        prev.map((item) =>
          item.id === messageId
            ? {
                ...item,
                saveState: 'error',
                saveStatusText: status ? `Ошибка ${status}: ${message}` : message,
              }
            : item,
        ),
      );
    }
  };

  const copyMessageJson = async (message: ChatMessage) => {
    if (message.role === 'user') {
      return;
    }

    const payload = message.raw ?? { text: message.text };

    try {
      await navigator.clipboard.writeText(JSON.stringify(payload, null, 2));
      setCopiedId(message.id);
    } catch {
      // ignore clipboard errors silently
    }
  };

  const content = (
    <>
      <button className={styles.launcher} onClick={() => setIsOpen(true)} aria-label="Открыть AI-чат">
        AI
      </button>

      {isOpen ? (
        <div className={styles.overlay} onClick={() => setIsOpen(false)}>
          <section className={styles.modal} onClick={(event) => event.stopPropagation()}>
            <header className={styles.header}>
              <div>
                <h3>AI ассистент</h3>
                <p>{sessionLabel}</p>
              </div>
              <div className={styles.headerActions}>
                <Button variant="ghost" onClick={clearSession}>
                  Очистить сессию
                </Button>
                <Button variant="ghost" onClick={() => setIsOpen(false)}>
                  Закрыть
                </Button>
              </div>
            </header>

            <div className={styles.chat}>
              {!messages.length ? (
                <div className={styles.empty}>Напишите сообщение, чтобы начать новую или продолжить текущую сессию.</div>
              ) : null}

              {messages.map((message) => (
                <article
                  key={message.id}
                  className={`${styles.message} ${styles[message.role]}`}
                  onClick={() => void copyMessageJson(message)}
                  title={message.role === 'user' ? '' : 'Нажмите, чтобы скопировать JSON ответа'}
                >
                  <p>{message.text}</p>

                  {message.role === 'assistant' && message.routeForSave ? (
                    <div className={styles.routeSaveRow} onClick={(event) => event.stopPropagation()}>
                      {message.saveState === 'saved' ? (
                        <span className={styles.routeSaveSuccess}>{message.saveStatusText ?? 'Маршрут сохранён'}</span>
                      ) : (
                        <Button
                          onClick={() => void saveRouteFromMessage(message.id)}
                          disabled={message.saveState === 'saving'}
                          variant={message.saveState === 'error' ? 'danger' : 'primary'}
                        >
                          {message.saveState === 'saving' ? 'Сохраняем...' : 'Сохранить маршрут'}
                        </Button>
                      )}

                      {message.saveState === 'error' && message.saveStatusText ? (
                        <span className={styles.routeSaveError}>{message.saveStatusText}</span>
                      ) : null}
                    </div>
                  ) : null}

                  <span>
                    {new Date(message.createdAt).toLocaleTimeString('ru-RU', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                    {copiedId === message.id ? ' · JSON скопирован' : ''}
                  </span>
                </article>
              ))}
            </div>

            <footer className={styles.footer}>
              <textarea
                value={input}
                onChange={(event) => setInput(event.target.value)}
                placeholder="Например: подбери спокойный маршрут рядом на 3 часа"
                rows={3}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    void sendMessage();
                  }
                }}
              />

              <Button onClick={() => void sendMessage()} disabled={isSending || !input.trim()}>
                {isSending ? 'Отправляем...' : 'Отправить'}
              </Button>
            </footer>
          </section>
        </div>
      ) : null}
    </>
  );

  if (typeof document === 'undefined') {
    return null;
  }

  return createPortal(content, document.body);
}
