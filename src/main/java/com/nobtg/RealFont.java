package com.nobtg;

import com.nobtg.Mixins.Mixins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.function.Function;

public class RealFont extends Font {
    public static float tick = 0.0F;

    private RealFont(Function<ResourceLocation, FontSet> function, boolean b) {
        super(function, b);
    }

    @Contract(" -> new")
    public static @NotNull Font getInstance() {
        return new RealFont(((Mixins.FontProxyMixin)((Mixins.MinecraftProxyMixin)Minecraft.getInstance()).getfontManager().createFont()).getfonts(), false);
    }

    @Override
    public int drawInBatch(@NotNull FormattedCharSequence formattedCharSequence, float x, float y, int rgb, boolean b1, @NotNull Matrix4f matrix4f, @NotNull MultiBufferSource multiBufferSource, @NotNull DisplayMode mode, int i, int i1) {
        StringBuilder stringBuilder = new StringBuilder();
        formattedCharSequence.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        String text = stringBuilder.toString().replaceAll("(?i)§[0-9A-FK-OR]", "");
        for (int index = 0; index < text.length(); index++) {
            String s = String.valueOf(text.charAt(index));
            float offset_y = y + ((float) Math.sin(((index + 1) + (float) System.nanoTime() / 1000000L / 300.0F)));
            int color = Color.HSBtoRGB(((tick + index) % 720.0f >= 360.0f ? 720.0f - (tick + index) % 720.0f : (tick + index) % 720.0f) / 100.0F,
                    0.8f, 0.8f);
            super.drawInBatch(s, x, offset_y, color, b1, matrix4f, multiBufferSource, mode, i, i1);
            super.drawInBatch(s, x + 0.75F, offset_y + 0.75F, color, b1, matrix4f, multiBufferSource, mode, i, i1);
            x += this.width(s);
        }
        return (int) x;
    }

    @Override
    public int drawInBatch(@NotNull Component component, float x, float y, int rgb, boolean b, @NotNull Matrix4f matrix4f, @NotNull MultiBufferSource multiBufferSource, @NotNull DisplayMode displayMode, int i, int i1) {
        return this.drawInBatch(component.getVisualOrderText(), x, y, rgb, b, matrix4f, multiBufferSource, displayMode, i, i1);
    }
}
