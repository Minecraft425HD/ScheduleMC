package de.rolandsw.schedulemc.weapon.handler;

import com.mojang.blaze3d.platform.InputConstants;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.weapon.attachment.Attachment;
import de.rolandsw.schedulemc.weapon.gun.GunItem;
import de.rolandsw.schedulemc.weapon.network.WeaponFirePacket;
import de.rolandsw.schedulemc.weapon.network.WeaponPackets;
import de.rolandsw.schedulemc.weapon.network.WeaponReloadPacket;
import de.rolandsw.schedulemc.weapon.network.WeaponSetAmmoTypePacket;
import de.rolandsw.schedulemc.weapon.network.WeaponStartAutoFirePacket;
import de.rolandsw.schedulemc.weapon.network.WeaponStopAutoFirePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, value = Dist.CLIENT)
public class WeaponClientEventHandler {
    private static boolean isScopeZoomed = false;
    private static boolean isMouseDown = false;

    private static long lastFireMs = 0L;
    private static long cooldownMs = 0L;

    private static float localCooldownPercent() {
        if (cooldownMs <= 0) return 0f;
        float f = 1f - (float)(System.currentTimeMillis() - lastFireMs) / cooldownMs;
        return Math.max(0f, f);
    }

    private static boolean rIsDown = false;
    private static long rPressTime = 0L;
    private static boolean isAmmoSelectActive = false;
    private static int selectedAmmoIndex = 0;

    private static final String[] AMMO_NAMES = {
        "Standard-Munition",
        "Panzerbrechende Munition",
        "Leuchtspurmunition",
        "Gummigeschosse"
    };

