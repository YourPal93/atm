package org.yourpal.petproject;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourpal.petproject.entity.Client;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class AtmInterfaceTest {
    private SessionFactory sessionFactory;

    @BeforeEach
    public void setup() {
        sessionFactory = configureHibernate();
        var session = sessionFactory.openSession();

        session.beginTransaction();
        var testClient = new Client("0000", "1234", 10000.0);
        var testClient1 = new Client("0001", "1234", 10000.0);
        var testClient2 = new Client("0002", "1234", 10000.0);

        session.persist(testClient);
        session.persist(testClient1);
        session.persist(testClient2);

        session.getTransaction().commit();
        session.close();
    }



    @Test
    void clientAuthorization() {
        var newClient = new Client("123", "123",  10000.0);

        try(var session = sessionFactory.openSession()) {
            session.beginTransaction();

            session.persist(newClient);

            session.getTransaction().commit();

            var query =
                    session.createQuery("select c from clients c where cardNumber= :cardNumber", Client.class);
            query.setParameter("cardNumber", "123");
            var oldClient = query.list();

            session.close();
            assertEquals(newClient, oldClient.get(0));
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception in client auth test");
        }
    }

    @Test
    void updateBalance() {

        try(var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var query =
                    session.createQuery("select c from clients c where c.cardNumber = :cardNumber", Client.class);
            query.setParameter("cardNumber", "0001");
            var query1 =
                    session.createQuery("select c.balance from clients c where c.cardNumber = :cardNumber", Double.class);
            query1.setParameter("cardNumber", "0001");

            var client = query.list().get(0);

            client.setBalance(5000.0);
            session.merge(client);
            session.getTransaction().commit();

            var clientBalance = query1.list().get(0);

            assertEquals(5000.0, clientBalance);

        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception ins withdraw test");
        }
    }


    @Test
    public void createClient() {
        var session = sessionFactory.openSession();
        var newCard = "9999";
        var newPin = "1234";
        session.beginTransaction();

        var newClient = new Client(newCard, newPin, 10000.0);
        session.persist(newClient);
        session.getTransaction().commit();
        session.close();

        session = sessionFactory.openSession();
        var query =
                session.createQuery("select count(*) from clients c where c.cardNumber = :cardNumber", Long.class);
        query.setParameter("cardNumber", newCard);

        assertEquals(1, query.uniqueResult());
        session.close();
    }

    @Test
    public void findClient() {
        var testClient = new Client("0000", "1234", 10000.0);
        testClient.setId(1);
        var session = sessionFactory.openSession();

        var existingCard = "0000";
        var existingCardPin = "1234";
        var query =
                session.createQuery("select c from clients c where c.cardNumber = :cardNumber and c.pin = :pin",
                        Client.class);
        query.setParameter("cardNumber", existingCard);
        query.setParameter("pin", existingCardPin);
        var existingClient = query.uniqueResult();

        assertEquals(testClient, existingClient);
        session.close();
    }

    @Test
    public void changePin() {

    }

    private SessionFactory configureHibernate() {
        var srBuilder = new StandardServiceRegistryBuilder();

        var props = new HashMap<String, Object>();
        props.put(Environment.DRIVER, "org.h2.Driver");
        props.put(Environment.URL, "jdbc:h2:file:./data/test/clientDb_test");
        props.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
        props.put(Environment.SHOW_SQL, "true");
//        props.put(Environment.FORMAT_SQL, "true");
//        props.put(Environment.USE_SQL_COMMENTS, "true");
        props.put(Environment.HBM2DDL_AUTO, "create-drop");
        srBuilder.applySettings(props);

        var sr = srBuilder.build();
        var metadataSources = new MetadataSources(sr);
        metadataSources.addAnnotatedClass(Client.class);

        var metadata = metadataSources.getMetadataBuilder().build();

        return metadata.getSessionFactoryBuilder().build();

    }
}