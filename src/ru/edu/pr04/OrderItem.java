package ru.edu.pr04;

/**
 * Позиция заказа: товар + количество.
 */
public class OrderItem {

    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 100;

    private final Product product;   // final: товар позиции не меняется
    private int quantity;            // меняется только через setQuantity / increase

    // R5: product не null, затем R4 через setQuantity
    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("product is null");
        }
        this.product = product;
        setQuantity(quantity);
    }

    // R4 — 1 <= quantity <= 100 включительно. Единственное место проверки (DRY)
    private void setQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "quantity must be in [" + MIN_QUANTITY + ", " + MAX_QUANTITY + "], got " + quantity);
        }
        this.quantity = quantity;
    }

    // Добавление количества (используется при объединении повторов R7)
    public void increase(int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be > 0, got " + delta);
        }
        setQuantity(this.quantity + delta);
    }

    // Стоимость позиции = цена × количество
    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }
}
