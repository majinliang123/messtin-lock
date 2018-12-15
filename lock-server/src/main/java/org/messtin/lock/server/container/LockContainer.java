package org.messtin.lock.server.container;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.server.entity.Lock;
import org.messtin.lock.server.entity.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The container of lock.
 * When user try to get lock, will acquire lock here.
 *
 * @author majinliang
 */
public final class LockContainer {
    private static final Logger logger = LogManager.getLogger(LockContainer.class);

    private static Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    /**
     * When user try to lock a resource,
     * 1.the resource is never locked. Will create a new lock.
     * 3.the resource is locked. Will put the operator into queue to wait for the release of the resource.
     *
     * @param resource the resource you want to lock.
     * @param operator
     * @return if get the lock of the resource.
     */
    public static synchronized boolean acquire(String resource, Operator operator) {
        logger.info("Try to get lock for resource={} sessionId={}.", resource, operator.getSessionId());
        Lock currentLock = lockMap.get(resource);

        if (currentLock == null) {
            currentLock = new Lock(resource, operator);
            lockMap.put(resource, currentLock);
            logger.info("Create new lock and get lock for resource={} sessionId={}.", resource, operator.getSessionId());
            return true;
        }

        logger.info("Did not get the lock for resource={}, will add into queue.", resource);
        currentLock.getAwaitOps().add(operator);
        return false;
    }

    /**
     * Try to release the lock and get the head of operator queue.
     * We will check if lock existed or if the current lock owned by the sessionId.
     *
     * @param resource  the resource we need to release.
     * @param sessionId the session id who want to release the session.
     * @return the new current operator.
     */
    public static Operator release(String resource, String sessionId) {
        logger.info("Try to release lock for resource={} sessionId={}.", resource, sessionId);

        Lock currentLock = lockMap.get(resource);
        if (currentLock == null) {
            return null;
        }
        if (!currentLock.getOperator().getSessionId().equals(sessionId)) {
            return null;
        }

        Operator headOp = currentLock.getAwaitOps().poll();
        if (headOp == null) {
            lockMap.remove(resource);
            return null;
        } else {
            currentLock.setOperator(headOp);
            return headOp;
        }
    }

    /**
     * Will release all the {@link Operator} owned by the session id.
     *
     * @param sessionId
     * @return the current operators we release, we will trigger them to get the resource.
     */
    public static List<Operator> release(String sessionId) {
        List<Operator> operators = new ArrayList<>();
        for (Map.Entry<String, Lock> lockEntry : lockMap.entrySet()) {
            BlockingQueue<Operator> opQueue = lockEntry.getValue().getAwaitOps();
            for (Operator op : opQueue) {
                if (op.getSessionId().equals(sessionId)) {
                    opQueue.remove(op);
                }
            }
            Operator currentOp = lockEntry.getValue().getOperator();
            if (currentOp.getSessionId().equals(sessionId)) {
                operators.add(release(lockEntry.getValue().getResource(), sessionId));
            }
        }
        return operators;
    }
}
