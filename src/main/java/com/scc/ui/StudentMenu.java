package com.scc.ui;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class StudentMenu {

    private final UserDao userDao;
    private final Scanner sc;
    private final String username;

    public StudentMenu(UserDao userDao, Scanner sc, String username) {
        this.userDao = userDao;
        this.sc = sc;
        this.username = username;
    }

    public void show() {
        while (true) {
            System.out.println("\n===== STUDENT MENU (" + username + ") =====");
            System.out.println("1. (add features here)");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();
            if (choice.equals("0")) {
                System.out.println("Logged out.");
                return;
            }
            System.out.println("Not implemented yet.");
        }
    }
}