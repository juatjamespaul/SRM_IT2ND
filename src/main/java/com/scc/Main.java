package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize database
        DatabaseInitializer.initialize();

        // 2. Instantiate shared DAOs & utilities (one instance each)
        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // 3. Delegate everything else
        new Authentication(userDao, sc).start();

        sc.close();
    }
}