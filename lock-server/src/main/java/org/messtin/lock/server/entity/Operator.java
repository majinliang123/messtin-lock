package org.messtin.lock.server.entity;

import io.netty.channel.Channel;
import org.messtin.lock.common.entity.Step;

/**
 * Keep the user operator.
 *
 * @author majinliang
 */
public class Operator {

    private Step step;
    private Channel channel;
    private String sessionId;
    private String resource;

    public Operator(Step step, Channel channel, String sessionId, String resource) {
        this.step = step;
        this.channel = channel;
        this.sessionId = sessionId;
        this.resource = resource;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
