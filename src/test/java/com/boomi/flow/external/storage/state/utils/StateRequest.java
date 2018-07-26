package com.boomi.flow.external.storage.state.utils;

public class StateRequest {
        private String token;

        public StateRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
