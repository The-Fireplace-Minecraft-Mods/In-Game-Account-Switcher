package the_fireplace.ias.gui;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import the_fireplace.ias.LiteModIAS;
import the_fireplace.ias.tools.Reference;

/**
 * @author The_Fireplace
 */
public class GuiPanelConfig implements ConfigPanel {

    private GuiCheckbox casesensitive;
    private GuiCheckbox enablerelog;
    private Minecraft minecraft = Minecraft.getMinecraft();

    @Override
    public String getPanelTitle() {
        return Reference.MODNAME+" Settings";
    }

    @Override
    public int getContentHeight() {
        return -1;
    }

    @Override
    public void onPanelShown(ConfigPanelHost host) {
        this.casesensitive = new GuiCheckbox(0, 20, 20, I18n.format("ias.cfg.casesensitive"));
        this.casesensitive.checked = LiteModIAS.instance.CASESENSITIVE;
        this.enablerelog = new GuiCheckbox(1, 20, 40, I18n.format("ias.cfg.enablerelog"));
        this.enablerelog.checked = LiteModIAS.instance.ENABLERELOG;
    }

    @Override
    public void onPanelResize(ConfigPanelHost host) {

    }

    @Override
    public void onPanelHidden() {

    }

    @Override
    public void onTick(ConfigPanelHost host) {

    }

    @Override
    public void drawPanel(ConfigPanelHost host, int mouseX, int mouseY, float partialTicks) {
        this.casesensitive.drawButton(this.minecraft, mouseX, mouseY);
        this.enablerelog.drawButton(this.minecraft, mouseX, mouseY);
    }

    @Override
    public void mousePressed(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) {
        if(this.casesensitive.mousePressed(this.minecraft, mouseX, mouseY)){
            this.casesensitive.checked = !this.casesensitive.checked;
            LiteModIAS.instance.CASESENSITIVE = !LiteModIAS.instance.CASESENSITIVE;
        }
        if(this.enablerelog.mousePressed(this.minecraft, mouseX, mouseY)){
            this.enablerelog.checked = !this.enablerelog.checked;
            LiteModIAS.instance.ENABLERELOG = !LiteModIAS.instance.ENABLERELOG;
        }
    }

    @Override
    public void mouseReleased(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void mouseMoved(ConfigPanelHost host, int mouseX, int mouseY) {

    }

    @Override
    public void keyPressed(ConfigPanelHost host, char keyChar, int keyCode) {

    }
}
