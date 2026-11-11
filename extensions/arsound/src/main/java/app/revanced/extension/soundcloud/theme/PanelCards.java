package app.revanced.extension.soundcloud.theme;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import java.lang.reflect.Method;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.soundcloud.settings.Settings;
import app.revanced.extension.soundcloud.shared.Colors;
import app.revanced.extension.soundcloud.shared.Dimens;
import app.revanced.extension.soundcloud.wallpaper.WallpaperStore;

/**
 * Styling helper that wraps SoundCloud's native list rows (such as Library rows,
 * Compose track lists, and settings action lists) into customizable translucent cards
 * with rounded corners and an accent/matte outline.
 */
public final class PanelCards {
    private PanelCards() {
    }

    private static volatile boolean composeInitialized = false;
    private static Method composePaddingMethod;
    private static Method composeShapeMethod;
    private static Method composeColorMethod;
    private static Method composeBgMethod;
    private static Method composeBorderMethod;
    private static Method composeClipMethod;
    private static Object composeCompanion;

    /**
     * Styles a View-based list row (such as library rows) into a rounded card
     * according to user settings.
     */
    public static void styleLibraryRow(View row) {
        if (row == null) return;
        if (!WallpaperStore.isActive() || !Settings.isWallpaperCardsEnabled()) return;

        try {
            Context context = row.getContext();
            int radiusDp = Settings.getWallpaperCardsRadius();
            int strokeOption = Settings.getWallpaperCardsStroke();
            int opacityPercent = Settings.getWallpaperCardsOpacity();

            int accent = ArsoundTheme.palette(context, "special");
            int highlight = ArsoundTheme.palette(context, "highlight");
            int baseFill = highlight != 0 ? highlight : 0x1A1A1E;

            int alpha = (opacityPercent * 255) / 100;
            int fill = (opacityPercent > 0) ? Colors.withAlpha(baseFill, alpha) : 0x00000000;

            int stroke = 0;
            if (strokeOption == 0) {
                stroke = Colors.withAlpha(accent != 0 ? accent : 0xFFFFFF, 0x66);
            } else if (strokeOption == 1) {
                stroke = 0x44FFFFFF;
            }

            GradientDrawable card = new GradientDrawable();
            card.setShape(GradientDrawable.RECTANGLE);
            card.setColor(fill);
            card.setCornerRadius(Dimens.dp(context, radiusDp));
            if (strokeOption != 2 && stroke != 0) {
                card.setStroke(Dimens.dp(context, 1), stroke);
            } else {
                card.setStroke(0, 0);
            }

            android.graphics.drawable.Drawable backgroundDrawable = card;
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                backgroundDrawable = new android.graphics.drawable.RippleDrawable(
                        android.content.res.ColorStateList.valueOf(0x33FFFFFF), card, null);
            }

            row.setBackground(backgroundDrawable);
            row.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            row.setClipToOutline(true);

            ViewGroup.LayoutParams params = row.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                boolean isSlide = row.getClass().getSimpleName().contains("Slide");
                int hMargin = Dimens.dp(context, isSlide ? 6 : 14);
                int vMargin = Dimens.dp(context, 4);
                if (marginParams.leftMargin != hMargin || marginParams.topMargin != vMargin) {
                    marginParams.leftMargin = hMargin;
                    marginParams.rightMargin = hMargin;
                    marginParams.topMargin = vMargin;
                    marginParams.bottomMargin = vMargin;
                    row.setLayoutParams(marginParams);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Wraps a Compose Modifier into a rounded translucent card with border and margin
     * according to user settings (standard full-width list row).
     *
     * @param modifierObj The incoming {@code androidx.compose.ui.Modifier} (may be null).
     * @return The card-wrapped {@code Modifier}.
     */
    public static Object wrapComposeCard(Object modifierObj) {
        return wrapComposeCardInternal(modifierObj, 14.0f, 3.0f);
    }

    /**
     * Wraps a Compose Modifier into a rounded translucent tile for carousels and grids
     * with compact horizontal margins.
     *
     * @param modifierObj The incoming {@code androidx.compose.ui.Modifier} (may be null).
     * @return The card-wrapped {@code Modifier}.
     */
    public static Object wrapComposeTile(Object modifierObj) {
        return wrapComposeCardInternal(modifierObj, 4.0f, 4.0f);
    }

    private static Object wrapComposeCardInternal(Object modifierObj, float hMargin, float vMargin) {
        if (!WallpaperStore.isActive() || !Settings.isWallpaperCardsEnabled()) {
            return modifierObj;
        }

        ensureComposeReflected(modifierObj);
        if (modifierObj == null) {
            modifierObj = composeCompanion;
        }
        if (modifierObj == null || composePaddingMethod == null) {
            return modifierObj;
        }

        try {
            Context context = Utils.getContext();
            int radius = Settings.getWallpaperCardsRadius();
            int strokeOption = Settings.getWallpaperCardsStroke();
            int opacityPercent = Settings.getWallpaperCardsOpacity();

            int highlight = context != null ? ArsoundTheme.palette(context, "highlight") : 0x1A1A1E;
            int baseFill = highlight != 0 ? highlight : 0x1A1A1E;
            int alpha = (opacityPercent * 255) / 100;
            int fillArgb = (opacityPercent > 0) ? Colors.withAlpha(baseFill, alpha) : 0x00000000;

            int strokeArgb = 0;
            if (strokeOption == 0) {
                int accent = context != null ? ArsoundTheme.palette(context, "special") : 0xFFFFFF;
                strokeArgb = Colors.withAlpha(accent != 0 ? accent : 0xFFFFFF, 0x66);
            } else if (strokeOption == 1) {
                strokeArgb = 0x44FFFFFF;
            }

            // Outer margin
            Object padded = composePaddingMethod.invoke(null, modifierObj, hMargin, vMargin);

            // Shape: RoundedCornerShape(radius)
            Object shape = composeShapeMethod.invoke(null, (float) radius);

            // Background
            long fillColorLong = (Long) composeColorMethod.invoke(null, fillArgb);
            Object withBg = composeBgMethod.invoke(null, padded, fillColorLong, shape);

            // Border (if enabled)
            Object withBorder = withBg;
            if (strokeOption != 2 && strokeArgb != 0) {
                long strokeColorLong = (Long) composeColorMethod.invoke(null, strokeArgb);
                withBorder = composeBorderMethod.invoke(null, withBg, 1.0f, strokeColorLong, shape);
            }

            // Clip
            return composeClipMethod.invoke(null, withBorder, shape);
        } catch (Throwable t) {
            return modifierObj;
        }
    }

    private static void ensureComposeReflected(Object modifierObj) {
        if (composeInitialized) return;
        synchronized (PanelCards.class) {
            if (composeInitialized) return;
            try {
                ClassLoader cl = modifierObj != null ? modifierObj.getClass().getClassLoader() : PanelCards.class.getClassLoader();
                Class<?> modifierClass = cl.loadClass("androidx.compose.ui.Modifier");
                composeCompanion = modifierClass.getField("Companion").get(null);
                composePaddingMethod = cl.loadClass("androidx.compose.foundation.layout.PaddingKt")
                        .getMethod("padding-VpY3zN4", modifierClass, float.class, float.class);
                composeShapeMethod = cl.loadClass("androidx.compose.foundation.shape.RoundedCornerShapeKt")
                        .getMethod("RoundedCornerShape-0680j_4", float.class);
                composeColorMethod = cl.loadClass("androidx.compose.ui.graphics.ColorKt")
                        .getMethod("Color", int.class);
                Class<?> shapeClass = cl.loadClass("androidx.compose.ui.graphics.Shape");
                composeBgMethod = cl.loadClass("androidx.compose.foundation.BackgroundKt")
                        .getMethod("background-bw27NRU", modifierClass, long.class, shapeClass);
                composeBorderMethod = cl.loadClass("androidx.compose.foundation.BorderKt")
                        .getMethod("border-xT4_qwU", modifierClass, float.class, long.class, shapeClass);
                composeClipMethod = cl.loadClass("androidx.compose.ui.draw.ClipKt")
                        .getMethod("clip", modifierClass, shapeClass);
            } catch (Throwable t) {
                Logger.printException(() -> "PanelCards: Compose reflection init error", t);
            }
            composeInitialized = true;
        }
    }
}
