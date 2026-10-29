package ru.edu.pr04;

import java.util.Objects;

/**
 * Сервисный слой без фреймворков. Бизнес-состояния нет: единственное поле —
 * ссылка на репозиторий, которую передают в конструктор вручную (без Spring).
 */
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is null");
    }

    // Вариант 26, часть А: созданный заказ сразу сохраняется в репозитории
    public Order createOrder(Employee employee) {
        Order order = new Order(employee);
        repository.save(order);
        return order;
    }

    // Вариант 26, часть Б: количество заказов в репозитории
    public int addRepositoryCount(OrderRepository repo) {
        Objects.requireNonNull(repo, "repo is null");
        return repo.findAll().size();
    }
}
