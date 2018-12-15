package org.messtin.lock.server.entity;

import io.netty.channel.Channel;

/**
 * Keep the connection information with user.
 *
 * @author majinliang
 */
public class Session {

    private String sessionId;
    private Channel channel;

    public Session(String sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
