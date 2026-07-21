package repository;

import java.util.List;

import model.Entity.User;

public class UserRepository extends CsvRepository<User> {

    public UserRepository() {
        super("data/users.csv");
    }

    public UserRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected User mapFromCsv(String csvLine) {
        User user = new User();

        try {
            user.fromCsvLine(csvLine);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Persists a new user to CSV storage.
     */
    public void register(User user) {
        save(user);
    }

    /**
     * Finds a user by username (case-insensitive). Returns null when not found.
     */
    public User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        for (User user : findAll()) {
            if (username.equalsIgnoreCase(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Validates a login attempt against the stored password hash.
     * The caller supplies an already-hashed password so this layer stays free
     * of business logic.
     */
    public User validateLogin(String username, String passwordHash) {
        User user = findByUsername(username);
        if (user == null || !user.isActive()) {
            return null;
        }
        if (user.getPasswordHash() != null && user.getPasswordHash().equals(passwordHash)) {
            return user;
        }
        return null;
    }

    /**
     * Updates an existing user and rewrites the CSV file.
     */
    public boolean updateUser(User user) {
        return update(user);
    }

    /**
     * Generates the next user ID based on the max existing Uxxxxx.
     */
    public String generateNextId() {
        int maxNumber = 0;
        List<User> users = findAll();
        for (User user : users) {
            String id = user.getId();
            if (id != null && id.matches("U\\d+")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(1)));
            }
        }
        return String.format("U%05d", maxNumber + 1);
    }
}
