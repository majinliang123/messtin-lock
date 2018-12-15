package org.messtin.lock.client;

import org.messtin.lock.client.connector.Connector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class LockClient {
    private static final int DEFAULT_PORT = 4043;
    private String address;
    private int port;
    private Map<String, CountDownLatch> lockMap = new ConcurrentHashMap<>();

    private static volatile LockClient instance;
    private static Connector connector;

    private LockClient(String address, int port) {
        this.address = address;
        this.port = port;
        connector = new Connector(address, port, lockMap);
        connector.init();
    }

    public static LockClient newInstance(String address) {
        if (instance == null) {
            synchronized (LockClient.class) {
                if (instance == null) {
                    instance = new LockClient(address, DEFAULT_PORT);
                }
            }
        }
        return instance;
    }

    public void lock(String resource) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        lockMap.put(resource, latch);
        connector.sendLockRequest(resource);
        latch.await();
    }

    public void release(String resource) {
        connector.sendReleaseRequest(resource);
    }
}
