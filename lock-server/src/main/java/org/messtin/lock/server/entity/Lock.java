package org.messtin.lock.server.entity;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keep the lock information.
 *
 * @author majinliang
 */
public class Lock {

    private AtomicLong lockCounts;
    private Operator operator;
    private BlockingQueue<Operator> awaitOps;
    private String resource;

    public Lock(String resource, Operator operator) {
        lockCounts = new AtomicLong(1);
        awaitOps = new LinkedBlockingQueue<>();
        this.resource = resource;
        this.operator = operator;
    }

    public AtomicLong getLockCounts() {
        return lockCounts;
    }

    public void setLockCounts(AtomicLong lockCounts) {
        this.lockCounts = lockCounts;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public BlockingQueue<Operator> getAwaitOps() {
        return awaitOps;
    }

    public void setAwaitOps(BlockingQueue<Operator> awaitOps) {
        this.awaitOps = awaitOps;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
