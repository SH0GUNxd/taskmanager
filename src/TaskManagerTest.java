package taskmanager;

// Tests autonomes — pas de dépendance externe (pas de JUnit)
// Lancement : java -cp out taskmanager.TaskManagerTest

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

public class TaskManagerTest {

    // Mini framework d'assertions
    private static int passed = 0;
    private static int failed = 0;

    private static void ok(String testName) {
        System.out.printf("  [PASS] %s%n", testName);
        passed++;
    }

    private static void fail(String testName, String reason) {
        System.out.printf("  [FAIL] %s — %s%n", testName, reason);
        failed++;
    }

    private static void assertEquals(String test, Object expected, Object actual) {
        if (expected.equals(actual)) {
            ok(test);
        } else {
            fail(test, "expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertTrue(String test, boolean condition) {
        if (condition) ok(test); else fail(test, "condition was false");
    }

    private static void assertFalse(String test, boolean condition) {
        if (!condition) ok(test); else fail(test, "condition was true");
    }

    private static void section(String name) {
        System.out.println("\n  -- " + name + " --");
    }

    // Helpers

    private static Path tmpFile() throws IOException {
        return Files.createTempFile("taskmanager_test_", ".json");
    }

    // Tests Task.toJson / Task.fromJson
    
    static void testJsonRoundtrip() {
        section("Task — sérialisation JSON");

        Task original = new Task(1, "Titre simple", "Description", LocalDate.of(2025, 6, 1), Task.Status.TODO);
        Task restored = Task.fromJson(original.toJson());

        assertEquals("id préservé",          original.getId(),          restored.getId());
        assertEquals("titre préservé",       original.getTitle(),       restored.getTitle());
        assertEquals("description préservée",original.getDescription(), restored.getDescription());
        assertEquals("date préservée",       original.getDueDate(),     restored.getDueDate());
        assertEquals("statut préservé",      original.getStatus(),      restored.getStatus());
    }

    static void testJsonEscaping() {
        section("Task — caractères spéciaux dans JSON");

        Task t = new Task(2, "Titre avec \"guillemets\"", "Desc avec \\backslash", LocalDate.of(2025, 1, 1), Task.Status.DOING);
        Task restored = Task.fromJson(t.toJson());

        assertEquals("guillemets roundtrip",  "Titre avec \"guillemets\"", restored.getTitle());
        assertEquals("backslash roundtrip",   "Desc avec \\backslash",    restored.getDescription());
    }

    static void testJsonAllStatuses() {
        section("Task — tous les statuts");

        for (Task.Status s : Task.Status.values()) {
            Task t = new Task(3, "T", "D", LocalDate.now(), s);
            Task restored = Task.fromJson(t.toJson());
            assertEquals("statut " + s + " roundtrip", s, restored.getStatus());
        }
    }

    static void testJsonEmptyDescription() {
        section("Task — description vide");

        Task t = new Task(4, "Titre", "", LocalDate.of(2026, 12, 31), Task.Status.DONE);
        Task restored = Task.fromJson(t.toJson());
        assertEquals("description vide préservée", "", restored.getDescription());
    }

    static void testStatusParseIgnoresCase() {
        section("Task.Status — insensible à la casse");

        assertEquals("todo minuscule",  Task.Status.TODO,  Task.Status.parseStatus("todo"));
        assertEquals("Doing mixte",     Task.Status.DOING, Task.Status.parseStatus("Doing"));
        assertEquals("DONE majuscule",  Task.Status.DONE,  Task.Status.parseStatus("DONE"));
    }

    static void testStatusParseInvalid() {
        section("Task.Status — valeur invalide");

        try {
            Task.Status.parseStatus("INVALID");
            fail("statut invalide lève exception", "aucune exception levée");
        } catch (IllegalArgumentException e) {
            ok("statut invalide lève IllegalArgumentException");
        }
    }

    // Tests TaskManager CRUD
    
    static void testAddAndGet() throws IOException {
        section("TaskManager — ajout et récupération");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());

        assertTrue("liste vide au départ", tm.getAllTasks().isEmpty());

        Task t = tm.addTask("Tache A", "Desc A", LocalDate.of(2025, 6, 1), Task.Status.TODO);
        assertEquals("ID = 1",      1,        t.getId());
        assertEquals("titre",       "Tache A", t.getTitle());
        assertEquals("total = 1",   1,        tm.getTotalTaskCount());

        Files.deleteIfExists(f);
    }

    static void testFindById() throws IOException {
        section("TaskManager — recherche par ID");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());
        tm.addTask("A", "d", LocalDate.now(), Task.Status.TODO);
        tm.addTask("B", "d", LocalDate.now(), Task.Status.TODO);

        Optional<Task> found = tm.findTaskById(1);
        assertTrue("trouve ID 1",        found.isPresent());
        assertEquals("bon titre",        "A", found.get().getTitle());

        Optional<Task> missing = tm.findTaskById(99);
        assertFalse("ID 99 absent",      missing.isPresent());

        Files.deleteIfExists(f);
    }

