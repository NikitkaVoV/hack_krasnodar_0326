import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Input } from '@/shared/ui/Input/Input';
import { Button } from '@/shared/ui/Button/Button';
import { Card } from '@/shared/ui/Card/Card';
import { useAuth } from '@/features/auth/model/useAuth';
import { appRoutes } from '@/shared/const/routes';
import { getApiErrorMessage } from '@/shared/lib/errors';
import styles from './LoginForm.module.css';

interface LoginLocationState {
  from?: { pathname?: string };
}

export function LoginForm() {
  const { login } = useAuth();
  const [form, setForm] = useState({ login: '', password: '' });
  const [errors, setErrors] = useState<{ login?: string; password?: string; common?: string }>({});
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const redirectPath = useMemo(
    () => (location.state as LoginLocationState | null)?.from?.pathname ?? appRoutes.home,
    [location.state],
  );

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    const nextErrors: typeof errors = {};
    if (!form.login.trim()) {
      nextErrors.login = 'Введите логин.';
    }
    if (!form.password.trim()) {
      nextErrors.password = 'Введите пароль.';
    } else if (form.password.trim().length < 4) {
      nextErrors.password = 'Пароль должен содержать минимум 4 символа.';
    }

    setErrors(nextErrors);

    if (Object.keys(nextErrors).length) {
      return;
    }

    setIsLoading(true);
    try {
      await login({ login: form.login.trim(), password: form.password });
      navigate(redirectPath, { replace: true });
    } catch (error) {
      setErrors({ common: getApiErrorMessage(error, 'Не удалось выполнить вход.') });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.layout}>
      <aside className={styles.side}>
        <p className={styles.kicker}>TourFront</p>
        <h1>Вход в аккаунт для персональных маршрутов</h1>
        <p>
          Авторизуйтесь, чтобы сохранить предпочтения, получать персональные рекомендации и быстро
          продолжать планирование поездки.
        </p>
      </aside>

      <Card className={styles.card}>
        <div className={styles.header}>
          <h2>Добро пожаловать</h2>
          <p>Используйте ваши учетные данные для входа в систему.</p>
        </div>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <Input
            id="login"
            label="Логин"
            autoComplete="username"
            value={form.login}
            onChange={(event) => setForm((prev) => ({ ...prev, login: event.target.value }))}
            error={errors.login}
          />
          <Input
            id="password"
            label="Пароль"
            type="password"
            autoComplete="current-password"
            value={form.password}
            onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
            error={errors.password}
          />
          {errors.common ? <p className={styles.error}>{errors.common}</p> : null}
          <Button type="submit" fullWidth disabled={isLoading}>
            {isLoading ? 'Выполняем вход...' : 'Войти'}
          </Button>
        </form>
      </Card>
    </div>
  );
}
