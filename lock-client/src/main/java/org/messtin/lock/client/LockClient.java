package org.messtin.lock.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.client.connector.Connector;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Lock client.
 *
 * @author majinliang
 */
public class LockClient {
    private static final Logger logger = LogManager.getLogger(LockClient.class);

    private static final int DEFAULT_PORT = 4043;
    private String address;
    private int port;

    /**
     * source -> queue of countDownLatch.
     * When user wants to lock something, the countDownLatch information will be put into {@link #lockMap}
     * We use queue, because may be there are many threads try to lock same resource.
     */
    private Map<String, Queue<CountDownLatch>> lockMap = new ConcurrentHashMap<>();

    private static volatile LockClient instance;
    private static Connector connector;

    private LockClient(String address, int port) throws InterruptedException {
        this.address = address;
        this.port = port;
        connector = new Connector(address, port, lockMap);
        connector.init();
    }

    /**
     * Use singleton pattern to create an instance of {@link LockClient}
     *
     * @param address
     * @return
     */
    public static LockClient newInstance(String address) throws InterruptedException {
        logger.info("Start initialise lock client with address={}.", address);
        if (instance == null) {
            synchronized (LockClient.class) {
                if (instance == null) {
                    instance = new LockClient(address, DEFAULT_PORT);
                }
            }
        }
        logger.info("Complete initialise lock client.");
        return instance;
    }

    /**
     * User uses this method to lock the resource they want to lock.
     *
     * @param resource the resource user want to lock.
     * @throws InterruptedException
     */
    public void lock(String resource) throws InterruptedException {
        logger.info("Try to lock resource={}.", resource);

        CountDownLatch latch = new CountDownLatch(1);
        synchronized (lockMap) {
            Queue<CountDownLatch> latches = lockMap.get(resource);
            if (latches == null) {
                latches = new ConcurrentLinkedQueue<>();
            }
            latches.add(latch);
            lockMap.put(resource, latches);
        }

        connector.sendLockRequest(resource);
        latch.await();
        logger.info("Get lock resource={}.", resource);
    }

    /**
     * User uses this method to release the resource.
     *
     * @param resource
     */
    public void release(String resource) {
        logger.info("Try to release resource={}.", resource);
        connector.sendReleaseRequest(resource);
        logger.info("Realsed resource={}.", resource);
    }
}
