package com.nexushire.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens JDBC connections to MySQL. Settings come from environment variables, with local defaults. */
public final class DBConnection {

private static final String URL =
getenv("DB_URL", "jdbc:mysql://localhost:3306/nexushire?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
private static final String USER = getenv("DB_USER", "root");
private static final String PASSWORD = getenv("DB_PASSWORD", "Sagnik@24");

private DBConnection() {}

public static Connection getConnection() throws SQLException {
return DriverManager.getConnection(URL, USER, PASSWORD);
 }

 private static String getenv(String key, String fallback) {
String value = System.getenv(key);
return (value == null || value.isBlank()) ? fallback : value;
}
}