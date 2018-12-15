package org.messtin.lock.server.entity;

import io.netty.channel.Channel;
import org.messtin.lock.common.entity.Step;

import java.util.Objects;

/**
 * Keep the user operator.
 *
 * @author majinliang
 */
public class Operator {

    private Step step;
    private Channel channel;
    private String sessionId;

    public Operator(Step step, Channel channel, String sessionId) {
        this.step = step;
        this.channel = channel;
        this.sessionId = sessionId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operator operator = (Operator) o;
        return step == operator.step &&
                Objects.equals(channel, operator.channel) &&
                Objects.equals(sessionId, operator.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(step, channel, sessionId);
    }
}
