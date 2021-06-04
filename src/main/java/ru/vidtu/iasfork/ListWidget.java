package ru.vidtu.iasfork;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class ListWidget extends AbstractParentElement implements Drawable {
	protected static final int NO_DRAG = -1;
	protected static final int DRAG_OUTSIDE = -2;
	protected final MinecraftClient minecraft;
	protected int width;
	protected int height;
	protected int top;
	protected int bottom;
	protected int right;
	protected int left;
	protected final int itemHeight;
	protected boolean centerListVertically = true;
	protected int yDrag = -2;
	protected double scroll;
	protected boolean visible = true;
	protected boolean renderSelection = true;
	protected boolean renderHeader;
	protected int headerHeight;
	private boolean scrolling;

	public ListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
		this.minecraft = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.itemHeight = itemHeight;
		this.left = 0;
		this.right = width;
	}

	public void updateSize(int width, int height, int y, int bottom) {
		this.width = width;
		this.height = height;
		this.top = y;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setRenderSelection(boolean bl) {
		this.renderSelection = bl;
	}

	protected void setRenderHeader(boolean bl, int i) {
		this.renderHeader = bl;
		this.headerHeight = i;
		if (!bl) {
			this.headerHeight = 0;
		}

	}

	public void setVisible(boolean bl) {
		this.visible = bl;
	}

	public boolean isVisible() {
		return this.visible;
	}

	protected abstract int getItemCount();

	public List<? extends Element> children() {
		return Collections.emptyList();
	}

	protected boolean selectItem(int index, int button, double mouseX, double mouseY) {
		return true;
	}

	protected abstract boolean isSelectedItem(int index);

	protected int getMaxPosition() {
		return this.getItemCount() * this.itemHeight + this.headerHeight;
	}

	protected abstract void renderBackground(MatrixStack ms);

	protected void updateItemPosition(int index, int i, int j, float f) {
	}

	protected abstract void renderItem(MatrixStack ms, int index, int y, int i, int j, int k, int l, float f);

	protected void renderHeader(int i, int j, Tessellator tessellator) {
	}

	protected void clickedHeader(int i, int j) {
	}

	protected void renderDecorations(int i, int j) {
	}

	public int getItemAtPosition(double d, double e) {
		int i = this.left + this.width / 2 - this.getRowWidth() / 2;
		int j = this.left + this.width / 2 + this.getRowWidth() / 2;
		int k = MathHelper.floor(e - (double) this.top) - this.headerHeight + (int) this.scroll - 4;
		int l = k / this.itemHeight;
		return d < (double) this.getScrollbarPosition() && d >= (double) i && d <= (double) j && l >= 0 && k >= 0
				&& l < this.getItemCount() ? l : -1;
	}

	protected void capYPosition() {
		this.scroll = MathHelper.clamp(this.scroll, 0.0D, (double) this.getMaxScroll());
	}

	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
	}

	public void centerScrollOn(int i) {
		this.scroll = (double) (i * this.itemHeight + this.itemHeight / 2 - (this.bottom - this.top) / 2);
		this.capYPosition();
	}

	public int getScroll() {
		return (int) this.scroll;
	}

	public boolean isMouseInList(double mouseX, double mouseY) {
		return mouseY >= (double) this.top && mouseY <= (double) this.bottom && mouseX >= (double) this.left
				&& mouseX <= (double) this.right;
	}

	public int getScrollBottom() {
		return (int) this.scroll - this.height - this.headerHeight;
	}

	public void scroll(int amount) {
		this.scroll += (double) amount;
		this.capYPosition();
		this.yDrag = -2;
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
		renderBackground(ms);
		int k = getScrollbarPosition();
		int l = k + 6;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		//this.field_33780 = isMouseOver(i, j) ? getEntryAtPosition(i, j) : null;
		RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(this.left, this.bottom, 0.0D)
				.texture(this.left / 32.0F, (this.bottom + (int) this.scroll) / 32.0F).color(32, 32, 32, 255)
				.next();
		bufferBuilder.vertex(this.right, this.bottom, 0.0D)
				.texture(this.right / 32.0F, (this.bottom + (int) this.scroll) / 32.0F).color(32, 32, 32, 255)
				.next();
		bufferBuilder.vertex(this.right, this.top, 0.0D)
				.texture(this.right / 32.0F, (this.top + (int) this.scroll) / 32.0F).color(32, 32, 32, 255)
				.next();
		bufferBuilder.vertex(this.left, this.top, 0.0D)
				.texture(this.left / 32.0F, (this.top + (int) this.scroll) / 32.0F).color(32, 32, 32, 255)
				.next();
		tessellator.draw();
		int m = this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
		int n = this.top + 4 - (int) this.scroll;
		if (this.renderHeader) renderHeader(m, n, tessellator);
		renderList(ms, m, n, mouseX, mouseY, delta);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(this.left, this.top, -100.0D).texture(0.0F, this.top / 32.0F).color(64, 64, 64, 255)
				.next();
		bufferBuilder.vertex((this.left + this.width), this.top, -100.0D)
				.texture(this.width / 32.0F, this.top / 32.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex((this.left + this.width), 0.0D, -100.0D).texture(this.width / 32.0F, 0.0F)
				.color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left, 0.0D, -100.0D).texture(0.0F, 0.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left, this.height, -100.0D).texture(0.0F, this.height / 32.0F)
				.color(64, 64, 64, 255).next();
		bufferBuilder.vertex((this.left + this.width), this.height, -100.0D)
				.texture(this.width / 32.0F, this.height / 32.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex((this.left + this.width), this.bottom, -100.0D)
				.texture(this.width / 32.0F, this.bottom / 32.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left, this.bottom, -100.0D).texture(0.0F, this.bottom / 32.0F)
				.color(64, 64, 64, 255).next();
		tessellator.draw();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
				GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO,
				GlStateManager.DstFactor.ONE);
		RenderSystem.disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(this.left, (this.top + 4), 0.0D).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.right, (this.top + 4), 0.0D).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.right, this.top, 0.0D).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.left, this.top, 0.0D).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.left, this.bottom, 0.0D).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.right, this.bottom, 0.0D).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.right, (this.bottom - 4), 0.0D).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.left, (this.bottom - 4), 0.0D).color(0, 0, 0, 0).next();
		tessellator.draw();
		int q = getMaxScroll();
		if (q > 0) {
			RenderSystem.disableTexture();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			int r = (int) (((this.bottom - this.top) * (this.bottom - this.top)) / getMaxPosition());
			r = MathHelper.clamp(r, 32, this.bottom - this.top - 8);
			int s = (int) this.scroll * (this.bottom - this.top - r) / q + this.top;
			if (s < this.top)
				s = this.top;
			bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			bufferBuilder.vertex(k, this.bottom, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(l, this.bottom, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(l, this.top, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(k, this.top, 0.0D).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(k, (s + r), 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(l, (s + r), 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(l, s, 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(k, s, 0.0D).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(k, (s + r - 1), 0.0D).color(192, 192, 192, 255).next();
			bufferBuilder.vertex((l - 1), (s + r - 1), 0.0D).color(192, 192, 192, 255).next();
			bufferBuilder.vertex((l - 1), s, 0.0D).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(k, s, 0.0D).color(192, 192, 192, 255).next();
			tessellator.draw();
		}
		renderDecorations(mouseX, mouseY);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	protected void updateScrollingState(double d, double e, int i) {
		this.scrolling = i == 0 && d >= (double) this.getScrollbarPosition()
				&& d < (double) (this.getScrollbarPosition() + 6);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if (this.isVisible() && this.isMouseInList(mouseX, mouseY)) {
			int i = this.getItemAtPosition(mouseX, mouseY);
			if (i == -1 && button == 0) {
				this.clickedHeader((int) (mouseX - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)),
						(int) (mouseY - (double) this.top) + (int) this.scroll - 4);
				return true;
			} else if (i != -1 && this.selectItem(i, button, mouseX, mouseY)) {
				if (this.children().size() > i) {
					this.setFocused((Element) this.children().get(i));
				}

				this.setDragging(true);
				return true;
			} else {
				return this.scrolling;
			}
		} else {
			return false;
		}
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.getFocused() != null) {
			this.getFocused().mouseReleased(mouseX, mouseY, button);
		}

		return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		} else if (this.isVisible() && button == 0 && this.scrolling) {
			if (mouseY < (double) this.top) {
				this.scroll = 0.0D;
			} else if (mouseY > (double) this.bottom) {
				this.scroll = (double) this.getMaxScroll();
			} else {
				double d = (double) this.getMaxScroll();
				if (d < 1.0D) {
					d = 1.0D;
				}

				int i = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top))
						/ (float) this.getMaxPosition());
				i = MathHelper.clamp(i, 32, this.bottom - this.top - 8);
				double e = d / (double) (this.bottom - this.top - i);
				if (e < 1.0D) {
					e = 1.0D;
				}

				this.scroll += deltaY * e;
				this.capYPosition();
			}

			return true;
		} else {
			return false;
		}
	}

	public boolean mouseScrolled(double d, double e, double amount) {
		if (!this.isVisible()) {
			return false;
		} else {
			this.scroll -= amount * (double) this.itemHeight / 2.0D;
			return true;
		}
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!this.isVisible()) {
			return false;
		} else if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 264) {
			this.moveSelection(1);
			return true;
		} else if (keyCode == 265) {
			this.moveSelection(-1);
			return true;
		} else {
			return false;
		}
	}

	protected void moveSelection(int i) {
	}

	public boolean charTyped(char chr, int keyCode) {
		return !this.isVisible() ? false : super.charTyped(chr, keyCode);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.isMouseInList(mouseX, mouseY);
	}

	public int getRowWidth() {
		return 220;
	}

	protected void renderList(MatrixStack ms, int x, int y, int mouseX, int mouseY, float f) {
		int i = this.getItemCount();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		for (int j = 0; j < i; ++j) {
			int k = y + j * this.itemHeight + this.headerHeight;
			int l = this.itemHeight - 4;
			if (k > this.bottom || k + l < this.top) {
				this.updateItemPosition(j, x, k, f);
			}

			if (this.renderSelection && this.isSelectedItem(j)) {
				int t = this.left + this.width / 2 - getRowWidth() / 2;
				int u = this.left + this.width / 2 + getRowWidth() / 2;
				RenderSystem.disableTexture();
				RenderSystem.setShader(GameRenderer::getPositionShader);
				float g = isFocused() ? 1.0F : 0.5F;
				RenderSystem.setShaderColor(g, g, g, 1.0F);
				bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION);
				bufferBuilder.vertex(t, (k + l + 2), 0.0D).next();
				bufferBuilder.vertex(u, (k + l + 2), 0.0D).next();
				bufferBuilder.vertex(u, (k - 2), 0.0D).next();
				bufferBuilder.vertex(t, (k - 2), 0.0D).next();
				tessellator.draw();
				RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
				bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION);
				bufferBuilder.vertex((t + 1), (k + l + 1), 0.0D).next();
				bufferBuilder.vertex((u - 1), (k + l + 1), 0.0D).next();
				bufferBuilder.vertex((u - 1), (k - 1), 0.0D).next();
				bufferBuilder.vertex((t + 1), (k - 1), 0.0D).next();
				tessellator.draw();
				RenderSystem.enableTexture();
			}

			this.renderItem(ms, j, x, k, l, mouseX, mouseY, f);
		}

	}

	protected boolean isFocused() {
		return false;
	}

	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}

	protected void renderHoleBackground(int i, int j, int k, int l) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex((double) this.left, (double) j, 0.0D).texture(0.0F, (float) j / 32.0F).color(64, 64, 64, l)
				.next();
		bufferBuilder.vertex((double) (this.left + this.width), (double) j, 0.0D)
				.texture((float) this.width / 32.0F, (float) j / 32.0F).color(64, 64, 64, l).next();
		bufferBuilder.vertex((double) (this.left + this.width), (double) i, 0.0D)
				.texture((float) this.width / 32.0F, (float) i / 32.0F).color(64, 64, 64, k).next();
		bufferBuilder.vertex((double) this.left, (double) i, 0.0D).texture(0.0F, (float) i / 32.0F).color(64, 64, 64, k)
				.next();
		tessellator.draw();
	}

	public void setLeftPos(int x) {
		this.left = x;
		this.right = x + this.width;
	}

	public int getItemHeight() {
		return this.itemHeight;
	}
}