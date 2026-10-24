package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

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
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            switch (readInt()) {
                case 1 -> printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                case 2 -> printUserTable(userDao.getPendingUsers(), "PENDING REGISTRATIONS");
                case 3 -> approveUser();
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