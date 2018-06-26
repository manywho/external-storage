package com.boomi.flow.external.storage.states;

public class StateResponse {
    private String token;

    public StateResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
