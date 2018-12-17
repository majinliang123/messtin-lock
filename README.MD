# Messtin Lock

### Example
```java
public class Example1 {

    public static void main(String[] args) throws InterruptedException {
        LockClient client = LockClient.newInstance("localhost");
        try {
            client.lock("1");
            System.out.println("1");
        } finally {
            client.release("1");        
        }
    }
}
```

### Tech
- use netty keep alive connetion.
- once client off line, will release lock


### Noted
There are one problem, when the main thead completed, the jvm will not close. It is because netty event loop's thread pool is not closed and all thread in thread pool is not deamon thread and could not be set as deamon thread at netty version 4.1.31.Final.

You could call System.exit(0) or kill the jvm manually currently.

At netty version 5.x, all thread at event loop thread pool will be deamon thread, so let's wait for the new version of netty.
