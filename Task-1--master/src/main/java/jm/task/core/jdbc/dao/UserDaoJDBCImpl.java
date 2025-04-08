package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao, AutoCloseable {
    private Connection connection;

    public UserDaoJDBCImpl() {
        try {
            this.connection = Util.getConnection();
            this.connection.setAutoCommit(false); // Отключаем авто-коммит
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL, " +
                "last_name VARCHAR(50) NOT NULL, " +
                "age SMALLINT NOT NULL)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            connection.commit();
        } catch (SQLException e) {
            rollbackConnection();
            throw new RuntimeException("Failed to create table", e);
        }
    }

    @Override
    public void dropUsersTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS users");
            connection.commit();
        } catch (SQLException e) {
            rollbackConnection();
            throw new RuntimeException("Failed to drop table", e);
        }
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        String sql = "INSERT INTO public.users (name, last_name, age) VALUES (?, ?, ?)";

        try (PreparedStatement pstm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, name);
            pstm.setString(2, lastName);
            pstm.setByte(3, age);

            int affectedRows = pstm.executeUpdate();
            connection.commit();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected");
            }

            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    System.out.println("User created with ID: " + id);
                }
            }
        } catch (SQLException e) {
            rollbackConnection();
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeUserById(long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            pstm.setLong(1, id);
            int affectedRows = pstm.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("No user found with ID: " + id);
            }
            connection.commit();
        } catch (SQLException e) {
            rollbackConnection();
            throw new RuntimeException("Failed to remove user", e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>(); // Всегда возвращаем новый список

        String sql = "SELECT id, name, last_name, age FROM users";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("name"),
                        rs.getString("last_name"),
                        rs.getByte("age")
                );
                user.setId(rs.getLong("id"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get users", e);
        }

        return users;
    }

    @Override
    public void cleanUsersTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE users RESTART IDENTITY");
            connection.commit();
        } catch (SQLException e) {
            rollbackConnection();
            throw new RuntimeException("Failed to clean table", e);
        }
    }

    private void rollbackConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to rollback transaction", ex);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
