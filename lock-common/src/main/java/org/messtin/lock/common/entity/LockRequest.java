package org.messtin.lock.common.entity;

import java.util.Objects;

public class LockRequest {

    private String sessionId;
    private Step step;
    private String resource;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LockRequest that = (LockRequest) o;
        return Objects.equals(sessionId, that.sessionId) &&
                step == that.step &&
                Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, step, resource);
    }

    @Override
    public String toString() {
        return "LockRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", step=" + step +
                ", resource='" + resource + '\'' +
                '}';
    }
}
