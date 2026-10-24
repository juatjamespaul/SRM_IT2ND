package com.scc.ui;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Authentication {

    // TODO: make this match the roles in Section 2 of your approved System Proposal
    private static final String[] ROLES = {"ADMIN", "TEACHER", "STUDENT", "STAFF"};

    private final UserDao userDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start() {
        while (true) {
            System.out.println("\n=========================================");
            System.out.println(" STUDENT SERVICE QUEUE MANAGEMENT SYSTEM ");
            System.out.println("=========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            switch (readInt()) {
                case 1 -> login();
                case 2 -> register();
                case 3 -> {
                    System.out.println("\nThank you for using SSQMS!");
                    return;
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    private void login() {
        System.out.println("\n============ LOGIN ===========");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String role = userDao.loginUser(username, password);
        if (role == null) {
            System.out.println("Invalid credentials, or account is not yet approved.");
            return;
        }
        System.out.println("\nLogin successful! Role: " + role);
        routeByRole(role, username);
    }

    private void register() {
        System.out.println("\n=========== REGISTER ==========");
        System.out.print("Enter new username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter new password: ");
        String password = sc.nextLine();

        System.out.println("Select role:");
        for (int i = 0; i < ROLES.length; i++) {
            System.out.printf("[%d] %s%n", i + 1, ROLES[i]);
        }
        System.out.print("Choice: ");
        int choice = readInt();

        if (choice < 1 || choice > ROLES.length || username.isEmpty() || password.isEmpty()) {
            System.out.println("Invalid input, registration cancelled.");
            return;
        }

        if (userDao.registerUser(username, password, ROLES[choice - 1])) {
            System.out.println("-> Registration successful, waiting for approval!");
        }
    }

    private void routeByRole(String role, String username) {
        switch (role) {
            case "ADMIN"   -> new AdminMenu(userDao, sc, username).show();
            case "TEACHER" -> new TeacherMenu(userDao, sc, username).show();
            case "STUDENT" -> new StudentMenu(userDao, sc, username).show();
            case "STAFF"   -> new StaffMenu(userDao, sc, username).show();
            default -> System.out.println("No menu available for role: " + role);
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}