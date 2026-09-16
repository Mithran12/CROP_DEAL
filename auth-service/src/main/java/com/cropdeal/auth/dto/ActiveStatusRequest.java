package com.cropdeal.auth.dto;

import jakarta.validation.constraints.NotNull;

public class ActiveStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}