package ru.edu.pr04;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Тестовый раннер: unit-проверки, data-driven режим по data/sample.csv и интерактивный режим.
 */
public class SelfCheck {

    // Текст ошибки R4 строится из констант OrderItem: при смене лимита тесты не нужно править
    private static final String QTY_ERROR =
            "quantity must be in [" + OrderItem.MIN_QUANTITY + ", " + OrderItem.MAX_QUANTITY + "]";

    private static int passed = 0;
    private static int failed = 0;

    // Состояние data-driven режима: репозиторий, сервис и текущий заказ
    private static OrderRepository dataRepo;
    private static OrderService dataService;
    private static Order dataOrder;

    public static void main(String[] args) {
        System.out.println("=== Unit-проверки (18 базовых) ===");
        checkProduct();
        checkOrderItem();
        checkOrderCompose();
        checkOrderConfirm();
        System.out.println();
        System.out.println("=== Вариант 26 ===");
        checkVariantA();
        checkVariantB();
        System.out.println();
        System.out.println("=== Дополнительные проверки ===");
        checkExtra();
        System.out.println("Unit: " + passed + "/" + (passed + failed) + " passed");
        runDataDriven("data/sample.csv");
        runInteractive();
    }

    // ===== 1–6: Product (R1, R2, R3) =====
    private static void checkProduct() {
        check("1 Product correct", "OK".equals(productError("M01", "Monitor 27", 32990)));
        check("2 Product id=null", "id is null or blank".equals(productError(null, "Monitor", 100)));
        check("3 Product id=\"\"", "id is null or blank".equals(productError("", "Monitor", 100)));
        check("4 Product name=null", "name is null or blank".equals(productError("M01", null, 100)));
        check("5 Product price=0", productError("M01", "Monitor", 0).startsWith("price must be > 0"));
        check("6 Product price=-100", productError("M01", "Monitor", -100).startsWith("price must be > 0"));
    }

    // ===== 7–12: OrderItem (R4, increase) =====
    private static void checkOrderItem() {
        check("7 OrderItem correct qty=2", "OK".equals(itemError(2)));
        int max = OrderItem.MAX_QUANTITY;
        check("8 OrderItem qty=0", itemError(0).startsWith(QTY_ERROR));
        check("9 OrderItem qty=" + (max + 1), itemError(max + 1).startsWith(QTY_ERROR));
        check("10 OrderItem qty=1 (нижняя граница)", "OK".equals(itemError(1)));
        check("11 OrderItem qty=" + max + " (верхняя граница)", "OK".equals(itemError(max)));
        OrderItem item = new OrderItem(monitor(), 2);
        item.increase(3);
        check("12 OrderItem increase(3) -> 5", item.getQuantity() == 5);
    }

    // ===== 13–15: Order — состав и итог (R7, R9) =====
    private static void checkOrderCompose() {
        Order order = newOrder();
        order.addProduct(monitor(), 1);
        order.addProduct(keyboard(), 2);
        check("13 Order два разных товара -> 2 позиции", order.getItems().size() == 2);

        Order merged = newOrder();
        merged.addProduct(monitor(), 2);
        merged.addProduct(monitor(), 1);
        check("14 Order тот же id (R7) -> 1 позиция, qty=3",
                merged.getItems().size() == 1 && merged.getItems().get(0).getQuantity() == 3);

        // 32990 × 1 + 4490 × 2 = 41970
        check("15 Order total двух позиций = 41970", order.total() == 41970.0);
    }

    // ===== 16–18: Order — подтверждение (R8, R10) =====
    private static void checkOrderConfirm() {
        Order order = newOrder();
        order.addProduct(monitor(), 1);
        order.confirm();
        check("16 Order confirm непустого -> CONFIRMED", order.getStatus() == OrderStatus.CONFIRMED);

        String empty = "OK";
        try {
            newOrder().confirm();
        } catch (IllegalStateException e) {
            empty = e.getMessage();
        }
        check("17 Order confirm пустого (R10)", "order is empty".equals(empty));

        String afterConfirm = "OK";
        try {
            order.addProduct(keyboard(), 1);
        } catch (IllegalStateException e) {
            afterConfirm = e.getMessage();
        }
        check("18 Order addProduct после confirm (R8)", "order is confirmed".equals(afterConfirm));
    }

    // ===== 19–22: Вариант 26, часть А — OrderRepository, createOrder сохраняет =====
    private static void checkVariantA() {
        OrderRepository repo = new OrderRepository();
        OrderService service = new OrderService(repo);
        Order order = service.createOrder(new Employee("E01", "Степанов Владимир"));
        check("19 variant A: createOrder сохраняет заказ в репозитории",
                repo.findAll().size() == 1 && repo.findAll().get(0) == order);

        repo.save(order);
        check("20 variant A: повторный save того же заказа не дублирует", repo.findAll().size() == 1);

        boolean copy = false;
        try {
            repo.findAll().add(order);
        } catch (UnsupportedOperationException e) {
            copy = true;
        }
        check("21 variant A: findAll возвращает неизменяемую копию", copy && repo.findAll().size() == 1);
        checkVariantANullRepo();
    }

