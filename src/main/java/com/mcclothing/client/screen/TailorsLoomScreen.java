package com.mcclothing.client.screen;

import com.mcclothing.MCClothing;
import com.mcclothing.pattern.ClothingPattern;
import com.mcclothing.pattern.ClothingPatterns;
import com.mcclothing.screen.TailorsLoomScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Minimal Tailor's Loom screen — vanilla-loom background reuse for shell, a
 * column of clickable pattern buttons on the right. Pattern preview textures
 * are not drawn yet (the renderer overlays them on the player live); future
 * pass can render mini-previews per pattern button.
 */
public class TailorsLoomScreen extends HandledScreen<TailorsLoomScreenHandler> {
    private static final Identifier BG = Identifier.ofVanilla("textures/gui/container/loom.png");
    private static final int BUTTON_SIZE = 14;
    private static final int BUTTONS_PER_ROW = 4;

    public TailorsLoomScreen(TailorsLoomScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        ctx.drawTexture(BG, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        List<RegistryKey<ClothingPattern>> keys = handler.availablePatterns();
        int selected = handler.getSelectedPattern();
        int startX = x + 60;
        int startY = y + 13;
        for (int i = 0; i < keys.size(); i++) {
            int col = i % BUTTONS_PER_ROW;
            int row = i / BUTTONS_PER_ROW;
            int bx = startX + col * (BUTTON_SIZE + 2);
            int by = startY + row * (BUTTON_SIZE + 2);
            int color = (i == selected) ? 0xFFFFCC55 : 0xFF6F6F6F;
            ctx.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, color);
            ctx.drawBorder(bx, by, BUTTON_SIZE, BUTTON_SIZE, 0xFF000000);
            // Letter glyph of pattern path so blank textures still ID-able.
            String label = String.valueOf(Character.toUpperCase(keys.get(i).getValue().getPath().charAt(0)));
            ctx.drawText(textRenderer, label, bx + 4, by + 3, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        List<RegistryKey<ClothingPattern>> keys = handler.availablePatterns();
        int startX = x + 60;
        int startY = y + 13;
        for (int i = 0; i < keys.size(); i++) {
            int col = i % BUTTONS_PER_ROW;
            int row = i / BUTTONS_PER_ROW;
            int bx = startX + col * (BUTTON_SIZE + 2);
            int by = startY + row * (BUTTON_SIZE + 2);
            if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
                if (client != null && client.interactionManager != null) {
                    client.interactionManager.clickButton(handler.syncId, i);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        ctx.drawText(textRenderer, title, titleX, titleY, 0x404040, false);
        ctx.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, 0x404040, false);
    }
}
