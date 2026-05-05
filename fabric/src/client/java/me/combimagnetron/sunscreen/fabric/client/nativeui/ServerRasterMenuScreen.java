package me.combimagnetron.sunscreen.fabric.client.nativeui;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.fabric.client.NativeMenuRasterReceiver;
import me.combimagnetron.sunscreen.nativeui.MenuRasterWireCodec;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;

// Resist the urge to rename to SunScreen
public final class ServerRasterMenuScreen extends Screen {
    private final String menuId;
    private @Nullable DirectGlRgbTexture canvas;
    private int logicalWidth;
    private int logicalHeight;
    private int rasterWidth;
    private int rasterHeight;
    private long lastMoveSendMs;

    private boolean leftClicked;
    private boolean rightClicked;

    public ServerRasterMenuScreen(@NotNull String menuId) {
        super(Component.literal("Sunscreen: " + menuId));
        this.menuId = menuId;
    }

    @Override
    protected void init() {
        super.init();
        // Screen init runs again after window resize; re-register per-screen input hooks so
        // mouse press/release forwarding (used by drags) remains active.
        registerInputHooks();
    }

    public @NotNull String menuId() {
        return menuId;
    }

    public void ingestRasterFrame(@NotNull MenuRasterWireCodec.DecodedFrame frame) {
        logicalWidth = frame.logicalWidth();
        logicalHeight = frame.logicalHeight();
        rasterWidth = (logicalWidth + 127) & ~127;
        rasterHeight = (logicalHeight + 127) & ~127;
        boolean resized = canvas == null
            || canvas.width() != rasterWidth
            || canvas.height() != rasterHeight;
        if (resized) {
            if (canvas != null) {
                canvas.close();
            }
            canvas = new DirectGlRgbTexture(rasterWidth, rasterHeight);
        }
        canvas.composeTiles(frame.tiles());
    }

    private @NotNull Vec2i toLogical(double mouseX, double mouseY) {
        if (logicalWidth <= 0 || logicalHeight <= 0) {
            return Vec2i.zero();
        }
        float scale = Math.min(
            (float) width / logicalWidth,
            (float) height / logicalHeight
        );
        int drawW = Math.round(logicalWidth * scale);
        int drawH = Math.round(logicalHeight * scale);
        int ox = (width - drawW) / 2;
        int oy = (height - drawH) / 2;
        double lx = (mouseX - ox) / scale;
        double ly = (mouseY - oy) / scale;
        int ix = (int) Math.clamp(lx, 0.0d, logicalWidth - 1.0d);
        int iy = (int) Math.clamp(ly, 0.0d, logicalHeight - 1.0d);
        return Vec2i.of(ix, iy);
    }

    private void sendMouse(double lx, double ly) {
        var p = toLogical(lx, ly);
        SunscreenLibrary.library().intermediate().clientMenuMouse(p.x(), p.y(), leftClicked, rightClicked);
    }

    private void sendScroll(double deltaY) {
        SunscreenLibrary.library().intermediate().clientMenuScroll((float) deltaY);
    }

    private void sendTextAppend(@NotNull String chars) {
        SunscreenLibrary.library().intermediate().clientMenuTextAppend(chars);
    }

    private void sendTextBackspace() {
        SunscreenLibrary.library().intermediate().clientMenuTextBackspace();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        long now = System.currentTimeMillis();
        if (now - lastMoveSendMs >= 33) {
            lastMoveSendMs = now;
            sendMouse(mouseX, mouseY);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    private boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftClicked = true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                rightClicked = true;
            }
            sendMouse(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                leftClicked = false;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                rightClicked = false;
            }
            sendMouse(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            sendTextBackspace();
            return true;
        }
        if (hasControlDown(modifiers) && keyCode == GLFW.GLFW_KEY_V) {
            long window = nativeWindowHandle(Minecraft.getInstance());
            String clip = glfwGetClipboardString(window);
            if (clip != null && !clip.isEmpty()) {
                int n = Math.min(clip.length(), 8192);
                sendTextAppend(clip.substring(0, n));
            }
            return true;
        }
        String typed = keyToText(keyCode, modifiers);
        if (!typed.isEmpty()) {
            sendTextAppend(typed);
            return true;
        }
        return false;
    }

