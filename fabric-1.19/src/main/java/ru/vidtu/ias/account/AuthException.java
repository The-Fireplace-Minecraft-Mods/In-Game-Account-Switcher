package ru.vidtu.ias.account;

import net.minecraft.network.chat.Component;

public class AuthException extends Exception {
    private static final long serialVersionUID = 1L;
    private Component component;

    public AuthException(Component component) {
        super(component.getString());
        this.component = component;
    }

    public AuthException(Component component, String detailed) {
        super(component.getString() + ":" + detailed);
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
