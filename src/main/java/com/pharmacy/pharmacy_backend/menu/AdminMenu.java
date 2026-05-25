package com.pharmacy.pharmacy_backend.menu;

import java.util.Scanner;

public class AdminMenu {

    public static void show() {
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Manage Medicines");
            System.out.println("2. Manage Users");
            System.out.println("3. View Reports");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Manage Medicines → next step");
                    break;
                case 2:
                    System.out.println("Manage Users → next step");
                    break;
                case 3:
                    System.out.println("Reports → next step");
                    break;
                case 4:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
