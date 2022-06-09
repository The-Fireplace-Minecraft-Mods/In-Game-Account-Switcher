package ru.vidtu.ias.account;

import net.minecraft.util.text.ITextComponent;

public class AuthException extends Exception {
    private static final long serialVersionUID = 1L;
    private ITextComponent component;

    public AuthException(ITextComponent component) {
        super(component.getString());
        this.component = component;
    }

    public AuthException(ITextComponent component, String detailed) {
        super(component.getString() + ":" + detailed);
        this.component = component;
    }

    public ITextComponent getComponent() {
        return component;
    }
}
