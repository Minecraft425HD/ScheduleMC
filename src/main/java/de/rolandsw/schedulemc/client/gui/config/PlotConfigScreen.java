package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlotConfigScreen extends Screen {
    private final Screen parent;

    public PlotConfigScreen(Screen parent) {
        super(Component.literal("Plot Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int leftCol = this.width / 2 - 155;
        int rightCol = this.width / 2 + 5;
        int w = 150;
        int y = 60;
        int s = 25;

        // Min Plot Size
        this.addRenderableWidget(new LongSlider(leftCol, y, w,
            "Min Size: §e%d blocks",
            ModConfigHandler.COMMON.MIN_PLOT_SIZE, 64, 10000));

        // Max Plot Size
        this.addRenderableWidget(new LongSlider(rightCol, y, w,
            "Max Size: §e%d blocks",
            ModConfigHandler.COMMON.MAX_PLOT_SIZE, 1000, 1000000));

        // Min Price
        this.addRenderableWidget(new DoubleSlider(leftCol, y + s, w,
            "Min Price: §e%.0f€",
            ModConfigHandler.COMMON.MIN_PLOT_PRICE, 1, 10000));

        // Max Price
        this.addRenderableWidget(new DoubleSlider(rightCol, y + s, w,
            "Max Price: §e%.0f€",
            ModConfigHandler.COMMON.MAX_PLOT_PRICE, 1000, 1000000));

        // Max Trusted Players
        this.addRenderableWidget(new IntSlider(leftCol, y + s * 2, w,
            "Max Trusted: §e%d",
            ModConfigHandler.COMMON.MAX_TRUSTED_PLAYERS, 1, 100));

        // Refund on Abandon
        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 2, w,
            "Refund: §e%.0f%%",
            ModConfigHandler.COMMON.REFUND_ON_ABANDON, 0, 1.0, 100));

        // Toggles
        boolean allowTransfer = ModConfigHandler.COMMON.ALLOW_PLOT_TRANSFER.get();
        this.addRenderableWidget(Button.builder(
            Component.literal("Transfer: " + (allowTransfer ? "§aALLOW" : "§cDENY")),
            button -> {
                boolean c = ModConfigHandler.COMMON.ALLOW_PLOT_TRANSFER.get();
                ModConfigHandler.COMMON.ALLOW_PLOT_TRANSFER.set(!c);
                ModConfigHandler.SPEC.save();
                button.setMessage(Component.literal("Transfer: " + (!c ? "§aALLOW" : "§cDENY")));
            }
        ).bounds(leftCol, y + s * 3, w, 20).build());

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("§7Land & Property System"),
            this.width / 2, 40, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // Slider classes...
    private static class DoubleSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.DoubleValue config;
        private final double min, max, multiplier;
        private final String format;

        public DoubleSlider(int x, int y, int w, String format,
                           net.minecraftforge.common.ForgeConfigSpec.DoubleValue config,
                           double min, double max) {
            this(x, y, w, format, config, min, max, 1.0);
        }

        public DoubleSlider(int x, int y, int w, String format,
                           net.minecraftforge.common.ForgeConfigSpec.DoubleValue config,
                           double min, double max, double multiplier) {
            super(x, y, w, 20, Component.empty(), (config.get() - min) / (max - min));
            this.config = config;
            this.min = min;
            this.max = max;
            this.multiplier = multiplier;
            this.format = format;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + (value * (max - min));
            this.setMessage(Component.literal(String.format(format, val * multiplier)));
        }

        @Override
        protected void applyValue() {
            double val = min + (value * (max - min));
            config.set(val);
            ModConfigHandler.SPEC.save();
        }
    }

    private static class IntSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.IntValue config;
        private final int min, max;
        private final String format;

        public IntSlider(int x, int y, int w, String format,
                        net.minecraftforge.common.ForgeConfigSpec.IntValue config,
                        int min, int max) {
            super(x, y, w, 20, Component.empty(), (double)(config.get() - min) / (max - min));
            this.config = config;
            this.min = min;
            this.max = max;
            this.format = format;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int val = min + (int)(value * (max - min));
            this.setMessage(Component.literal(String.format(format, val)));
        }

        @Override
        protected void applyValue() {
            int val = min + (int)(value * (max - min));
            config.set(val);
            ModConfigHandler.SPEC.save();
        }
    }

    private static class LongSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.LongValue config;
        private final long min, max;
        private final String format;

        public LongSlider(int x, int y, int w, String format,
                         net.minecraftforge.common.ForgeConfigSpec.LongValue config,
                         long min, long max) {
            super(x, y, w, 20, Component.empty(), (double)(config.get() - min) / (max - min));
            this.config = config;
            this.min = min;
            this.max = max;
            this.format = format;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            long val = min + (long)(value * (max - min));
            this.setMessage(Component.literal(String.format(format, val)));
        }

        @Override
        protected void applyValue() {
            long val = min + (long)(value * (max - min));
            config.set(val);
            ModConfigHandler.SPEC.save();
        }
    }
}
