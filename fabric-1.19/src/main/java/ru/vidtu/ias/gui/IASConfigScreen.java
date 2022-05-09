package ru.vidtu.ias.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import ru.vidtu.ias.Config;

/**
 * Screen for editing IAS config.
 *
 * @author VidTu
 */
public class IASConfigScreen extends Screen {
    public final Screen prev;
    public Checkbox caseSensitive, multiplayerScreen, titleScreen;
    public EditBox textX, textY, buttonX, buttonY;

    public IASConfigScreen(Screen prev) {
        super(Component.literal("config/ias.json"));
        this.prev = prev;
    }

    @Override
    public void init() {
        addRenderableWidget(caseSensitive = new Checkbox(width / 2 - font.width(Component.translatable("ias.cfg.casesensitive"))
                / 2 - 24, 40, 24 + font.width(Component.translatable("ias.cfg.casesensitive")), 20,
                Component.translatable("ias.cfg.casesensitive"), Config.caseSensitiveSearch));
        addRenderableWidget(multiplayerScreen = new Checkbox(width / 2 - font.width(Component.translatable("ias.cfg.mpscreen"))
                / 2 - 24, 60, 24 + font.width(Component.translatable("ias.cfg.mpscreen")), 20,
                Component.translatable("ias.cfg.mpscreen"), Config.showOnMPScreen));
        addRenderableWidget(titleScreen = new Checkbox(width / 2 - font.width(Component.translatable("ias.cfg.titlescreen"))
                / 2 - 24, 80, 24 + font.width(Component.translatable("ias.cfg.titlescreen")), 20,
                Component.translatable("ias.cfg.titlescreen"), Config.showOnTitleScreen));
        addRenderableWidget(textX = new EditBox(font, width / 2 - 100, 110, 98, 20, Component.literal("X")));
        addRenderableWidget(textY = new EditBox(font, width / 2 + 2, 110, 98, 20, Component.literal("Y")));
        addRenderableWidget(buttonX = new EditBox(font, width / 2 - 100, 152, 98, 20, Component.literal("X")));
        addRenderableWidget(buttonY = new EditBox(font, width / 2 + 2, 152, 98, 20, Component.literal("Y")));
        addRenderableWidget(new Button(width / 2 - 75, height - 24, 150, 20,
                Component.translatable("gui.done"), btn -> minecraft.setScreen(prev)));
        textX.setValue(StringUtils.trimToEmpty(Config.textX));
        textY.setValue(StringUtils.trimToEmpty(Config.textY));
        buttonX.setValue(StringUtils.trimToEmpty(Config.btnX));
        buttonY.setValue(StringUtils.trimToEmpty(Config.btnY));
    }

    @Override
    public void removed() {
        Config.caseSensitiveSearch = caseSensitive.selected();
        Config.showOnMPScreen = multiplayerScreen.selected();
        Config.showOnTitleScreen = titleScreen.selected();
        Config.textX = textX.getValue();
        Config.textY = textY.getValue();
        Config.btnX = buttonX.getValue();
        Config.btnY = buttonY.getValue();
        Config.save();
    }

    @Override
    public void tick() {
        buttonX.visible = titleScreen.selected();
        buttonY.visible = titleScreen.selected();
        textX.tick();
        textY.tick();
        buttonX.tick();
        buttonY.tick();
        textX.setSuggestion(textX.getValue().isEmpty() ? "X" : "");
        textY.setSuggestion(textY.getValue().isEmpty() ? "Y" : "");
        buttonX.setSuggestion(buttonX.getValue().isEmpty() ? "X" : "");
        buttonY.setSuggestion(buttonY.getValue().isEmpty() ? "Y" : "");
        super.tick();
    }

    @Override
    public void render(PoseStack ms, int mx, int my, float delta) {
        renderBackground(ms);
        drawCenteredString(ms, font, this.title, width / 2, 10, -1);
        drawCenteredString(ms, font, Component.translatable("ias.cfg.textpos"), width / 2, 100, -1);
        if (titleScreen.selected()) drawCenteredString(ms, font, Component.translatable("ias.cfg.buttonpos"), width / 2, 142, -1);
        super.render(ms, mx, my, delta);
    }
}