    private static boolean hasControlDown(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    private static boolean hasShiftDown(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
    }

    private static boolean hasCapsLock(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0;
    }

    private static String keyToText(int keyCode, int modifiers) {
        boolean shift = hasShiftDown(modifiers);
        boolean caps = hasCapsLock(modifiers);

        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            char base = (char) ('a' + (keyCode - GLFW.GLFW_KEY_A));
            return Character.toString((shift ^ caps) ? Character.toUpperCase(base) : base);
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            final char[] normal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
            final char[] shifted = {')', '!', '@', '#', '$', '%', '^', '&', '*', '('};
            int idx = keyCode - GLFW.GLFW_KEY_0;
            return Character.toString(shift ? shifted[idx] : normal[idx]);
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return Character.toString((char) ('0' + (keyCode - GLFW.GLFW_KEY_KP_0)));
        }
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> " ";
            case GLFW.GLFW_KEY_APOSTROPHE -> shift ? "\"" : "'";
            case GLFW.GLFW_KEY_COMMA -> shift ? "<" : ",";
            case GLFW.GLFW_KEY_MINUS -> shift ? "_" : "-";
            case GLFW.GLFW_KEY_PERIOD -> shift ? ">" : ".";
            case GLFW.GLFW_KEY_SLASH -> shift ? "?" : "/";
            case GLFW.GLFW_KEY_SEMICOLON -> shift ? ":" : ";";
            case GLFW.GLFW_KEY_EQUAL -> shift ? "+" : "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> shift ? "{" : "[";
            case GLFW.GLFW_KEY_BACKSLASH -> shift ? "|" : "\\";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> shift ? "}" : "]";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> shift ? "~" : "`";
            default -> "";
        };
    }

    private static long nativeWindowHandle(Minecraft minecraft) {
        var window = minecraft.getWindow();
        #if MC_1_21_1
        return window.getWindow();
        #elif MC_1_21_11
        return window.handle();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    private void registerInputHooks() {
        ScreenMouseEvents.allowMouseClick(this)
        #if MC_1_21_1
                .register((s, x, y, button) -> !onMouseClicked(x, y, button))
        #elif MC_1_21_11
                .register((s, ctx) -> !onMouseClicked(ctx.x(), ctx.y(), ctx.button()));
        #endif;
        ScreenMouseEvents.allowMouseRelease(this)
        #if MC_1_21_1
                .register((s, x, y, button) -> !onMouseReleased(x, y, button))
        #elif MC_1_21_11
                .register((s, ctx) -> !onMouseReleased(ctx.x(), ctx.y(), ctx.button()));
        #endif;
        ScreenKeyboardEvents.allowKeyPress(this)
        #if MC_1_21_1
                .register((s, key, scancode, modifiers) -> !onKeyPressed(key, scancode, modifiers))
        #elif MC_1_21_11
                .register((s, ctx) -> !onKeyPressed(ctx.key(), ctx.scancode(), ctx.modifiers()));
        #endif;
    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        // No background for sunscreens
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (canvas == null || logicalWidth <= 0 || logicalHeight <= 0 || rasterWidth <= 0 || rasterHeight <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int vw = mc.getWindow().getGuiScaledWidth();
        int vh = mc.getWindow().getGuiScaledHeight();
        float scale = Math.min(
                (float) vw / logicalWidth,
                (float) vh / logicalHeight
        );
        int drawW = Math.round(logicalWidth * scale);
        int drawH = Math.round(logicalHeight * scale);
        int ox = (vw - drawW) / 2;
        int oy = (vh - drawH) / 2;
        float uMax = (float) logicalWidth / rasterWidth;
        float vMax = (float) logicalHeight / rasterHeight;
        canvas.draw(graphics, ox, oy, drawW, drawH, uMax, vMax);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (canvas != null) {
            canvas.close();
            canvas = null;
        }
        super.onClose();
        NativeMenuRasterReceiver.resetAll();
    }
}
