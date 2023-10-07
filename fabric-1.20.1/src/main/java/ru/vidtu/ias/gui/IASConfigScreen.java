package ru.vidtu.ias.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.vidtu.ias.config.Alignment;
import ru.vidtu.ias.config.IASConfig;

import java.util.Objects;

/**
 * Screen for editing IAS config.
 *
 * @author VidTu
 */
public class IASConfigScreen extends Screen {
    private final Screen prev;
    private Checkbox titleScreenText;
    private EditBox titleScreenTextX;
    private EditBox titleScreenTextY;
    private Button titleScreenTextAlignment;
    private Checkbox titleScreenButton;
    private EditBox titleScreenButtonX;
    private EditBox titleScreenButtonY;
    private Checkbox multiplayerScreenButton;
    private EditBox multiplayerScreenButtonX;
    private EditBox multiplayerScreenButtonY;

    public IASConfigScreen(Screen prev) {
        super(Component.translatable("ias.configGui.title"));
        this.prev = prev;
    }

    @Override
    public void init() {
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(prev)).bounds(width / 2 - 75, height - 28, 150, 20).build());
        addRenderableWidget(titleScreenText = new Checkbox(5, 20, 24 + font.width(Component.translatable(
                "ias.configGui.titleScreenText")), 20, Component.translatable(
                        "ias.configGui.titleScreenText"), IASConfig.titleScreenText));
        addRenderableWidget(titleScreenTextX = new EditBox(font, 35 + font.width(Component.translatable(
                "ias.configGui.titleScreenText")), 20, 50, 20, Component.literal("X")));
        addRenderableWidget(titleScreenTextY = new EditBox(font, 35 + font.width(Component.translatable(
                "ias.configGui.titleScreenText")) + 54, 20, 50, 20, Component.literal("Y")));
        addRenderableWidget(titleScreenTextAlignment = Button.builder(
                Component.translatable("ias.configGui.titleScreenText.alignment", I18n.get(IASConfig.titleScreenTextAlignment.key())),
                btn -> changeAlignment()).bounds(
                35 + font.width(Component.translatable(
                        "ias.configGui.titleScreenText")) + 108, 20, font.width(Component.translatable(
                        "ias.configGui.titleScreenText.alignment", I18n.get(IASConfig.titleScreenTextAlignment.key()))) + 20, 20
        ).build());
        addRenderableWidget(titleScreenButton = new Checkbox(5, 44, 24 + font.width(Component.translatable(
                "ias.configGui.titleScreenButton")), 20, Component.translatable(
                "ias.configGui.titleScreenButton"), IASConfig.titleScreenButton));
        addRenderableWidget(titleScreenButtonX = new EditBox(font, 35 + font.width(Component.translatable(
                "ias.configGui.titleScreenButton")), 44, 50, 20, Component.literal("X")));
        addRenderableWidget(titleScreenButtonY = new EditBox(font, 35 + font.width(Component.translatable(
                "ias.configGui.titleScreenButton")) + 54, 44, 50, 20, Component.literal("Y")));
        addRenderableWidget(multiplayerScreenButton = new Checkbox(5, 68, 24 + font.width(Component.translatable(
                "ias.configGui.multiplayerScreenButton")), 20, Component.translatable(
                "ias.configGui.multiplayerScreenButton"), IASConfig.multiplayerScreenButton));
        addRenderableWidget(multiplayerScreenButtonX = new EditBox(font, 35 + font.width(Component.translatable(
                "ias.configGui.multiplayerScreenButton")), 68, 50, 20, Component.literal("X")));
        addRenderableWidget(multiplayerScreenButtonY = new EditBox(font, 35 + font.width(Component.translatable(
                "ias.configGui.multiplayerScreenButton")) + 54, 68, 50, 20, Component.literal("Y")));
        titleScreenTextX.setSuggestion(titleScreenTextX.getValue().isEmpty() ? "X" : "");
        titleScreenTextY.setSuggestion(titleScreenTextY.getValue().isEmpty() ? "Y" : "");
        titleScreenButtonX.setSuggestion(titleScreenButtonX.getValue().isEmpty() ? "X" : "");
        titleScreenButtonY.setSuggestion(titleScreenButtonY.getValue().isEmpty() ? "Y" : "");
        multiplayerScreenButtonX.setSuggestion(multiplayerScreenButtonX.getValue().isEmpty() ? "X" : "");
        multiplayerScreenButtonY.setSuggestion(multiplayerScreenButtonY.getValue().isEmpty() ? "Y" : "");
        titleScreenTextX.setResponder(s -> titleScreenTextX.setSuggestion(s.isEmpty() ? "X" : ""));
        titleScreenTextY.setResponder(s -> titleScreenTextY.setSuggestion(s.isEmpty() ? "Y" : ""));
        titleScreenButtonX.setResponder(s -> titleScreenButtonX.setSuggestion(s.isEmpty() ? "X" : ""));
        titleScreenButtonY.setResponder(s -> titleScreenButtonY.setSuggestion(s.isEmpty() ? "Y" : ""));
        multiplayerScreenButtonX.setResponder(s -> multiplayerScreenButtonX.setSuggestion(s.isEmpty() ? "X" : ""));
        multiplayerScreenButtonY.setResponder(s -> multiplayerScreenButtonY.setSuggestion(s.isEmpty() ? "Y" : ""));
        titleScreenTextX.setValue(Objects.toString(IASConfig.titleScreenTextX, ""));
        titleScreenTextY.setValue(Objects.toString(IASConfig.titleScreenTextY, ""));
        titleScreenButtonX.setValue(Objects.toString(IASConfig.titleScreenButtonX, ""));
        titleScreenButtonY.setValue(Objects.toString(IASConfig.titleScreenButtonY, ""));
        multiplayerScreenButtonX.setValue(Objects.toString(IASConfig.multiplayerScreenButtonX, ""));
        multiplayerScreenButtonY.setValue(Objects.toString(IASConfig.multiplayerScreenButtonY, ""));
        tick();
    }

    private void changeAlignment() {
        int i = IASConfig.titleScreenTextAlignment.ordinal() + 1;
        if (i >= Alignment.values().length) i = 0;
        IASConfig.titleScreenTextAlignment = Alignment.values()[i];
        titleScreenTextAlignment.setMessage(Component.translatable("ias.configGui.titleScreenText.alignment",
                I18n.get(IASConfig.titleScreenTextAlignment.key())));
        titleScreenTextAlignment.setWidth(font.width(titleScreenTextAlignment.getMessage()) + 20);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(prev);
    }

    @Override
    public void removed() {
        IASConfig.titleScreenText = titleScreenText.selected();
        IASConfig.titleScreenTextX = titleScreenTextX.getValue().trim().isEmpty() ? null : titleScreenTextX.getValue();
        IASConfig.titleScreenTextY = titleScreenTextY.getValue().trim().isEmpty() ? null : titleScreenTextY.getValue();
        IASConfig.titleScreenButton = titleScreenButton.selected();
        IASConfig.titleScreenButtonX = titleScreenButtonX.getValue().trim().isEmpty() ? null : titleScreenButtonX.getValue();
        IASConfig.titleScreenButtonY = titleScreenButtonY.getValue().trim().isEmpty() ? null : titleScreenButtonY.getValue();
        IASConfig.multiplayerScreenButton = multiplayerScreenButton.selected();
        IASConfig.multiplayerScreenButtonX = multiplayerScreenButtonX.getValue().trim().isEmpty() ? null : multiplayerScreenButtonX.getValue();
        IASConfig.multiplayerScreenButtonY = multiplayerScreenButtonY.getValue().trim().isEmpty() ? null : multiplayerScreenButtonY.getValue();
        IASConfig.save(minecraft.gameDirectory.toPath());
    }

    @Override
    public void tick() {
        titleScreenTextX.visible = titleScreenTextY.visible = titleScreenTextAlignment.visible = titleScreenText.selected();
        titleScreenButtonX.visible = titleScreenButtonY.visible = titleScreenButton.selected();
        multiplayerScreenButtonX.visible = multiplayerScreenButtonY.visible = multiplayerScreenButton.selected();
        titleScreenTextX.tick();
        titleScreenTextY.tick();
        titleScreenButtonX.tick();
        titleScreenButtonY.tick();
        multiplayerScreenButtonX.tick();
        multiplayerScreenButtonY.tick();
        super.tick();
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        renderBackground(ctx);
        ctx.drawCenteredString(font, this.title, width / 2, 5, -1);
        super.render(ctx, mx, my, delta);
    }
}
