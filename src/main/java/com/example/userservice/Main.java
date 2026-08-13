package com.example.userservice;

import com.example.userservice.config.HibernateUtil;
import com.example.userservice.dao.UserDAO;
import com.example.userservice.dao.UserDAOImpl;
import com.example.userservice.exception.DAOException;
import com.example.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final UserDAO userDAO = new UserDAOImpl();

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("   User Service  (Hibernate + PostgreSQL)");
        System.out.println("=============================================");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> handleCreate(scanner);
                        case "2" -> handleFind(scanner);
                        case "3" -> handleList();
                        case "4" -> handleUpdate(scanner);
                        case "5" -> handleDelete(scanner);
                        case "0" -> running = false;
                        default -> System.out.println("Неизвестная опция: " + choice);
                    }
                } catch (DAOException e) {
                    System.out.println("Ошибка БД: " + e.getMessage());
                    log.error("DAO-ошибка при выполнении операции", e);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите корректное число.");
                }
                System.out.println();
            }
        } finally {
            System.out.println("Завершение работы...");
            HibernateUtil.shutdown();
        }
    }

    // -------------------- menu actions --------------------

    private static void handleCreate(Scanner scanner) {
        String name = readNonEmpty(scanner, "Имя: ");
        String email = readNonEmpty(scanner, "Email: ");
        Integer age = readInt(scanner, "Возраст: ");
        User created = userDAO.create(new User(name, email, age));
        System.out.println("Создан пользователь:");
        printUser(created);
    }

    private static void handleFind(Scanner scanner) {
        long id = readLong(scanner, "ID: ");
        userDAO.findById(id).ifPresentOrElse(
                Main::printUser,
                () -> System.out.println("Пользователь не найден.")
        );
    }

    private static void handleList() {
        List<User> users = userDAO.findAll();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст.");
            return;
        }
        System.out.printf("%-5s %-20s %-25s %-8s %-20s%n",
                "ID", "Имя", "Email", "Возраст", "Создан");
        for (User u : users) {
            System.out.printf("%-5d %-20.20s %-25.25s %-8s %-20s%n",
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getAge() == null ? "" : u.getAge(),
                    u.getCreatedAt() == null ? "" : u.getCreatedAt().format(FMT));
        }
    }

    private static void handleUpdate(Scanner scanner) {
        long id = readLong(scanner, "ID для обновления: ");
        userDAO.findById(id).ifPresentOrElse(u -> {
            System.out.println("Текущие данные:");
            printUser(u);

            String name = readNonEmpty(scanner, "Новое имя [" + u.getName() + "]: ");
            String email = readNonEmpty(scanner, "Новый email [" + u.getEmail() + "]: ");
            int age = readInt(scanner, "Новый возраст [" + u.getAge() + "]: ");

            u.setName(name);
            u.setEmail(email);
            u.setAge(age);

            User updated = userDAO.update(u);
            System.out.println("Обновлено:");
            printUser(updated);
        }, () -> System.out.println("Пользователь не найден."));
    }

    private static void handleDelete(Scanner scanner) {
        long id = readLong(scanner, "ID для удаления: ");
        userDAO.findById(id).ifPresentOrElse(u -> {
            userDAO.deleteById(id);
            System.out.println("Удалён пользователь с ID=" + id);
        }, () -> System.out.println("Пользователь не найден."));
    }

    // -------------------- helpers --------------------

    private static void printMenu() {
        System.out.println("----- Меню -----");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти по ID");
        System.out.println("3. Показать всех");
        System.out.println("4. Обновить");
        System.out.println("5. Удалить");
        System.out.println("0. Выход");
        System.out.print("Выбор: ");
    }

    private static void printUser(User u) {
        System.out.println("---------------------------------------------");
        System.out.println("ID:      " + u.getId());
        System.out.println("Имя:     " + u.getName());
        System.out.println("Email:   " + u.getEmail());
        System.out.println("Возраст: " + u.getAge());
        System.out.println("Создан:  " + (u.getCreatedAt() == null ? "-" : u.getCreatedAt().format(FMT)));
        System.out.println("---------------------------------------------");
    }

    private static String readNonEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Значение не может быть пустым.");
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    private static long readLong(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }
}