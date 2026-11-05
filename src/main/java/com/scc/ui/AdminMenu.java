package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    // NEW: valid choices for the update feature (keep the same as the roles in Authentication)
    private static final String[] VALID_ROLES = {"ADMIN", "TEACHER", "STUDENT", "STAFF"};
    private static final String[] VALID_STATUSES = {"APPROVED", "PENDING"};

    private final UserDao userDao;
    private final Scanner sc;
    private final String username;

    public AdminMenu(UserDao userDao, Scanner sc, String username) {
        this.userDao = userDao;
        this.sc = sc;
        this.username = username;
    }

    public void show() {
        while (true) {
            System.out.println("\n===== ADMIN MENU (" + username + ") =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User (Role & Status)");   // NEW
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            switch (readInt()) {
                case 1 -> printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                case 2 -> printUserTable(userDao.getPendingUsers(), "PENDING REGISTRATIONS");
                case 3 -> approveUser();
                case 4 -> updateUser();                              // NEW
                case 0 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending, "PENDING REGISTRATIONS");

        if (pending.isEmpty()) {
            return;
        }

        while (true) {
            System.out.print("Enter Target User ID to approve (0 to cancel): ");
            int id = readInt();

            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean inPendingList = pending.stream().anyMatch(u -> u.id() == id);
            if (!inPendingList) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(id)) {
                System.out.println("-> Success! User ID " + id + " has been APPROVED.");
            } else {
                System.out.println("-> Approval failed. Please try again.");
            }
            return;
        }
    }

    // ===================== NEW (update-user requirement) =====================

    /** Admin changes another user's role and status. Enter 0 at any step to cancel. */
    private void updateUser() {
        int currentAdminId = userDao.getUserIdByUsername(username);   // id of the logged-in admin

        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL SYSTEM USERS");
        if (users.isEmpty()) {
            return;
        }

        // Step 1: target user ID
        User target = null;
        while (target == null) {
            int targetUserId = readIntInput("Enter target User ID (0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (targetUserId == currentAdminId) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }
            for (User u : users) {
                if (u.id() == targetUserId) {
                    target = u;
                }
            }
            if (target == null) {
                System.out.println("User ID " + targetUserId + " not found. Try again.");
            }
        }

        System.out.println("\nSelected: " + target.username() + " | role: " + target.role()
                + " | status: " + target.status());

        // Step 2: new role
        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Step 3: new status
        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(target.id(), newRole, newStatus)) {
            System.out.println("-> Success! User ID " + target.id() + " (" + target.username()
                    + ") is now " + newRole + " / " + newStatus + ".");
        } else {
            System.out.println("-> Update failed. Please try again.");
        }
    }

    /** Keeps asking until a whole number is typed, so letters/symbols never crash the app. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number (digits only).");
            }
        }
    }

    /** Only accepts a valid role. Returns null if the admin enters 0 to cancel. */
    private String readValidRole() {
        String roleList = String.join(" / ", VALID_ROLES);
        while (true) {
            System.out.print("Enter new role [" + roleList + "] (0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }
            for (String role : VALID_ROLES) {
                if (role.equals(input)) {
                    return role;
                }
            }
            System.out.println("Invalid role! Please type one of: " + roleList);
        }
    }

    /** Only accepts APPROVED or PENDING. Returns null if the admin enters 0 to cancel. */
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status [APPROVED / PENDING] (0 to cancel): ");
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }
            for (String status : VALID_STATUSES) {
                if (status.equals(input)) {
                    return status;
                }
            }
            System.out.println("Invalid status! Please type APPROVED or PENDING.");
        }
    }

    // =========================================================================

    /** Console table helper: aligned headers, borders and rows. */
    private void printUserTable(List<User> users, String title) {
        String border = "+-------+-----------------+--------------+------------+";
        String format = "| %-5s | %-15s | %-12s | %-10s |%n";

        System.out.println("\n" + title);
        if (users.isEmpty()) {
            System.out.println("(no records found)");
            return;
        }

        System.out.println(border);
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}