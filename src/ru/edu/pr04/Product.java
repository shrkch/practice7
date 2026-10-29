package ru.edu.pr04;

/**
 * Товар каталога. Иммутабельный: поля final, setter-ов нет.
 */
public class Product {

    private final String id;
    private final String name;
    private final double price;

    // Проверка инвариантов R1, R2, R3 до присваивания полей (fail-fast в конструкторе)
    public Product(String id, String name, double price) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is null or blank");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("price must be > 0, got " + price);
        }
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
