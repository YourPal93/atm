package org.yourpal.petproject;

import org.yourpal.petproject.enums.Menus;

import java.util.Scanner;

public class ATM {

    public static void main(String[] args) {
        var atmInterface = new AtmInterface();
        var scanner = new Scanner(System.in);
        System.out.println("Добро пожаловать в банкомат");
        atmInterface.chooseAtmMod();
        atmInterface.clientAuthorization();
        var exit = false;
        var menu = Menus.ATM_MENU.text;
        while(!exit) {
            System.out.println("\nВыберите команду:\n" + menu);
            var command = scanner.next();
            switch(command) {
                case "1":
                    atmInterface.balance();
                    break;
                case "2":
                    atmInterface.withdraw();
                    break;
                case "3":
                    atmInterface.deposit();
                    break;
                case "4":
                    atmInterface.chooseAtmMod();
                    System.out.println("\nМод был успешно изменён\n");
                    break;
                case "5":
                    atmInterface.changePin();
                    break;
                case "6":
                    atmInterface.clientAuthorization();
                    break;
                case "7":
                    atmInterface.exit();
                    System.out.println("\nХорошего дня!");
                    exit = true;
                    break;
                default:
                    System.out.println("неизвестная команда: " + command);
            }
        }
    }


}
