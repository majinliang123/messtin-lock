package org.messtin.lock.example;

import org.messtin.lock.client.LockClient;

public class Example1 {

    public static void main(String[] args) throws InterruptedException {
        LockClient client = LockClient.newInstance("localhost");
        client.lock("1");
        System.out.println("1");
        client.release("1");
    }
}