    private static void checkVariantANullRepo() {
        String nullRepo = "OK";
        try {
            new OrderService(null);
        } catch (NullPointerException e) {
            nullRepo = e.getMessage();
        }
        check("22 variant A: OrderService без репозитория запрещён", "repository is null".equals(nullRepo));
    }

    // ===== 23–25: Вариант 26, часть Б — addRepositoryCount =====
    private static void checkVariantB() {
        OrderRepository repo = new OrderRepository();
        OrderService service = new OrderService(repo);
        check("23 variant B: пустой репозиторий -> 0", service.addRepositoryCount(repo) == 0);

        service.createOrder(new Employee("E01", "Степанов Владимир"));
        service.createOrder(new Employee("E02", "Петров Пётр"));
        service.createOrder(new Employee("E03", "Иванова Анна"));
        check("24 variant B: три заказа -> 3", service.addRepositoryCount(repo) == 3);

        Order confirmed = service.createOrder(new Employee("E04", "Смирнов Олег"));
        confirmed.addProduct(monitor(), 1);
        confirmed.confirm();
        check("25 variant B: подтверждённый заказ тоже учитывается -> 4", service.addRepositoryCount(repo) == 4);
    }

    // ===== 26–30: дополнительные проверки R5, R6, R8, R11 =====
    private static void checkExtra() {
        Order order = newOrder();
        order.addProduct(monitor(), 1);
        order.addProduct(keyboard(), 1);
        order.removeProduct("M01");
        check("26 Order removeProduct по id", order.getItems().size() == 1
                && order.getItems().get(0).getProduct().getId().equals("K01"));

        boolean unmodifiable = false;
        try {
            order.getItems().add(new OrderItem(monitor(), 1));
        } catch (UnsupportedOperationException e) {
            unmodifiable = true;
        }
        check("27 Order getItems -> List.copyOf (R11)", unmodifiable && order.getItems().size() == 1);
        checkMergeOverLimit();
        checkExtraNulls();
    }

    private static void checkMergeOverLimit() {
        Order order = newOrder();
        int start = OrderItem.MAX_QUANTITY - 1;
        String overLimit = "OK";
        try {
            order.addProduct(monitor(), start);
            order.addProduct(monitor(), 2);
        } catch (IllegalArgumentException e) {
            overLimit = e.getMessage();
        }
        check("28 Order объединение сверх максимума отклонено, qty не изменилось",
                overLimit.startsWith(QTY_ERROR) && quantityOf(order, "M01") == start);
    }

    private static void checkExtraNulls() {
        String nullProduct = "OK";
        try {
            new OrderItem(null, 1);
        } catch (IllegalArgumentException e) {
            nullProduct = e.getMessage();
        }
        check("29 OrderItem product=null (R5)", "product is null".equals(nullProduct));

        String nullEmployee = "OK";
        try {
            new Order(null);
        } catch (NullPointerException e) {
            nullEmployee = e.getMessage();
        }
        check("30 Order employee=null (R6)", "employee is null".equals(nullEmployee));
    }

