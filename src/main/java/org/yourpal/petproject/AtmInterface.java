package org.yourpal.petproject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.yourpal.petproject.entity.Client;
import org.yourpal.petproject.enums.AtmMods;
import org.yourpal.petproject.enums.Menus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class AtmInterface {

    private final SessionFactory sessionFactory;
    private final Scanner scanner;
    private Client client;
    private AtmMods atmMod;
    private Session session;
    private DecimalFormat decFormat;
    
    public AtmInterface() {
        decFormat = new DecimalFormat("0.00");
        decFormat.setRoundingMode(RoundingMode.UP);
        decFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        scanner = new Scanner(System.in).useLocale(Locale.ENGLISH);

        sessionFactory = configureHibernate();
    }

    private void newSession() {
        if(session != null) {
            session.close();
        }
        session = sessionFactory.openSession();
    }

    // выбор режима банкомата(будет ли использоваться алгоритм Луна)
    public void chooseAtmMod() {
        var exit = false;
        while(!exit) {
            System.out.println("\nВыберите режим работы банкомата: \n" + Menus.ATM_MOD.text);
            var command = scanner.next();
            switch(command) {
                case "1" -> {
                    atmMod = AtmMods.TEST;
                    exit = true;
                    System.out.println("Установлен тестовый режим");
                }
                case "2" -> {
                    atmMod = AtmMods.NORMAL;
                    exit = true;
                    System.out.println("Установлен обычный режим");
                }
                default -> System.out.println("Низвестная команда: " + command);
            }
        }
    }

    // авторизация клиента
    public void clientAuthorization() {
        try {
            newSession();
            var exit = false;
            while(!exit) {
                System.out.println("\nВыберите способ работы с клиентом\n" + Menus.ATM_CLIENTS.text);
                var clientMod = scanner.next();
                switch(clientMod) {
                    // создать нового клиента
                    case "1" -> {
                        System.out.println("Введите номер кредитной карты клиента\n");
                        if (!createClient()) {
                            System.out.println("Неверный номер карты или клиент уже существует");
                            break;
                        }
                        exit = true;
                    }
                    // найти существующего
                    case "2" -> {
                        System.out.println("Введите номер кредитной карты клиента\n");
                        if (!findClient()) {
                            System.out.println("Неверный номер карты или пин-код");
                            break;
                        }
                        exit = true;
                    }
                    default -> System.out.println("\nНеизвестная команда: " + clientMod);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Something went wrong in clientAuthorization");
        }
    }

    // снять средства
    public void withdraw() {
        try {
            while (true) {
                System.out.println("\nВведите сумму или ноль для выхода: ");
                var amount = scanner.nextDouble();
                if(amount == 0) return;
                // при снятии кол-во будет отформатировано в значение с двумя знаками после целого числа
                var amountFormated = Double.valueOf(decFormat.format(amount));
                if (amount > client.getBalance() || amount < 0) {
                    System.out.println("Запрашиваемая сумма не может превышать баланс и должна быть положительным значеныем");
                    continue;
                }
                var balance = client.getBalance();
                client.setBalance(balance - amountFormated);
                break;
            }

            session.beginTransaction();
            session.merge(client);
            session.getTransaction().commit();
            System.out.println("Заберите наличные. Хорошего дня!\n");

        } catch(InputMismatchException ex) {
            System.out.println("Значение должно быть числом, ваше значение: " + scanner.next());
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Something went wrong in withdraw");
        }
    }

    // пополнение баланса
    public void deposit() {
        try {
            while(true) {
                System.out.println("\nВведите сумму или ноль для выхода: ");
                var amount = scanner.nextDouble();
                if(amount == 0) return;
                // при пополнение баланса кол-во будет отформатировано в значение с двумя знаками после целого числа
                var amountFormated = Double.valueOf(decFormat.format(amount));
                if(amount < 0) {
                    System.out.println("Значение для пополнения баланса не может быть отрицательным");
                    continue;
                }
                var balance = client.getBalance();
                client.setBalance(balance + amountFormated);
                break;
            }

            session.beginTransaction();
            session.merge(client);
            session.getTransaction().commit();
            System.out.println("Счёт был успешно пополнен\n");

        } catch(InputMismatchException ex) {
            System.out.println("Значение должно быть числом, ваше значение: " + scanner.next());
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Something went wrong in withdraw");
        }
    }

    // смена пин-кода
    public void changePin() {
        try {
            while(true) {
                System.out.println("\nВведите старый пин-код: ");
                var pin = scanner.next();
                if(pin.equals("0")) return;
                if(!pin.equals(client.getPin())) {
                    System.out.println("неверный пин-код, попробуйте снова или введите 0 чтобы выйти");
                    continue;
                }

                System.out.println("Введите новый пин код, пин-код должен содержать 4 цифры");
                var newPin = scanner.next();

                if(newPin.length() != 4) {
                    System.out.println("Пин-код должен содержать 4 цифры, попробуйте снова или введите 0 чтобы выйти");
                    continue;
                }
                client.setPin(newPin);
                session.beginTransaction();
                session.merge(client);
                session.getTransaction().commit();
                System.out.println("\nПин-код изменён");
                break;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Something went wrong in changePin");
        }
    }
    public void balance() {
        System.out.println(decFormat.format(client.getBalance()) + "\n");;
    }

    public void exit() {
        session.close();
        sessionFactory.close();
    }


    // создание нового клиента
    public boolean createClient() {
        var newCard = scanner.next();
        // если установлен обычный режим, то проводится проверка по алгоритму Луна
        if(Validator.validateCard(newCard, atmMod)) return false;
        System.out.println("Введите пин-код");
        var newPin = scanner.next();

        var query =
                session.createQuery("select count(*) from clients c where c.cardNumber = :cardNumber", Long.class);
        query.setParameter("cardNumber", newCard);
        if(query.uniqueResult() > 0) return false;

        var newClient = new Client(newCard, newPin, 10000.0);
        session.beginTransaction();
        session.persist(newClient);
        session.getTransaction().commit();
        client = newClient;
        System.out.println("\nНовый клиент был успешно добавлен, установленный баланс 10000Р");

        return true;
    }

    //поиск клиента в базе данных
    public boolean findClient() {
        var existingCard = scanner.next();
        System.out.println("Введите пин-код");
        var existingCardPin = scanner.next();
        var query =
                session.createQuery("select c from clients c where c.cardNumber = :cardNumber and c.pin = :pin",
                        Client.class);
        query.setParameter("cardNumber", existingCard);
        query.setParameter("pin", existingCardPin);
        var existingClient = query.uniqueResult();

        if(existingClient == null) {
            return false;
        }

        client = existingClient;
        return true;
    }

    // конфигурация SessionFactory в Hibernate
    private SessionFactory configureHibernate() {
        var srBuilder = new StandardServiceRegistryBuilder();

        var props = new HashMap<String, Object>();
        props.put(Environment.DRIVER, "org.h2.Driver");
        props.put(Environment.URL, "jdbc:h2:file:./data/clientDB");
        props.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
//        props.put(Environment.SHOW_SQL, "true");
        props.put(Environment.HBM2DDL_AUTO, "update");
        srBuilder.applySettings(props);

        var sr = srBuilder.build();
        var metadataSources = new MetadataSources(sr);
        metadataSources.addAnnotatedClass(Client.class);

        var metadata = metadataSources.getMetadataBuilder().build();

        return metadata.getSessionFactoryBuilder().build();

    }

}
