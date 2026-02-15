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
public class WarehouseConfigScreen extends Screen {
    private final Screen parent;

    public WarehouseConfigScreen(Screen parent) {
        super(Component.literal("Warehouse Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int y = 80;
        int s = 25;
        int w = 200;

        this.addRenderableWidget(new IntSlider(centerX - w/2, y, w,
            "Slot Count: §e%d",
            ModConfigHandler.COMMON.WAREHOUSE_SLOT_COUNT, 8, 128));

        this.addRenderableWidget(new IntSlider(centerX - w/2, y + s, w,
            "Max Per Slot: §e%d items",
            ModConfigHandler.COMMON.WAREHOUSE_MAX_CAPACITY_PER_SLOT, 64, 10000));

        this.addRenderableWidget(new IntSlider(centerX - w/2, y + s * 2, w,
            "Delivery Interval: §e%d days",
            ModConfigHandler.COMMON.WAREHOUSE_DELIVERY_INTERVAL_DAYS, 1, 30));

        this.addRenderableWidget(new IntSlider(centerX - w/2, y + s * 3, w,
            "Default Price: §e%d€",
            ModConfigHandler.COMMON.WAREHOUSE_DEFAULT_DELIVERY_PRICE, 1, 10000));

        this.addRenderableWidget(Button.builder(Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent))
            .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("§7Warehouse & Delivery System"),
            this.width / 2, 50, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
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
}
