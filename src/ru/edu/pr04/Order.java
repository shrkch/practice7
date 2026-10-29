package ru.edu.pr04;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Заказ сотрудника. Сам управляет своим составом и статусом.
 */
public class Order {

    private final Employee employee;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;

    // R6: employee не null
    public Order(Employee employee) {
        this.employee = Objects.requireNonNull(employee, "employee is null");
    }

    // R8 — запрет на CONFIRMED; R7 — повтор id объединяется через increase, иначе новая позиция
    public void addProduct(Product product, int quantity) {
        checkDraft();
        if (product == null) {
            throw new IllegalArgumentException("product is null");
        }
        for (OrderItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.increase(quantity);
                return;
            }
        }
        items.add(new OrderItem(product, quantity));
    }

    // R8 — запрет на CONFIRMED; удаление позиции по id через Iterator
    public void removeProduct(String productId) {
        checkDraft();
        Iterator<OrderItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getProduct().getId().equals(productId)) {
                iterator.remove();
                return;
            }
        }
    }

    // R9 — сумма пересчитывается при каждом вызове, поле total не хранится
    public double total() {
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.getLineTotal();
        }
        return sum;
    }

    // R8 — повторное подтверждение запрещено; R10 — пустой заказ подтвердить нельзя
    public void confirm() {
        checkDraft();
        if (items.isEmpty()) {
            throw new IllegalStateException("order is empty");
        }
        status = OrderStatus.CONFIRMED;
    }

    // R11: неизменяемая копия — внешний код не может изменить состав в обход addProduct
    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Employee getEmployee() {
        return employee;
    }

    // R8: общая проверка для addProduct, removeProduct, confirm
    private void checkDraft() {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("order is confirmed");
        }
    }
}
