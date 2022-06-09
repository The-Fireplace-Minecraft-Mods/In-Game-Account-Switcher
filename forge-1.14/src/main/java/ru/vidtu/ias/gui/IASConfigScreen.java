package ru.vidtu.ias.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;
import ru.vidtu.ias.Config;

/**
 * Screen for editing IAS config.
 *
 * @author VidTu
 */
public class IASConfigScreen extends Screen {
    public final Screen prev;
    public CheckboxButton caseSensitive, multiplayerScreen, titleScreen;
    public TextFieldWidget textX, textY, buttonX, buttonY;

    public IASConfigScreen(Screen prev) {
        super(new StringTextComponent("config/ias.json"));
        this.prev = prev;
    }

    @Override
    public void init() {
        addButton(caseSensitive = new CheckboxButton(width / 2 - font.width(I18n.get("ias.cfg.casesensitive"))
                / 2 - 12, 40, 24 + font.width(I18n.get("ias.cfg.casesensitive")), 20,
                I18n.get("ias.cfg.casesensitive"), Config.caseSensitiveSearch));
        addButton(multiplayerScreen = new CheckboxButton(width / 2 - font.width(I18n.get("ias.cfg.mpscreen"))
                / 2 - 12, 60, 24 + font.width(I18n.get("ias.cfg.mpscreen")), 20,
                I18n.get("ias.cfg.mpscreen"), Config.showOnMPScreen));
        addButton(titleScreen = new CheckboxButton(width / 2 - font.width(I18n.get("ias.cfg.titlescreen"))
                / 2 - 12, 80, 24 + font.width(I18n.get("ias.cfg.titlescreen")), 20,
                I18n.get("ias.cfg.titlescreen"), Config.showOnTitleScreen));
        addButton(textX = new TextFieldWidget(font, width / 2 - 100, 110, 98, 20, "X"));
        addButton(textY = new TextFieldWidget(font, width / 2 + 2, 110, 98, 20, "Y"));
        addButton(buttonX = new TextFieldWidget(font, width / 2 - 100, 152, 98, 20, "X"));
        addButton(buttonY = new TextFieldWidget(font, width / 2 + 2, 152, 98, 20, "Y"));
        addButton(new Button(width / 2 - 75, height - 24, 150, 20,
                I18n.get("gui.done"), btn -> minecraft.setScreen(prev)));
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
    public void render(int mx, int my, float delta) {
        renderBackground();
        drawCenteredString(font, this.title.getColoredString(), width / 2, 10, -1);
        drawCenteredString(font, I18n.get("ias.cfg.textpos"), width / 2, 100, -1);
        if (titleScreen.selected()) drawCenteredString(font, I18n.get("ias.cfg.buttonpos"), width / 2, 142, -1);
        super.render(mx, my, delta);
    }
}
