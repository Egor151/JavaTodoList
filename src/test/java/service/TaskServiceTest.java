package service;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.TaskRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {
    private TaskService service;
    private TaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TaskRepository();
        service = new TaskService(repository);
    }

    @Test
    void addTask_ShouldAddTaskWithCorrectData() {
        Task task = service.addTask("Test", "Description", LocalDate.now());

        assertNotNull(task.getId());
        assertEquals("Test", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(LocalDate.now(), task.getDueDate());
        assertEquals(Status.TODO, task.getStatus());
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        service.addTask("Task 1", "Desc 1", LocalDate.now());
        service.addTask("Task 2", "Desc 2", LocalDate.now().plusDays(1));

        List<Task> tasks = service.getAllTasks();

        assertEquals(2, tasks.size());
    }

    @Test
    void updateTask_ShouldUpdateFields() {
        Task task = service.addTask("Original", "Original", LocalDate.now());

        Task updated = service.updateTask(
                task.getId(),
                "Updated",
                "Updated",
                LocalDate.now().plusDays(1),
                Status.IN_PROGRESS);

        assertNotNull(updated);
        assertEquals("Updated", updated.getTitle());
        assertEquals("Updated", updated.getDescription());
        assertEquals(LocalDate.now().plusDays(1), updated.getDueDate());
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void deleteTask_ShouldRemoveTask() {
        Task task = service.addTask("To delete", "Desc", LocalDate.now());

        boolean result = service.deleteTask(task.getId());

        assertTrue(result);
        assertNull(repository.findById(task.getId()));
    }

    @Test
    void filterByStatus_ShouldReturnOnlyTasksWithSpecifiedStatus() {
        Task todoTask = service.addTask("Todo", "Desc", LocalDate.now());
        Task doneTask = service.addTask("Done", "Desc", LocalDate.now());
        service.updateTask(doneTask.getId(), null, null, null, Status.DONE);

        List<Task> doneTasks = service.filterByStatus(Status.DONE);

        assertEquals(1, doneTasks.size());
        assertEquals(doneTask.getId(), doneTasks.get(0).getId());
    }

    @Test
    void sortByDueDate_ShouldReturnTasksInCorrectOrder() {
        Task lateTask = service.addTask("Late", "Desc", LocalDate.now().plusDays(2));
        Task earlyTask = service.addTask("Early", "Desc", LocalDate.now());

        List<Task> sorted = service.sortByDueDate();

        assertEquals(earlyTask.getId(), sorted.get(0).getId());
        assertEquals(lateTask.getId(), sorted.get(1).getId());
    }

    @Test
    void sortByStatus_ShouldReturnTasksInCorrectOrder() {
        Task doneTask = service.addTask("Done", "Desc", LocalDate.now());
        service.updateTask(doneTask.getId(), null, null, null, Status.DONE);
        Task inProgressTask = service.addTask("In Progress", "Desc", LocalDate.now());
        service.updateTask(inProgressTask.getId(), null, null, null, Status.IN_PROGRESS);
        Task todoTask = service.addTask("Todo", "Desc", LocalDate.now());

        List<Task> sorted = service.sortByStatus();

        assertEquals(todoTask.getId(), sorted.get(0).getId());
        assertEquals(inProgressTask.getId(), sorted.get(1).getId());
        assertEquals(doneTask.getId(), sorted.get(2).getId());
    }
}