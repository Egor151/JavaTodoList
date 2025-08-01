package controller;

import exception.InvalidDateException;
import exception.InvalidStatusException;
import exception.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import model.Status;
import model.Task;
import service.TaskService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@RequiredArgsConstructor
public class TaskController {
    private final TaskService service;
    private final Scanner scanner;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void start() {
        System.out.println("Добро пожаловать в TODO приложение!");
        printHelp();

        while (true) {
            System.out.print("\nВведите команду: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "add":
                    addTask();
                    break;
                case "list":
                    listTasks();
                    break;
                case "edit":
                    editTask();
                    break;
                case "delete":
                    deleteTask();
                    break;
                case "filter":
                    filterTasks();
                    break;
                case "sort":
                    sortTasks();
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("Выход из приложения...");
                    return;
                default:
                    System.out.println("Неизвестная команда. Введите 'help' для списка команд.");
            }
        }
    }

    private void printHelp() {
        System.out.println("\nДоступные команды:");
        System.out.println("add    - Добавить новую задачу");
        System.out.println("list   - Показать все задачи");
        System.out.println("edit   - Редактировать существующую задачу");
        System.out.println("delete - Удалить задачу");
        System.out.println("filter - Показать задачи с определённым статусом");
        System.out.println("sort   - Отсортировать задачи");
        System.out.println("help   - Показать справку");
        System.out.println("exit   - Завершить работу приложения");
    }

    private void addTask() {
        System.out.println("\nДобавление новой задачи:");

        System.out.print("Введите название: ");
        String title = scanner.nextLine();

        System.out.print("Введите описание: ");
        String description = scanner.nextLine();

        LocalDate dueDate = readDate();

        try {
            Task task = service.addTask(title, description, dueDate);
            System.out.println("Задача добавлена с ID: " + task.getId());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private LocalDate readDate() {
        while (true) {
            System.out.print("Введите срок выполнения (дд.мм.гггг): ");
            String dateInput = scanner.nextLine();
            try {
                return LocalDate.parse(dateInput, dateFormatter);
            } catch (DateTimeParseException e) {
                throw new InvalidDateException("Неверный формат даты: " + dateInput +
                        ". Ожидается формат дд.мм.гггг. Попробуйте снова.");
            }
        }
    }

    private void listTasks() {
        List<Task> tasks = service.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст.");
            return;
        }

        System.out.println("\nСписок всех задач:");
        tasks.forEach(this::printTask);
    }

    private void editTask() {
        System.out.println("\nРедактирование задачи:");

        Long id = readTaskId();
        if (id == null) return;

        try {
            System.out.print("Введите новое название (оставьте пустым, чтобы не менять): ");
            String title = scanner.nextLine();

            System.out.print("Введите новое описание (оставьте пустым, чтобы не менять): ");
            String description = scanner.nextLine();

            LocalDate dueDate = null;
            System.out.print("Введите новый срок выполнения (дд.мм.гггг или оставьте пустым): ");
            String dateInput = scanner.nextLine();
            if (!dateInput.isEmpty()) {
                dueDate = LocalDate.parse(dateInput, dateFormatter);
            }

            Status status = null;
            System.out.print("Введите новый статус (TODO, IN_PROGRESS, DONE или оставьте пустым): ");
            String statusInput = scanner.nextLine();
            if (!statusInput.isEmpty()) {
                try {
                    status = Status.valueOf(statusInput.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new InvalidStatusException(statusInput);
                }
            }

            Task updatedTask = service.updateTask(id,
                    title.isEmpty() ? null : title,
                    description.isEmpty() ? null : description,
                    dueDate,
                    status);

            System.out.println("Задача обновлена:");
            printTask(updatedTask);

        } catch (TaskNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (InvalidDateException | InvalidStatusException e) {
            System.out.println("Ошибка ввода: " + e.getMessage());
        }
    }

    private void deleteTask() {
        System.out.println("\nУдаление задачи:");

        Long id = readTaskId();
        if (id == null) return;

        try {
            service.deleteTask(id);
            System.out.println("Задача с ID " + id + " удалена.");
        } catch (TaskNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void filterTasks() {
        System.out.println("\nФильтрация задач по статусу:");
        System.out.print("Введите статус (TODO, IN_PROGRESS, DONE): ");

        try {
            String statusInput = scanner.nextLine();
            Status status = Status.valueOf(statusInput.toUpperCase());
            List<Task> tasks = service.filterByStatus(status);

            if (tasks.isEmpty()) {
                System.out.println("Нет задач со статусом " + status);
            } else {
                System.out.println("Задачи со статусом " + status + ":");
                tasks.forEach(this::printTask);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: Неверный статус. Допустимые значения: " +
                    String.join(", ", Status.getNames()));
        }
    }

    private void sortTasks() {
        System.out.println("\nСортировка задач:");
        System.out.println("1 - По сроку выполнения");
        System.out.println("2 - По статусу");
        System.out.print("Выберите вариант сортировки: ");

        String choice = scanner.nextLine();
        List<Task> tasks;

        switch (choice) {
            case "1":
                tasks = service.sortByDueDate();
                System.out.println("Задачи отсортированы по сроку выполнения:");
                break;
            case "2":
                tasks = service.sortByStatus();
                System.out.println("Задачи отсортированы по статусу:");
                break;
            default:
                System.out.println("Неверный выбор. Возврат в главное меню.");
                return;
        }

        tasks.forEach(this::printTask);
    }

    private Long readTaskId() {
        System.out.print("Введите ID задачи: ");
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID. Должно быть число.");
            return null;
        }
    }

    private void printTask(Task task) {
        System.out.printf("ID: %d | %s | %s | %s | %s%n",
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate().format(dateFormatter),
                task.getStatus());
    }
}
