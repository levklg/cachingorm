package com.example.cachingorm.crm.service;


import com.example.cachingorm.cachehw.HwCache;
import com.example.cachingorm.cachehw.MyCache;
import com.example.cachingorm.core.repository.DataTemplate;
import com.example.cachingorm.core.sessionmanager.TransactionManager;
import com.example.cachingorm.crm.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DbServiceClientImpl implements DBServiceClient {
    private static final Logger log = LoggerFactory.getLogger(DbServiceClientImpl.class);

    private final DataTemplate<Client> clientDataTemplate;
    private final TransactionManager transactionManager;
    private final HwCache<Long, Client> cache = new MyCache<>();

    public DbServiceClientImpl(TransactionManager transactionManager, DataTemplate<Client> clientDataTemplate) {
        this.transactionManager = transactionManager;
        this.clientDataTemplate = clientDataTemplate;
    }

@Override
public Client saveClient(Client client) {
    return transactionManager.doInTransaction(session -> {
        var clientCloned = client.clone();

        Client savedClient;
        if (client.getId() == null) {
            savedClient = clientDataTemplate.insert(session, clientCloned);
            log.info("created client: {}", savedClient);
        } else {
            Client existingClient = session.get(Client.class, client.getId());
            if (existingClient != null) {
                clientCloned = (Client) session.merge(clientCloned);
            }
            savedClient = clientDataTemplate.update(session, clientCloned);
            log.info("updated client: {}", savedClient);
        }
        cache.put(savedClient.getId(), savedClient);
        return savedClient;
    });
}
    @Override
    public Optional<Client> getClient(long id) {
        return transactionManager.doInReadOnlyTransaction(session -> {
            var clientOptional = Optional.ofNullable(cache.get(id));
            if (clientOptional.isEmpty()) {
                clientOptional = clientDataTemplate.findById(session, id);
                clientOptional.ifPresent(client -> cache.put(id, client));
            }
            log.info("client: {}", clientOptional);
            return clientOptional;
        });
    }

    @Override
    public List<Client> findAll() {
        return transactionManager.doInReadOnlyTransaction(session -> {
            var clientList = clientDataTemplate.findAll(session);
            log.info("clientList:{}", clientList);
            return clientList;
        });
    }
}