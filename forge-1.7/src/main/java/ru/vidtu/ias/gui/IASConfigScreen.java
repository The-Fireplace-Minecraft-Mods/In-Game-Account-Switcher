package ru.vidtu.ias.gui;

import cpw.mods.fml.client.config.GuiCheckBox;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import ru.vidtu.ias.Config;

import java.util.Objects;

/**
 * Screen for editing IAS config.
 *
 * @author VidTu
 */
public class IASConfigScreen extends GuiScreen {
    private final GuiScreen prev;
    private GuiCheckBox titleScreenText;
    private GuiTextField titleScreenTextX;
    private GuiTextField titleScreenTextY;
    private GuiButton titleScreenTextAlignment;
    private GuiCheckBox titleScreenButton;
    private GuiTextField titleScreenButtonX;
    private GuiTextField titleScreenButtonY;
    private GuiCheckBox multiplayerScreenButton;
    private GuiTextField multiplayerScreenButtonX;
    private GuiTextField multiplayerScreenButtonY;

    public IASConfigScreen(GuiScreen prev) {
        this.prev = prev;
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiButton(0, width / 2 - 75, height - 28, 150, 20, I18n.format("gui.done")));
        buttonList.add(titleScreenText = new GuiCheckBox(1, 5, 20 + 4, I18n.format(
                        "ias.configGui.titleScreenText"), Config.titleScreenText));
        titleScreenTextX = new GuiTextField(fontRendererObj, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.titleScreenText")), 20, 50, 20);
        titleScreenTextY = new GuiTextField(fontRendererObj, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.titleScreenText")) + 54, 20, 50, 20);
        buttonList.add(titleScreenTextAlignment = new GuiButton(4, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.titleScreenText")) + 108, 20, fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.titleScreenText.alignment", I18n.format(Config.titleScreenTextAlignment.key()))) + 20, 20,
                I18n.format("ias.configGui.titleScreenText.alignment", I18n.format(Config.titleScreenTextAlignment.key()))));
        buttonList.add(titleScreenButton = new GuiCheckBox(5, 5, 44 + 4, I18n.format(
                "ias.configGui.titleScreenButton"), Config.titleScreenButton));
        titleScreenButtonX = new GuiTextField(fontRendererObj, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.titleScreenButton")), 44, 50, 20);
        titleScreenButtonY = new GuiTextField(fontRendererObj, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.titleScreenButton")) + 54, 44, 50, 20);
        buttonList.add(multiplayerScreenButton = new GuiCheckBox(8, 5, 68 + 4, I18n.format(
                "ias.configGui.multiplayerScreenButton"), Config.multiplayerScreenButton));
        multiplayerScreenButtonX = new GuiTextField(fontRendererObj, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.multiplayerScreenButton")), 68, 50, 20);
        multiplayerScreenButtonY = new GuiTextField(fontRendererObj, 35 + fontRendererObj.getStringWidth(I18n.format(
                "ias.configGui.multiplayerScreenButton")) + 54, 68, 50, 20);
        titleScreenTextX.setText(Objects.toString(Config.titleScreenTextX, ""));
        titleScreenTextY.setText(Objects.toString(Config.titleScreenTextY, ""));
        titleScreenButtonX.setText(Objects.toString(Config.titleScreenButtonX, ""));
        titleScreenButtonY.setText(Objects.toString(Config.titleScreenButtonY, ""));
        multiplayerScreenButtonX.setText(Objects.toString(Config.multiplayerScreenButtonX, ""));
        multiplayerScreenButtonY.setText(Objects.toString(Config.multiplayerScreenButtonY, ""));
        updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) mc.displayGuiScreen(prev);
        else if (button.id == 4) changeAlignment();
        super.actionPerformed(button);
    }

    private void changeAlignment() {
        int i = Config.titleScreenTextAlignment.ordinal() + 1;
        if (i >= Config.Alignment.values().length) i = 0;
        Config.titleScreenTextAlignment = Config.Alignment.values()[i];
        titleScreenTextAlignment.displayString = I18n.format("ias.configGui.titleScreenText.alignment",
                I18n.format(Config.titleScreenTextAlignment.key()));
        titleScreenTextAlignment.width = fontRendererObj.getStringWidth(titleScreenTextAlignment.displayString) + 20;
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn)  {
        titleScreenTextX.mouseClicked(mx, my, btn);
        titleScreenTextY.mouseClicked(mx, my, btn);
        titleScreenButtonX.mouseClicked(mx, my, btn);
        titleScreenButtonY.mouseClicked(mx, my, btn);
        multiplayerScreenButtonX.mouseClicked(mx, my, btn);
        multiplayerScreenButtonY.mouseClicked(mx, my, btn);
        super.mouseClicked(mx, my, btn);
    }

    @Override
    public void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prev);
            return;
        }
        if (titleScreenTextX.textboxKeyTyped(c, key) || titleScreenTextY.textboxKeyTyped(c, key) ||
                titleScreenButtonX.textboxKeyTyped(c, key) || titleScreenButtonY.textboxKeyTyped(c, key) ||
                multiplayerScreenButtonX.textboxKeyTyped(c, key) || multiplayerScreenButtonY.textboxKeyTyped(c, key)) return;
        super.keyTyped(c, key);
    }

    @Override
    public void onGuiClosed() {
        Config.titleScreenText = titleScreenText.isChecked();
        Config.titleScreenTextX = titleScreenTextX.getText().trim().isEmpty() ? null : titleScreenTextX.getText();
        Config.titleScreenTextY = titleScreenTextY.getText().trim().isEmpty() ? null : titleScreenTextY.getText();
        Config.titleScreenButton = titleScreenButton.isChecked();
        Config.titleScreenButtonX = titleScreenButtonX.getText().trim().isEmpty() ? null : titleScreenButtonX.getText();
        Config.titleScreenButtonY = titleScreenButtonY.getText().trim().isEmpty() ? null : titleScreenButtonY.getText();
        Config.multiplayerScreenButton = multiplayerScreenButton.isChecked();
        Config.multiplayerScreenButtonX = multiplayerScreenButtonX.getText().trim().isEmpty() ? null : multiplayerScreenButtonX.getText();
        Config.multiplayerScreenButtonY = multiplayerScreenButtonY.getText().trim().isEmpty() ? null : multiplayerScreenButtonY.getText();
        Config.save(mc.mcDataDir.toPath());
    }

    @Override
    public void updateScreen() {
        titleScreenTextX.setVisible(titleScreenText.isChecked());
        titleScreenTextY.setVisible(titleScreenText.isChecked());
        titleScreenTextAlignment.visible = titleScreenText.isChecked();
        titleScreenButtonX.setVisible(titleScreenButton.isChecked());
        titleScreenButtonY.setVisible(titleScreenButton.isChecked());
        multiplayerScreenButtonX.setVisible(multiplayerScreenButton.isChecked());
        multiplayerScreenButtonY.setVisible(multiplayerScreenButton.isChecked());
        titleScreenTextX.updateCursorCounter();
        titleScreenTextY.updateCursorCounter();
        titleScreenButtonX.updateCursorCounter();
        titleScreenButtonY.updateCursorCounter();
        multiplayerScreenButtonX.updateCursorCounter();
        multiplayerScreenButtonY.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mx, int my, float delta) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("ias.configGui.title"), width / 2, 5, -1);
        titleScreenTextX.drawTextBox();
        titleScreenTextY.drawTextBox();
        titleScreenButtonX.drawTextBox();
        titleScreenButtonY.drawTextBox();
        multiplayerScreenButtonX.drawTextBox();
        multiplayerScreenButtonY.drawTextBox();
        if (titleScreenTextX.getVisible() && titleScreenTextX.getText().isEmpty()) drawString(fontRendererObj, "X", titleScreenTextX.xPosition + 4, titleScreenTextX.yPosition + (titleScreenTextX.height - 8) / 2, 0xFF808080);
        if (titleScreenTextY.getVisible() && titleScreenTextY.getText().isEmpty()) drawString(fontRendererObj, "Y", titleScreenTextY.xPosition + 4, titleScreenTextY.yPosition + (titleScreenTextY.height - 8) / 2, 0xFF808080);
        if (titleScreenButtonX.getVisible() && titleScreenButtonX.getText().isEmpty()) drawString(fontRendererObj, "X", titleScreenButtonX.xPosition + 4, titleScreenButtonX.yPosition + (titleScreenButtonX.height - 8) / 2, 0xFF808080);
        if (titleScreenButtonY.getVisible() && titleScreenButtonY.getText().isEmpty()) drawString(fontRendererObj, "Y", titleScreenButtonY.xPosition + 4, titleScreenButtonY.yPosition + (titleScreenButtonY.height - 8) / 2, 0xFF808080);
        if (multiplayerScreenButtonX.getVisible() && multiplayerScreenButtonX.getText().isEmpty()) drawString(fontRendererObj, "X", multiplayerScreenButtonX.xPosition + 4, multiplayerScreenButtonX.yPosition + (multiplayerScreenButtonX.height - 8) / 2, 0xFF808080);
        if (multiplayerScreenButtonY.getVisible() && multiplayerScreenButtonY.getText().isEmpty()) drawString(fontRendererObj, "Y", multiplayerScreenButtonY.xPosition + 4, multiplayerScreenButtonY.yPosition + (multiplayerScreenButtonY.height - 8) / 2, 0xFF808080);
        super.drawScreen(mx, my, delta);
    }
}
