import styles from './HowItWorks.module.css';

const steps = [
  {
    title: 'Опишите интересы',
    text: 'Выберите, что хотите увидеть: природу, культуру, гастро-точки или события.',
  },
  {
    title: 'Уточните дату и длительность',
    text: 'Сервис учитывает ваш график и предлагает оптимальный формат поездки.',
  },
  {
    title: 'Получите готовый маршрут',
    text: 'Сформированный план включает ключевые точки, советы и удобный темп.',
  },
];

export function HowItWorks() {
  return (
    <section className={styles.section}>
      <h2>Как это работает</h2>
      <div className={styles.grid}>
        {steps.map((step, index) => (
          <article key={step.title} className={styles.step}>
            <span>{index + 1}</span>
            <h3>{step.title}</h3>
            <p>{step.text}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