    // ===== Data-driven режим =====
    private static void runDataDriven(String path) {
        System.out.println();
        System.out.println("=== Data-driven: " + path + " ===");
        List<String> lines = readLines(path);
        if (lines == null) {
            return;
        }
        dataRepo = new OrderRepository();
        dataService = new OrderService(dataRepo);
        dataOrder = dataService.createOrder(new Employee("E01", "Степанов Владимир"));

        int ok = 0;
        int count = 0;
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#") || line.startsWith("productId")) {
                continue;
            }
            count++;
            if (runDataLine(line, count)) {
                ok++;
            }
        }
        System.out.println("Data-driven: " + ok + "/" + count + " passed");
    }

    private static List<String> readLines(String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (Exception e) {
            System.out.println("FAIL чтение файла: " + e.getMessage());
            return null;
        }
    }

    private static boolean runDataLine(String line, int number) {
        String[] p = line.split(";", -1);
        String expected;
        String actual;
        if (p[0].equals("TOTAL")) {
            expected = line;
            actual = "TOTAL;positions=" + dataOrder.getItems().size()
                    + ";sum=" + String.format(Locale.US, "%.2f", dataOrder.total());
        } else {
            expected = p[4];
            actual = evaluate(p);
        }
        boolean match = actual.equals(expected) || (!expected.startsWith("OK:") && actual.startsWith(expected));
        if (match) {
            System.out.println("PASS data/" + number + " " + line);
        } else {
            System.out.println("FAIL data/" + number + " " + line + " -> получено " + actual);
        }
        return match;
    }

    // Выполняет строку sample.csv и возвращает результат в формате колонки expected
    private static String evaluate(String[] p) {
        try {
            switch (p[0]) {
                case "CONFIRM":
                    dataOrder.confirm();
                    return "OK:" + dataOrder.getStatus();
                case "NEW_ORDER":
                    dataOrder = dataService.createOrder(new Employee(p[1], p[2]));
                    return "OK:count=" + dataService.addRepositoryCount(dataRepo);
                case "REPO":
                    return "OK:count=" + dataService.addRepositoryCount(dataRepo);
                default:
                    return addFromData(p);
            }
        } catch (IllegalArgumentException e) {
            return "SKIP:" + e.getMessage();
        } catch (IllegalStateException e) {
            return "ERROR:" + e.getMessage();
        }
    }

    private static String addFromData(String[] p) {
        Product product = new Product(p[0], p[1], Double.parseDouble(p[2]));
        boolean existed = quantityOf(dataOrder, p[0]) > 0;
        dataOrder.addProduct(product, Integer.parseInt(p[3]));
        return "OK:" + (existed ? "merged," : "") + "qty=" + quantityOf(dataOrder, p[0]);
    }

    // ===== Интерактивный режим =====
    private static void runInteractive() {
        System.out.println();
        System.out.println("=== Интерактивный режим ===");
        System.out.println("Команды: add <id> <name> <price> <qty> | remove <id> | items | total | confirm | status");
        System.out.println("         new <employeeId> <name> (новый заказ) | orders (заказов в репозитории) | exit");
        OrderRepository repo = new OrderRepository();
        OrderService service = new OrderService(repo);
        Order order = service.createOrder(new Employee("E01", "Степанов Владимир"));
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        while (scanner.hasNextLine()) {
            String[] parts = scanner.nextLine().trim().split("\\s+");
            if (parts[0].isEmpty() || parts[0].equals("exit")) {
                break;
            }
            try {
                order = handleCommand(parts, order, service, repo);
            } catch (RuntimeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
            System.out.print("> ");
        }
        System.out.println("Завершение");
    }

    // Возвращает текущий заказ (команда new заменяет его новым)
    private static Order handleCommand(String[] c, Order order, OrderService service, OrderRepository repo) {
        switch (c[0]) {
            case "add" -> addCommand(c, order);
            case "remove" -> {
                requireArgs(c, 2, "remove <id>");
                order.removeProduct(c[1]);
                System.out.println("Позиций в заказе: " + order.getItems().size());
            }
            case "items" -> printItems(order);
            case "total" -> System.out.printf(Locale.US, "Итог: %.2f%n", order.total());
            case "confirm" -> {
                order.confirm();
                System.out.println("Статус: " + order.getStatus());
            }
            case "status" -> System.out.println("Статус: " + order.getStatus());
            case "new" -> {
                requireArgs(c, 3, "new <employeeId> <name>");
                order = service.createOrder(new Employee(c[1], c[2]));
                System.out.println("Создан новый заказ. Заказов в репозитории: " + service.addRepositoryCount(repo));
            }
            case "orders" -> System.out.println("Заказов в репозитории: " + service.addRepositoryCount(repo));
            default -> System.out.println("Неизвестная команда: " + c[0]);
        }
        return order;
    }

    private static void addCommand(String[] c, Order order) {
        requireArgs(c, 5, "add <id> <name> <price> <qty>");
        order.addProduct(new Product(c[1], c[2], Double.parseDouble(c[3])), Integer.parseInt(c[4]));
        System.out.println("Добавлено: " + c[1] + ", количество в заказе " + quantityOf(order, c[1]));
    }

    private static void printItems(Order order) {
        if (order.getItems().isEmpty()) {
            System.out.println("Заказ пуст");
        }
        for (OrderItem item : order.getItems()) {
            System.out.printf(Locale.US, "%s %s x %d = %.2f%n", item.getProduct().getId(),
                    item.getProduct().getName(), item.getQuantity(), item.getLineTotal());
        }
    }

    private static void requireArgs(String[] c, int count, String usage) {
        if (c.length != count) {
            throw new IllegalArgumentException("формат: " + usage);
        }
    }

    // ===== Вспомогательные методы =====
    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    // Сообщение исключения конструктора Product или "OK"
    private static String productError(String id, String name, double price) {
        try {
            new Product(id, name, price);
            return "OK";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // Сообщение исключения конструктора OrderItem или "OK"
    private static String itemError(int quantity) {
        try {
            new OrderItem(monitor(), quantity);
            return "OK";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private static int quantityOf(Order order, String productId) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                return item.getQuantity();
            }
        }
        return 0;
    }

    private static Order newOrder() {
        return new Order(new Employee("E01", "Степанов Владимир"));
    }

    private static Product monitor() {
        return new Product("M01", "Monitor 27", 32990);
    }

    private static Product keyboard() {
        return new Product("K01", "Keyboard", 4490);
    }
}
