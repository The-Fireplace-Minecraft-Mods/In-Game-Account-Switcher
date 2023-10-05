package ru.vidtu.ias.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import ru.vidtu.ias.Config;

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
        super(new TranslatableComponent("ias.configGui.title"));
        this.prev = prev;
    }

    @Override
    public void init() {
        addRenderableWidget(new Button(width / 2 - 75, height - 28, 150, 20, CommonComponents.GUI_DONE, button -> minecraft.setScreen(prev)));
        addRenderableWidget(titleScreenText = new Checkbox(5, 20, 24 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenText")), 20, new TranslatableComponent(
                        "ias.configGui.titleScreenText"), Config.titleScreenText));
        addRenderableWidget(titleScreenTextX = new EditBox(font, 35 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenText")), 20, 50, 20, new TextComponent("X")));
        addRenderableWidget(titleScreenTextY = new EditBox(font, 35 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenText")) + 54, 20, 50, 20, new TextComponent("Y")));
        addRenderableWidget(titleScreenTextAlignment = new Button(35 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenText")) + 108, 20, font.width(new TranslatableComponent(
                "ias.configGui.titleScreenText.alignment", I18n.get(Config.titleScreenTextAlignment.key()))) + 20, 20,
                new TranslatableComponent("ias.configGui.titleScreenText.alignment", I18n.get(Config.titleScreenTextAlignment.key())),
                btn -> changeAlignment()));
        addRenderableWidget(titleScreenButton = new Checkbox(5, 44, 24 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenButton")), 20, new TranslatableComponent(
                "ias.configGui.titleScreenButton"), Config.titleScreenButton));
        addRenderableWidget(titleScreenButtonX = new EditBox(font, 35 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenButton")), 44, 50, 20, new TextComponent("X")));
        addRenderableWidget(titleScreenButtonY = new EditBox(font, 35 + font.width(new TranslatableComponent(
                "ias.configGui.titleScreenButton")) + 54, 44, 50, 20, new TextComponent("Y")));
        addRenderableWidget(multiplayerScreenButton = new Checkbox(5, 68, 24 + font.width(new TranslatableComponent(
                "ias.configGui.multiplayerScreenButton")), 20, new TranslatableComponent(
                "ias.configGui.multiplayerScreenButton"), Config.multiplayerScreenButton));
        addRenderableWidget(multiplayerScreenButtonX = new EditBox(font, 35 + font.width(new TranslatableComponent(
                "ias.configGui.multiplayerScreenButton")), 68, 50, 20, new TextComponent("X")));
        addRenderableWidget(multiplayerScreenButtonY = new EditBox(font, 35 + font.width(new TranslatableComponent(
                "ias.configGui.multiplayerScreenButton")) + 54, 68, 50, 20, new TextComponent("Y")));
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
        titleScreenTextX.setValue(Objects.toString(Config.titleScreenTextX, ""));
        titleScreenTextY.setValue(Objects.toString(Config.titleScreenTextY, ""));
        titleScreenButtonX.setValue(Objects.toString(Config.titleScreenButtonX, ""));
        titleScreenButtonY.setValue(Objects.toString(Config.titleScreenButtonY, ""));
        multiplayerScreenButtonX.setValue(Objects.toString(Config.multiplayerScreenButtonX, ""));
        multiplayerScreenButtonY.setValue(Objects.toString(Config.multiplayerScreenButtonY, ""));
        tick();
    }

    private void changeAlignment() {
        int i = Config.titleScreenTextAlignment.ordinal() + 1;
        if (i >= Config.Alignment.values().length) i = 0;
        Config.titleScreenTextAlignment = Config.Alignment.values()[i];
        titleScreenTextAlignment.setMessage(new TranslatableComponent("ias.configGui.titleScreenText.alignment",
                I18n.get(Config.titleScreenTextAlignment.key())));
        titleScreenTextAlignment.setWidth(font.width(titleScreenTextAlignment.getMessage()) + 20);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(prev);
    }

    @Override
    public void removed() {
        Config.titleScreenText = titleScreenText.selected();
        Config.titleScreenTextX = titleScreenTextX.getValue().trim().isEmpty() ? null : titleScreenTextX.getValue();
        Config.titleScreenTextY = titleScreenTextY.getValue().trim().isEmpty() ? null : titleScreenTextY.getValue();
        Config.titleScreenButton = titleScreenButton.selected();
        Config.titleScreenButtonX = titleScreenButtonX.getValue().trim().isEmpty() ? null : titleScreenButtonX.getValue();
        Config.titleScreenButtonY = titleScreenButtonY.getValue().trim().isEmpty() ? null : titleScreenButtonY.getValue();
        Config.multiplayerScreenButton = multiplayerScreenButton.selected();
        Config.multiplayerScreenButtonX = multiplayerScreenButtonX.getValue().trim().isEmpty() ? null : multiplayerScreenButtonX.getValue();
        Config.multiplayerScreenButtonY = multiplayerScreenButtonY.getValue().trim().isEmpty() ? null : multiplayerScreenButtonY.getValue();
        Config.save(minecraft.gameDirectory.toPath());
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
    public void render(PoseStack ms, int mx, int my, float delta) {
        renderBackground(ms);
        drawCenteredString(ms, font, this.title, width / 2, 5, -1);
        super.render(ms, mx, my, delta);
    }
}
