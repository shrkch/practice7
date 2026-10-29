package ru.edu.pr04;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Вариант 26, часть А: хранилище созданных заказов в памяти.
 */
public class OrderRepository {

    private final List<Order> orders = new ArrayList<>();

    // Сохраняет заказ; повторное сохранение того же объекта не создаёт дубликат
    public void save(Order order) {
        Objects.requireNonNull(order, "order is null");
        for (Order saved : orders) {
            if (saved == order) {
                return;
            }
        }
        orders.add(order);
    }

    // Неизменяемая копия, как getItems в Order (R11)
    public List<Order> findAll() {
        return List.copyOf(orders);
    }
}