    static void testUpdate() throws IOException {
        section("TaskManager — modification");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());
        tm.addTask("Original", "d", LocalDate.of(2025, 1, 1), Task.Status.TODO);

        boolean ok = tm.updateTask(1, "Modifie", null, null, Task.Status.DONE);
        assertTrue("updateTask retourne true", ok);

        Task updated = tm.findTaskById(1).get();
        assertEquals("titre modifié",  "Modifie",       updated.getTitle());
        assertEquals("statut modifié", Task.Status.DONE, updated.getStatus());
        assertEquals("date inchangée", LocalDate.of(2025, 1, 1), updated.getDueDate());

        assertFalse("ID inexistant retourne false", tm.updateTask(99, "X", null, null, null));

        Files.deleteIfExists(f);
    }

    static void testDelete() throws IOException {
        section("TaskManager — suppression");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());
        tm.addTask("A", "d", LocalDate.now(), Task.Status.TODO);
        tm.addTask("B", "d", LocalDate.now(), Task.Status.TODO);

        assertTrue("supprime ID 1",             tm.deleteTask(1));
        assertEquals("total = 1 après delete",  1, tm.getTotalTaskCount());
        assertFalse("ID 1 plus trouvable",       tm.findTaskById(1).isPresent());
        assertFalse("delete ID inexistant",      tm.deleteTask(99));

        Files.deleteIfExists(f);
    }

    static void testFilterByStatus() throws IOException {
        section("TaskManager — filtre par statut");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());
        tm.addTask("A", "d", LocalDate.now(), Task.Status.TODO);
        tm.addTask("B", "d", LocalDate.now(), Task.Status.DOING);
        tm.addTask("C", "d", LocalDate.now(), Task.Status.DONE);
        tm.addTask("D", "d", LocalDate.now(), Task.Status.TODO);

        assertEquals("2 TODO",  2, tm.getTasksByStatus(Task.Status.TODO).size());
        assertEquals("1 DOING", 1, tm.getTasksByStatus(Task.Status.DOING).size());
        assertEquals("1 DONE",  1, tm.getTasksByStatus(Task.Status.DONE).size());
        assertEquals("count TODO == size", tm.getTasksByStatus(Task.Status.TODO).size(),
                                           tm.countTasksByStatus(Task.Status.TODO));

        Files.deleteIfExists(f);
    }

    static void testIdAutoIncrement() throws IOException {
        section("TaskManager — auto-incrément des IDs");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());
        Task t1 = tm.addTask("A", "d", LocalDate.now(), Task.Status.TODO);
        Task t2 = tm.addTask("B", "d", LocalDate.now(), Task.Status.TODO);
        Task t3 = tm.addTask("C", "d", LocalDate.now(), Task.Status.TODO);

        assertTrue("IDs croissants", t1.getId() < t2.getId() && t2.getId() < t3.getId());

        Files.deleteIfExists(f);
    }

    // Tests de persistence (sauvegarde + rechargement)
    
    static void testPersistence() throws IOException {
        section("TaskManager — persistence sur disque");

        Path f = tmpFile();

        // Session 1 : on crée des tâches
        TaskManager tm1 = new TaskManager(f.toString());
        tm1.addTask("Persistante", "desc", LocalDate.of(2026, 3, 15), Task.Status.DOING);
        tm1.addTask("Persistante 2", "desc2", LocalDate.of(2026, 4, 1), Task.Status.DONE);

        // Session 2 : nouveau TaskManager sur le même fichier
        TaskManager tm2 = new TaskManager(f.toString());
        assertEquals("2 tâches rechargées",    2,           tm2.getTotalTaskCount());
        assertEquals("titre OK après reload",  "Persistante", tm2.findTaskById(1).get().getTitle());
        assertEquals("statut OK après reload", Task.Status.DOING, tm2.findTaskById(1).get().getStatus());

        Files.deleteIfExists(f);
    }

    static void testPersistenceWithSpecialChars() throws IOException {
        section("TaskManager — persistence avec caractères spéciaux");

        Path f = tmpFile();

        TaskManager tm1 = new TaskManager(f.toString());
        tm1.addTask("Titre \"quoted\"", "Desc \\backslash", LocalDate.of(2025, 1, 1), Task.Status.TODO);

        TaskManager tm2 = new TaskManager(f.toString());
        Task reloaded = tm2.findTaskById(1).get();
        assertEquals("guillemets préservés",  "Titre \"quoted\"",   reloaded.getTitle());
        assertEquals("backslash préservé",    "Desc \\backslash",   reloaded.getDescription());

        Files.deleteIfExists(f);
    }

    static void testIdContinuesAfterReload() throws IOException {
        section("TaskManager — IDs ne repartent pas de 1 après reload");

        Path f = tmpFile();

        TaskManager tm1 = new TaskManager(f.toString());
        tm1.addTask("A", "d", LocalDate.now(), Task.Status.TODO);
        tm1.addTask("B", "d", LocalDate.now(), Task.Status.TODO);

        TaskManager tm2 = new TaskManager(f.toString());
        Task t3 = tm2.addTask("C", "d", LocalDate.now(), Task.Status.TODO);
        assertTrue("nouvel ID > 2 après reload", t3.getId() > 2);

        Files.deleteIfExists(f);
    }

    static void testAtomicWriteNoPollution() throws IOException {
        section("TaskManager — pas de fichier .tmp résiduel");

        Path f = tmpFile();
        TaskManager tm = new TaskManager(f.toString());
        tm.addTask("A", "d", LocalDate.now(), Task.Status.TODO);

        Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
        assertFalse("pas de .tmp après sauvegarde", Files.exists(tmp));

        Files.deleteIfExists(f);
    }

    static void testEmptyFileHandled() throws IOException {
        section("TaskManager — fichier JSON vide");

        Path f = tmpFile();
        Files.writeString(f, "");

        TaskManager tm = new TaskManager(f.toString());
        assertEquals("0 tâches depuis fichier vide", 0, tm.getTotalTaskCount());

        Files.deleteIfExists(f);
    }

    static void testEmptyArrayHandled() throws IOException {
        section("TaskManager — fichier JSON []");

        Path f = tmpFile();
        Files.writeString(f, "[]");

        TaskManager tm = new TaskManager(f.toString());
        assertEquals("0 tâches depuis []", 0, tm.getTotalTaskCount());

        Files.deleteIfExists(f);
    }

    // Point d'entrée
    
    public static void main(String[] args) throws IOException {
        System.out.println("\n  ========================================");
        System.out.println("  TASKMANAGER — TESTS");
        System.out.println("  ========================================");

        // Task serialization
        testJsonRoundtrip();
        testJsonEscaping();
        testJsonAllStatuses();
        testJsonEmptyDescription();
        testStatusParseIgnoresCase();
        testStatusParseInvalid();

        // TaskManager CRUD
        testAddAndGet();
        testFindById();
        testUpdate();
        testDelete();
        testFilterByStatus();
        testIdAutoIncrement();

        // Persistence
        testPersistence();
        testPersistenceWithSpecialChars();
        testIdContinuesAfterReload();
        testAtomicWriteNoPollution();
        testEmptyFileHandled();
        testEmptyArrayHandled();

        // Résumé
        System.out.println("\n  ========================================");
        System.out.printf("  %d/%d tests passés%n", passed, passed + failed);
        System.out.println("  ========================================\n");

        if (failed > 0) System.exit(1);
    }
}
