package org.messtin.lock.common.entity;

import java.util.Objects;

public class LockResponse {

    private ResponseCode responseCode;
    private String resource;
    private String sessionId;
    private Step step;

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LockResponse that = (LockResponse) o;
        return responseCode == that.responseCode &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(sessionId, that.sessionId) &&
                step == that.step;
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseCode, resource, sessionId, step);
    }

    @Override
    public String toString() {
        return "LockResponse{" +
                "responseCode=" + responseCode +
                ", resource='" + resource + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", step=" + step +
                '}';
    }
}
