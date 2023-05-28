package com.example.cachingorm;


import com.example.cachingorm.core.repository.DataTemplateHibernate;
import com.example.cachingorm.core.repository.HibernateUtils;
import com.example.cachingorm.core.sessionmanager.TransactionManagerHibernate;
import com.example.cachingorm.crm.dbmigrations.MigrationsExecutorFlyway;
import com.example.cachingorm.crm.model.Address;
import com.example.cachingorm.crm.model.Client;
import com.example.cachingorm.crm.model.Phone;
import com.example.cachingorm.crm.service.DbServiceClientImpl;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeWork {

    private static final Logger log = LoggerFactory.getLogger(HomeWork.class);
    public static final String HIBERNATE_CFG_FILE = "hibernate.cfg.xml";
    public static final int NUM_CLIENTS = 100;
    public static final int NUM_OBJECTS = 10000;
    public static final int SIZE_OBJECTS = 50000;

    public static void main(String[] args) {
        var configuration = new Configuration().configure(HIBERNATE_CFG_FILE);
        var dbUrl = configuration.getProperty("hibernate.connection.url");
        var dbUserName = configuration.getProperty("hibernate.connection.username");
        var dbPassword = configuration.getProperty("hibernate.connection.password");
        new MigrationsExecutorFlyway(dbUrl, dbUserName, dbPassword).executeMigrations();
        var sessionFactory = HibernateUtils.buildSessionFactory(configuration, Client.class, Address.class, Phone.class);
        var transactionManager = new TransactionManagerHibernate(sessionFactory);
        var clientTemplate = new DataTemplateHibernate<>(Client.class);
        var dbServiceClient = new DbServiceClientImpl(transactionManager, clientTemplate);

        // Вставка клиентов в базу данных.
        for (int i = 0; i < NUM_CLIENTS; i++) {
            Address address = new Address(null, "Street " + i);
            List<Phone> phones = Arrays.asList(new Phone(null, "123456" + i, null));
            Client client = new Client(null, "Client " + i, address, phones);
            dbServiceClient.saveClient(client);
        }

        // Загрузка клиентов в кэш.
        for (long i = 1; i <= NUM_CLIENTS; i++) {
            var clientOptional = dbServiceClient.getClient(i);
            clientOptional.ifPresent(client -> log.info("Loaded client: {}", client));
        }

        // Проверяем производительность кэша.
        long id = 1L;
        long startTime = System.nanoTime();
        var clientFromDb = dbServiceClient.getClient(id);
        long cacheTime = System.nanoTime() - startTime;
        System.out.println("Time to get object from cache: " + cacheTime);

        //  Заполняем память.
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < NUM_OBJECTS; i++) {
            try {
                list.add(new byte[SIZE_OBJECTS]);
            } catch (OutOfMemoryError e) {
                log.info("Memory is full: created {} objects", i);
                list.clear();
                System.gc();
                break;
            }
        }

        // Проверяем, что клиенты все еще находятся в кэше или загружены из БД.
        for (long i = 1; i <= NUM_CLIENTS; i++) {
            var clientOptional = dbServiceClient.getClient(i);
            clientOptional.ifPresent(client -> log.info("Loaded client: {}", client));
        }

    }

}

