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
 * Compose track lists, and settings action lists), home screen shortcuts, and carousel
 * slides into customizable translucent cards with rounded corners and an accent/matte outline.
 */
public final class PanelCards {
    private PanelCards() {
    }

    private static volatile boolean composeInitialized = false;
    private static Method composePaddingMethod;
    private static Method composeShapeMethod;
    private static Method composeBgMethod;
    private static Method composeBorderMethod;
    private static Method composeClipMethod;

    /**
     * Styles a View-based list row (such as library rows, action list items, or track cells)
     * into a rounded card according to user settings.
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

            applyRowMargins(row, context);
        } catch (Throwable ignored) {
        }
    }

    private static void applyRowMargins(View row, Context context) {
        ViewGroup.LayoutParams params = row.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            int hMargin = Dimens.dp(context, 14);
            int vMargin = Dimens.dp(context, 4);
            if (marginParams.leftMargin != hMargin || marginParams.topMargin != vMargin) {
                marginParams.leftMargin = hMargin;
                marginParams.rightMargin = hMargin;
                marginParams.topMargin = vMargin;
                marginParams.bottomMargin = vMargin;
                row.setLayoutParams(marginParams);
            }
        } else if (params == null) {
            row.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    v.removeOnAttachStateChangeListener(this);
                    applyRowMargins(v, v.getContext());
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    /**
     * Styles a horizontal carousel slide (such as CellSlidePlaylist, CellSlideTrack, CellSlideUser)
     * as a unified card with rounded corners and an accent/matte outline.
     */
    public static void styleSlideCard(View cardView) {
        if (cardView == null) return;
        if (!WallpaperStore.isActive() || !Settings.isWallpaperCardsEnabled()) return;

        try {
            Context context = cardView.getContext();
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

            cardView.setBackground(backgroundDrawable);
            cardView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            cardView.setClipToOutline(true);

            int pad = Dimens.dp(context, 6);
            cardView.setPadding(pad, pad, pad, pad);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Styles home screen shortcut tiles by replacing their BackgroundKt.background-bw27NRU call.
     */
    public static Object styleShortcutBackground(Object modifierObj, long defaultColor, Object defaultShape) {
        ensureComposeReflected(modifierObj);

        if (!WallpaperStore.isActive() || !Settings.isWallpaperCardsEnabled()) {
            if (composeBgMethod != null) {
                try {
                    return composeBgMethod.invoke(null, modifierObj, defaultColor, defaultShape);
                } catch (Throwable ignored) {
                }
            }
            return modifierObj;
        }

        try {
            Context context = Utils.peekContext();
            int radiusDp = Settings.getWallpaperCardsRadius();
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

            Object shape = defaultShape;
            if (composeShapeMethod != null) {
                try {
                    shape = composeShapeMethod.invoke(null, (float) radiusDp);
                } catch (Throwable ignored) {
                }
            }

            long fillColorLong = ((long) fillArgb & 0xFFFFFFFFL) << 32;
            Object withBg = modifierObj;
            if (composeBgMethod != null) {
                withBg = composeBgMethod.invoke(null, modifierObj, fillColorLong, shape);
            }

            Object withBorder = withBg;
            if (strokeOption != 2 && strokeArgb != 0 && composeBorderMethod != null) {
                long strokeColorLong = ((long) strokeArgb & 0xFFFFFFFFL) << 32;
                withBorder = composeBorderMethod.invoke(null, withBg, 1.0f, strokeColorLong, shape);
            }

            if (composeClipMethod != null) {
                return composeClipMethod.invoke(null, withBorder, shape);
            }
            return withBorder;
        } catch (Throwable t) {
            if (composeBgMethod != null) {
                try {
                    return composeBgMethod.invoke(null, modifierObj, defaultColor, defaultShape);
                } catch (Throwable ignored) {
                }
            }
            return modifierObj;
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
        if (modifierObj == null) {
            return null;
        }
        if (!WallpaperStore.isActive() || !Settings.isWallpaperCardsEnabled()) {
            return modifierObj;
        }

        ensureComposeReflected(modifierObj);

        try {
            Context context = Utils.peekContext();
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

            Object current = modifierObj;
            if (composePaddingMethod != null) {
                try {
                    current = composePaddingMethod.invoke(null, current, 14.0f, 3.0f);
                } catch (Throwable ignored) {
                }
            }

            Object shape = null;
            if (composeShapeMethod != null) {
                try {
                    shape = composeShapeMethod.invoke(null, (float) radius);
                } catch (Throwable ignored) {
                }
            }

            if (composeBgMethod != null && shape != null) {
                try {
                    long fillColorLong = ((long) fillArgb & 0xFFFFFFFFL) << 32;
                    current = composeBgMethod.invoke(null, current, fillColorLong, shape);
                } catch (Throwable ignored) {
                }
            }

            if (strokeOption != 2 && strokeArgb != 0 && composeBorderMethod != null && shape != null) {
                try {
                    long strokeColorLong = ((long) strokeArgb & 0xFFFFFFFFL) << 32;
                    current = composeBorderMethod.invoke(null, current, 1.0f, strokeColorLong, shape);
                } catch (Throwable ignored) {
                }
            }

            if (composeClipMethod != null && shape != null) {
                try {
                    current = composeClipMethod.invoke(null, current, shape);
                } catch (Throwable ignored) {
                }
            }

            return current;
        } catch (Throwable t) {
            return modifierObj;
        }
    }

    private static void ensureComposeReflected(Object modifierObj) {
        if (composeInitialized) return;
        synchronized (PanelCards.class) {
            if (composeInitialized) return;
            ClassLoader cl = modifierObj != null ? modifierObj.getClass().getClassLoader() : PanelCards.class.getClassLoader();
            try {
                Class<?> modifierClass = cl.loadClass("androidx.compose.ui.Modifier");
                Class<?> shapeClass = cl.loadClass("androidx.compose.ui.graphics.Shape");

                try {
                    composePaddingMethod = cl.loadClass("androidx.compose.foundation.layout.PaddingKt")
                            .getMethod("padding-VpY3zN4", modifierClass, float.class, float.class);
                } catch (Throwable ignored) {
                }

                try {
                    composeShapeMethod = cl.loadClass("androidx.compose.foundation.shape.RoundedCornerShapeKt")
                            .getMethod("RoundedCornerShape-0680j_4", float.class);
                } catch (Throwable ignored) {
                }

                try {
                    composeBgMethod = cl.loadClass("androidx.compose.foundation.BackgroundKt")
                            .getMethod("background-bw27NRU", modifierClass, long.class, shapeClass);
                } catch (Throwable ignored) {
                }

                try {
                    composeBorderMethod = cl.loadClass("androidx.compose.foundation.BorderKt")
                            .getMethod("border-xT4_qwU", modifierClass, float.class, long.class, shapeClass);
                } catch (Throwable ignored) {
                }

                try {
                    composeClipMethod = cl.loadClass("androidx.compose.ui.draw.ClipKt")
                            .getMethod("clip", modifierClass, shapeClass);
                } catch (Throwable ignored) {
                }
            } catch (Throwable t) {
                Logger.printException(() -> "PanelCards: Compose reflection init error", t);
            }
            composeInitialized = true;
        }
    }
}
