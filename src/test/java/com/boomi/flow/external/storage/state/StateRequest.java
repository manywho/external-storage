package com.boomi.flow.external.storage.state;

public class StateRequest {
        private String token;

        public StateRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
