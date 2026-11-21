package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        String enableForeignKeys = "PRAGMA foreign_keys = ON;";

        // TABLE 4: USERS
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                );
                """;

        // TABLE 1: STUDENTS
        String studentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name TEXT NOT NULL,
                    course TEXT NOT NULL,
                    year INTEGER NOT NULL,
                    level TEXT NOT NULL,
                    section TEXT NOT NULL,
                    contact_number TEXT
                );
                """;

        // TABLE 2: TEACHERS
        String teachersTable = """
                CREATE TABLE IF NOT EXISTS teachers (
                    teacher_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    teacher_name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    subject TEXT NOT NULL,
                    section TEXT NOT NULL
                );
                """;

        // Supporting table referenced by GRADES.subject_id [FK]
        String subjectsTable = """
                CREATE TABLE IF NOT EXISTS subjects (
                    subject_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    subject_code TEXT NOT NULL UNIQUE,
                    subject_name TEXT NOT NULL
                );
                """;

        // TABLE 3: ATTENDANCE
        String attendanceTable = """
                CREATE TABLE IF NOT EXISTS attendance (
                    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (student_id) REFERENCES students(student_id)
                        ON DELETE CASCADE
                );
                """;

        // TABLE 5: GRADES
        String gradesTable = """
                CREATE TABLE IF NOT EXISTS grades (
                    grade_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    subject_id INTEGER NOT NULL,
                    grade_value REAL NOT NULL,
                    term TEXT NOT NULL,
                    FOREIGN KEY (student_id) REFERENCES students(student_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
                        ON DELETE RESTRICT
                );
                """;

        // Activity Logs Table
        String activityLogsTable = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                        ON DELETE CASCADE
                );
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(enableForeignKeys);
            stmt.execute(usersTable);
            stmt.execute(studentsTable);
            stmt.execute(teachersTable);
            stmt.execute(subjectsTable);
            stmt.execute(attendanceTable);
            stmt.execute(gradesTable);
            stmt.execute(activityLogsTable);

            System.out.println("-> Database initialized.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }
}