package org.messtin.lock.server.entity;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Keep the lock information.
 *
 * @author majinliang
 */
public class Lock {

    private Operator operator;
    private BlockingQueue<Operator> awaitOps;
    private String resource;

    public Lock(String resource, Operator operator) {
        awaitOps = new LinkedBlockingQueue<>();
        this.resource = resource;
        this.operator = operator;
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
