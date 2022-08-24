package ru.vidtu.ias.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.List;

public class IASAlertScreen extends GuiScreen {
    private final Runnable ok;
    private final String title;
    private final String text;
    private List<String> textList;
    public IASAlertScreen(Runnable ok, String title, String text) {
        this.ok = ok;
        this.title = title;
        this.text = text;
    }

    @Override
    public void initGui() {
        addButton(new GuiButton(0, width / 2 - 50, height - 28, 100, 20, I18n.format("gui.back")));
        textList = fontRenderer.listFormattedStringToWidth(text, width - 50);
        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) ok.run();
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, 30, -1);
        if (textList != null) {
            for (int i = 0; i < textList.size(); i++) {
                drawCenteredString(fontRenderer, textList.get(i), width / 2, 50 + i * 10, -1);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
