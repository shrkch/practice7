package ru.edu.pr04;

/**
 * Сотрудник. record: поля final, методы id() и fullName(), equals, hashCode, toString генерируются автоматически.
 * Компактный конструктор из starter_project проверяет, что данные сотрудника не пустые.
 */
public record Employee(String id, String fullName) {
    public Employee {
        if (id == null || id.isBlank() || fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Employee data");
        }
    }
}
