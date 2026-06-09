package repository;

import model.BaseEntity.BaseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class CsvRepository<T extends BaseEntity> {

    protected final String filePath;

    /*
        basic thread-safe
        mọi CRUD đều đi qua lock này
     */
    protected final Object lock = new Object();

    protected CsvRepository(String filePath) {
        this.filePath = filePath;
    }

    // =====================================================
    // ABSTRACT METHODS
    // =====================================================

    protected abstract T mapFromCsv(String csvLine);

    // =====================================================
    // FIND ALL
    // =====================================================

    public List<T> findAll() {

        synchronized (lock) {

            List<T> result = new ArrayList<>();

            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                return result;
            }

            try (BufferedReader reader =
                         new BufferedReader(
                                 new FileReader(filePath))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line != null && !line.trim().isEmpty()) {
                        if (line.startsWith("id,")) {
                            continue;
                        }

                        T entity = mapFromCsv(line);

                        if (entity != null) {
                            result.add(entity);
                        }
                    }
                }
            } catch (IOException e) {

            System.out.println(
                "[ERROR] " + e.getMessage()
            );
        }

            return result;
        }
    }

    // =====================================================
    // FIND BY ID
    // =====================================================

    public T findById(String id) {

        List<T> entities = findAll();

        for (T entity : entities) {

            if (entity.getId().equals(id)) {
                return entity;
            }
        }

        return null;
    }

    // =====================================================
    // SAVE
    // =====================================================

    public void save(T entity) {

        synchronized (lock) {

            try (BufferedWriter writer =
                         new BufferedWriter(
                                 new FileWriter(
                                         filePath,
                                         true
                                 ))) {

                writer.write(
                        entity.toCsvLine()
                );

                writer.newLine();
            } catch (IOException e) {

            System.out.println(
                "[ERROR] " + e.getMessage()
            );
        }
        }
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public boolean update(T updatedEntity) {

        synchronized (lock) {

            List<T> entities = findAll();

            boolean found = false;

            for (int i = 0; i < entities.size(); i++) {

                if (entities.get(i)
                        .getId()
                        .equals(updatedEntity.getId())) {

                    entities.set(i, updatedEntity);

                    found = true;

                    break;
                }
            }

            if (!found) {
                return false;
            }

            rewriteFile(entities);

            return true;
        }
    }

    // =====================================================
    // DELETE
    // =====================================================

    public boolean delete(String id)
            throws IOException {

        synchronized (lock) {

            List<T> entities = findAll();

            boolean removed =
                    entities.removeIf(
                            entity ->
                                    entity.getId()
                                            .equals(id)
                    );

            if (!removed) {
                return false;
            }

            rewriteFile(entities);

            return true;
        }
    }

    // =====================================================
    // REWRITE CSV
    // =====================================================

    protected void rewriteFile(List<T> entities) {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(
                                     filePath,
                                     false
                             ))) {

            for (T entity : entities) {

                writer.write(
                        entity.toCsvLine()
                );

                writer.newLine();
            } 
        } catch (IOException e) {

            System.out.println(
                "[ERROR] " + e.getMessage()
            );
        }
    }
}
