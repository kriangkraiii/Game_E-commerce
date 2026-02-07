package com.ecom.model;

public enum NotificationType {
    LIKE("liked your pet's post"),
    COMMENT("commented on your pet's post");

    private final String defaultMessage;

    NotificationType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}