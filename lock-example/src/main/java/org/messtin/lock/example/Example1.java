package org.messtin.lock.example;

import org.messtin.lock.client.LockClient;

public class Example1 {

    public static void main(String[] args) throws InterruptedException {
        LockClient client = LockClient.newInstance("localhost");
        client.lock("1");
        System.out.println("2");
//        Thread.sleep(1000*30);
        client.release("1");
    }
}
