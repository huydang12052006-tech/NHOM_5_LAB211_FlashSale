package repository;

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
}
