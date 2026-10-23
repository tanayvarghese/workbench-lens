package com.workbenchlens;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/** Persistence and password hashing for Workbench Lens application accounts. */
public final class UserRepository {
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String DB_USER = "WORKBENCH";
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public User register(String username, String email, char[] password) throws SQLException {
        String salt = newSalt();
        String encoded = hash(password, salt);
        try (Connection c = DriverManager.getConnection(URL, DB_USER, databasePassword());
             PreparedStatement count = c.prepareStatement("SELECT COUNT(*) FROM app_users");
             ResultSet rs = count.executeQuery()) {
            rs.next();
            // Bootstrap the first account as admin on this local prototype.
            String role = rs.getInt(1) == 0 ? "ADMIN" : "USER";
            try (PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO app_users (username,email,password_hash,user_role) VALUES (?,?,?,?)")) {
                insert.setString(1, username.trim());
                insert.setString(2, email.trim().toLowerCase(java.util.Locale.ROOT));
                insert.setString(3, "PBKDF2-SHA256$" + ITERATIONS + "$" + salt + "$" + encoded);
                insert.setString(4, role);
                insert.executeUpdate();
            }
            return new User(username.trim(), role);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) throw new SQLException("That username or email is already registered.", e);
            throw e;
        } finally { java.util.Arrays.fill(password, '\0'); }
    }

    public User authenticate(String usernameOrEmail, char[] password) throws SQLException {
        try (Connection c = DriverManager.getConnection(URL, DB_USER, databasePassword());
             PreparedStatement q = c.prepareStatement("SELECT username,user_role,password_hash FROM app_users WHERE LOWER(username)=LOWER(?) OR LOWER(email)=LOWER(?)")) {
            q.setString(1, usernameOrEmail.trim()); q.setString(2, usernameOrEmail.trim());
            try (ResultSet rs = q.executeQuery()) {
                if (!rs.next()) throw new SQLException("Invalid username/email or password.");
                String[] parts = rs.getString("password_hash").split("\\$", -1);
                if (parts.length != 4 || !"PBKDF2-SHA256".equals(parts[0])) throw new SQLException("Account password format is invalid.");
                String actual = hash(password, parts[2], Integer.parseInt(parts[1]));
                if (!java.security.MessageDigest.isEqual(actual.getBytes(java.nio.charset.StandardCharsets.US_ASCII), parts[3].getBytes(java.nio.charset.StandardCharsets.US_ASCII)))
                    throw new SQLException("Invalid username/email or password.");
                return new User(rs.getString("username"), rs.getString("user_role"));
            }
        } finally { java.util.Arrays.fill(password, '\0'); }
    }

    public List<Map<String,String>> listUsers() throws SQLException {
        var users = new java.util.ArrayList<Map<String,String>>();
        try (Connection c = DriverManager.getConnection(URL, DB_USER, databasePassword());
             PreparedStatement q = c.prepareStatement("SELECT user_id,username,email,user_role,created_at FROM app_users ORDER BY user_id");
             ResultSet rs = q.executeQuery()) {
            while (rs.next()) users.add(Map.of("id", rs.getString("user_id"), "username", rs.getString("username"), "email", rs.getString("email"), "role", rs.getString("user_role"), "createdAt", rs.getString("created_at")));
        }
        return users;
    }

    private static String databasePassword() {
        String p = System.getenv("WORKBENCH_DB_PASSWORD");
        if (p == null || p.isBlank()) throw new IllegalStateException("Set WORKBENCH_DB_PASSWORD before starting the app.");
        return p;
    }
    private static String newSalt() { byte[] b = new byte[16]; RANDOM.nextBytes(b); return Base64.getEncoder().encodeToString(b); }
    private static String hash(char[] password, String salt) { return hash(password, salt, ITERATIONS); }
    private static String hash(char[] password, String salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(salt), iterations, KEY_BITS);
        try { return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded()); }
        catch (GeneralSecurityException e) { throw new IllegalStateException("Password hashing is unavailable.", e); }
        finally { spec.clearPassword(); }
    }
    public record User(String username, String role) { }
}