    private static boolean canSendToServer() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.getConnection() != null && mc.player != null;
    }

    private static void safeSendToServer(Object packet) {
        if (!canSendToServer()) {
            return;
        }
        WeaponPackets.sendToServer(packet);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GunItem gun) {
            GuiGraphics graphics = event.getGuiGraphics();
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();

            int ammo = gun.getCurrentAmmo(mainHand);
            int maxAmmo = gun.getProperties().getMaxAmmo();
            String ammoText = "Ammo: " + ammo + "/" + maxAmmo;
            graphics.drawString(mc.font, ammoText, w - 110, h - 50, 0xFFFFFF);

            int mode = gun.getFireMode(mainHand);
            String modeText = switch (mode) {
                case 0 -> "Single";
                case 1 -> "Burst";
                case 2 -> "Auto";
                default -> "";
            };
            graphics.drawString(mc.font, "Mode: " + modeText, w - 110, h - 40, 0xFFFF55);

            float cooldown = localCooldownPercent();
            if (cooldown > 0f) {
                int barWidth = 80;
                int barX = w - 110;
                int barY = h - 28;
                graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + 5, 0x88000000);
                graphics.fill(barX, barY, barX + (int)(barWidth * cooldown), barY + 4, 0xFFFF4422);
            }

            if (isAmmoSelectActive) {
                renderAmmoSelectionOverlay(graphics, mc, w, h);
            }
        }
    }

    private static void renderAmmoSelectionOverlay(GuiGraphics graphics, Minecraft mc, int w, int h) {
        int panelWidth  = 170;
        int entryHeight = 14;
        int padding     = 4;
        int panelHeight = padding * 2 + entryHeight * 4 + padding * 3;
        int panelX = w - panelWidth - 12;
        int panelY = h / 2 - panelHeight / 2;

        graphics.fill(panelX - 2, panelY - 14,
                      panelX + panelWidth + 2, panelY + panelHeight + 2,
                      0xAA000000);
        graphics.drawString(mc.font, "Munitionstyp", panelX + 4, panelY - 10, 0xFFCCCCCC);

        for (int i = 0; i < 4; i++) {
            int entryY = panelY + padding + i * (entryHeight + padding);
            boolean selected = (i == selectedAmmoIndex);

            if (selected) {
                graphics.fill(panelX, entryY - 1,
                              panelX + panelWidth, entryY + entryHeight - 1,
                              0x88FFAA00);
            }

            int textColor = selected ? 0xFFFFAA00 : 0xFFFFFFFF;
            String prefix = selected ? "> " : "  ";
            graphics.drawString(mc.font, prefix + AMMO_NAMES[i], panelX + 4, entryY + 2, textColor);
        }
    }



    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (rIsDown && !isAmmoSelectActive) {
            if (System.currentTimeMillis() - rPressTime >= 300L) {
                isAmmoSelectActive = true;
            }
        }
        if (isAmmoSelectActive) {
            if (mc.screen != null || !(mc.player.getMainHandItem().getItem() instanceof GunItem)) {
                rIsDown = false;
                isAmmoSelectActive = false;
            }
        }

        LocalPlayer player = mc.player;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GunItem gun) {
            boolean hasScope = gun.getAttachments(mainHand).stream()
                    .anyMatch(a -> a.getType() == Attachment.Type.SCOPE);
            if (hasScope) {
                if (mc.options.keyShift.isDown()) {
                    if (!isScopeZoomed) {
                        mc.options.fov().set(30);
                        isScopeZoomed = true;
                    }
                } else {
                    if (isScopeZoomed) {
                        mc.options.fov().set(70);
                        isScopeZoomed = false;
                    }
                }
            } else if (isScopeZoomed) {
                mc.options.fov().set(70);
                isScopeZoomed = false;
            }

            long handle = mc.getWindow().getWindow();
            boolean isLeftDown = mc.screen == null
                    && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            int fireMode = gun.getFireMode(mainHand);
            if (isLeftDown) {
                if (fireMode == 2) {
                    if (!isMouseDown) {
                        safeSendToServer(new WeaponStartAutoFirePacket(mc.player.getInventory().selected));
                        isMouseDown = true;
                    }
                } else {
                    if (!isMouseDown) {
                        isMouseDown = true;
                        if (localCooldownPercent() == 0f) {
                            int shots = fireMode == 1 ? 3 : 1;
                            safeSendToServer(new WeaponFirePacket(mc.player.getInventory().selected, shots));
                            lastFireMs = System.currentTimeMillis();
                            cooldownMs = gun.getCurrentCooldown(mainHand) * 50L;
                        }
                    }
                }
            } else {
                if (isMouseDown) {
                    if (fireMode == 2) {
                        safeSendToServer(new WeaponStopAutoFirePacket());
                    }
                    isMouseDown = false;
                }
            }
        } else {
            if (isScopeZoomed) {
                mc.options.fov().set(70);
                isScopeZoomed = false;
            }
            if (isMouseDown) {
                safeSendToServer(new WeaponStopAutoFirePacket());
                isMouseDown = false;
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getKey() != GLFW.GLFW_KEY_R) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            rIsDown = true;
            rPressTime = System.currentTimeMillis();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ItemStack mainHand = mc.player.getMainHandItem();
                if (mainHand.getItem() instanceof GunItem gun) {
                    Item loaded = gun.getLoadedAmmoType(mainHand);
                    selectedAmmoIndex = ammoItemToIndex(loaded);
                }
            }
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (isAmmoSelectActive) {
                safeSendToServer(new WeaponSetAmmoTypePacket(selectedAmmoIndex));
            } else if (System.currentTimeMillis() - rPressTime < 300L) {
                safeSendToServer(new WeaponReloadPacket());
            }
            rIsDown = false;
            isAmmoSelectActive = false;
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof GunItem) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!isAmmoSelectActive) return;
        event.setCanceled(true);
        if (event.getScrollDelta() > 0) {
            selectedAmmoIndex = (selectedAmmoIndex + 1) % 4;
        } else {
            selectedAmmoIndex = (selectedAmmoIndex + 3) % 4;
        }
    }

    private static int ammoItemToIndex(Item item) {
        if (item == WeaponSetAmmoTypePacket.AMMO_TYPES.get(1).get()) return 1;
        if (item == WeaponSetAmmoTypePacket.AMMO_TYPES.get(2).get()) return 2;
        if (item == WeaponSetAmmoTypePacket.AMMO_TYPES.get(3).get()) return 3;
        return 0;
    }
}
