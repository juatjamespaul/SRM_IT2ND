
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.User;
import com.scc.model.ActivityLog;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    // Valid choices for the update feature
    private static final String[] VALID_ROLES = {"ADMIN", "TEACHER", "STUDENT", "STAFF"};
    private static final String[] VALID_STATUSES = {"APPROVED", "PENDING"};

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner sc;
    private final String username;

    public AdminMenu(UserDao userDao, ActivityLogDao activityLogDao,
                     Scanner sc, String username) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
        this.sc = sc;
        this.username = username;
    }

    public void show() {
        while (true) {
            System.out.println("\n===== ADMIN MENU (" + username + ") =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User (Role & Status)");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Clear Historical Audit Logs");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            switch (readInt()) {
                case 1 -> printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                case 2 -> printUserTable(userDao.getPendingUsers(), "PENDING REGISTRATIONS");
                case 3 -> approveUser();
                case 4 -> updateUser();
                case 5 -> viewAuditLogs();
                case 6 -> clearAuditLogs();

                case 0 -> {
                    System.out.println("Logged out.");
                    return;
                }

                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    // ===================== APPROVE USER =====================

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

                int adminId = userDao.getUserIdByUsername(username);

                activityLogDao.logAction(
                        adminId,
                        "USER_APPROVED",
                        "Admin [" + username + "] approved account for User ID " + id
                );

            } else {
                System.out.println("-> Approval failed. Please try again.");
            }

            return;
        }
    }

    // ===================== UPDATE USER =====================

    /** Admin changes another user's role and status. Enter 0 at any step to cancel. */
    private void updateUser() {

        int currentAdminId = userDao.getUserIdByUsername(username);

        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL SYSTEM USERS");

        if (users.isEmpty()) {
            return;
        }

        // Step 1: Target user ID
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

        System.out.println("\nSelected: " + target.username()
                + " | role: " + target.role()
                + " | status: " + target.status());

        // Step 2: New role
        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Step 3: New status
        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(target.id(), newRole, newStatus)) {

            System.out.println("-> Success! User ID " + target.id() + " (" + target.username()
                    + ") is now " + newRole + " / " + newStatus + ".");

            int adminId = userDao.getUserIdByUsername(username);

            activityLogDao.logAction(
                    adminId,
                    "USER_UPDATED",
                    "Admin [" + username + "] changed role of User ID " + target.id()
                            + " to " + newRole + " and status to " + newStatus
            );

        } else {
            System.out.println("-> Update failed. Please try again.");
        }
    }

    // ===================== INPUT VALIDATION =====================

    /** Keeps asking until a whole number is typed. */
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

    // ===================== ACTIVITY AUDIT LOGS =====================

    private void viewAuditLogs() {
        while (true) {
            System.out.println("\n========== SYSTEM AUDIT LOGS ==========");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            switch (readInt()) {

                case 1 -> displayAuditLogs(activityLogDao.getAllLogs());

                case 2 -> {

                    // Display all registered members
                    List<User> users = userDao.getAllUsers();

                    printUserTable(users, "ALL SYSTEM MEMBERS");

                    if (users.isEmpty()) {
                        System.out.println("No registered members found.");
                        break;
                    }

                    // Ask for User ID
                    int userId = readIntInput(
                            "\nEnter User ID to filter (0 to cancel): "
                    );

                    if (userId == 0) {
                        System.out.println("Filtering cancelled.");
                        break;
                    }

                    // Check if User ID exists
                    boolean userExists = users.stream()
                            .anyMatch(user -> user.id() == userId);

                    if (!userExists) {
                        System.out.println("User ID not found. Please try again.");
                        break;
                    }

                    // Display logs belonging to selected user
                    System.out.println("\nShowing activity logs for User ID: " + userId);

                    displayAuditLogs(activityLogDao.getLogsByUserId(userId));
                }

                case 0 -> {
                    return;
                }

                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    // Display logs in an aligned console table
    private void displayAuditLogs(List<ActivityLog> logs) {

        System.out.println("\n==================== ACTIVITY AUDIT LOGS ====================");

        if (logs.isEmpty()) {
            System.out.println("(no activity logs found)");
            return;
        }

        String border = "+-------+----------+--------------------+-------------------------------------+----------------------+";

        String format = "| %-5s | %-8s | %-18s | %-35s | %-20s |%n";

        System.out.println(border);

        System.out.printf(
                format,
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(border);

        for (ActivityLog log : logs) {

            System.out.printf(
                    format,
                    log.id(),
                    log.userId(),
                    truncate(log.actionType(), 18),
                    truncate(log.description(), 35),
                    truncate(log.timestamp(), 20)
            );
        }

        System.out.println(border);
    }

    // Prevent text from breaking the table alignment
    private String truncate(String value, int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() > maxLength) {
            return value.substring(0, maxLength - 3) + "...";
        }

        return value;
    }

    // Clear historical activity logs safely
    private void clearAuditLogs() {

        System.out.println("\n========== CLEAR HISTORICAL LOGS ==========");
        System.out.println("WARNING: This will permanently delete all activity logs.");
        System.out.print("Are you sure? Type YES to confirm: ");

        String confirmation = sc.nextLine().trim();

        if (!confirmation.equals("YES")) {
            System.out.println("Operation cancelled. No logs were deleted.");
            return;
        }

        if (activityLogDao.clearAllLogs()) {
            System.out.println("-> All historical activity logs have been cleared.");
        } else {
            System.out.println("-> Failed to clear activity logs.");
        }
    }

    // ===================== USER TABLE =====================

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

    // Read integer input
    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
