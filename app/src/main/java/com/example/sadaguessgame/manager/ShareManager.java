package com.example.sadaguessgame.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import com.example.sadaguessgame.R;
import com.example.sadaguessgame.data.GameState;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Generates a shareable game-summary bitmap and fires an ACTION_SEND intent.
 * All user-visible strings are loaded via context.getString() — no hardcoded literals.
 */
public class ShareManager {

    private static final int   W          = 800;
    private static final int   H          = 480;
    private static final float PADDING    = 40f;
    private static final String AUTHORITY = ".fileprovider";

    // ─── Public API ──────────────────────────────────────────────────────────

    public static boolean shareGameSummary(@NonNull Context context,
                                           @NonNull GameState game) {
        Bitmap bmp = buildBitmap(context, game);
        if (bmp == null) return false;

        Uri uri = saveBitmapAndGetUri(context, bmp);
        if (uri == null) return false;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, buildShareText(context, game));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent,
                context.getString(R.string.share_chooser_title)));
        return true;
    }

    // ─── Bitmap builder ──────────────────────────────────────────────────────

    @Nullable
    private static Bitmap buildBitmap(@NonNull Context context,
                                      @NonNull GameState game) {
        try {
            Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            // 1. Background gradient
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setShader(new LinearGradient(
                    0, 0, W, H,
                    new int[]{0xFF0F2137, 0xFF1D426F, 0xFF9CDFEA},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, W, H, bgPaint);

            // 2. Rounded card overlay
            Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cardPaint.setColor(0x22FFFFFF);
            canvas.drawRoundRect(new RectF(PADDING, PADDING, W - PADDING, H - PADDING),
                    30f, 30f, cardPaint);

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);

            // 3. App title
            textPaint.setTextSize(28f);
            textPaint.setAlpha(180);
            canvas.drawText(context.getString(R.string.share_app_name),
                    PADDING + 10, PADDING + 45, textPaint);

            // 4. Group names + "vs"
            textPaint.setAlpha(255);
            textPaint.setTextSize(36f);
            float centerX = W / 2f;
            canvas.drawText(safe(game.groupAName), PADDING + 10, 160, textPaint);
            textPaint.setTextSize(28f);
            canvas.drawText(context.getString(R.string.share_vs_separator),
                    centerX - 22, 155, textPaint);
            textPaint.setTextSize(36f);
            float bNameX = W - PADDING - measureText(safe(game.groupBName), 36f) - 10;
            canvas.drawText(safe(game.groupBName), bNameX, 160, textPaint);

            // 5. Scores
            textPaint.setTextSize(72f);
            textPaint.setColor(0xFFB2A2D0);
            canvas.drawText(String.valueOf(game.getTotalScoreA()), PADDING + 10, 270, textPaint);
            textPaint.setColor(0xFF9CDFEA);
            float bScoreX = W - PADDING - measureText(
                    String.valueOf(game.getTotalScoreB()), 72f) - 10;
            canvas.drawText(String.valueOf(game.getTotalScoreB()), bScoreX, 270, textPaint);

            // 6. Divider
            Paint divPaint = new Paint();
            divPaint.setColor(0x55FFFFFF);
            divPaint.setStrokeWidth(1.5f);
            canvas.drawLine(PADDING, 295, W - PADDING, 295, divPaint);

            // 7. Winner line
            textPaint.setTextSize(26f);
            textPaint.setColor(Color.WHITE);
            int scoreA = game.getTotalScoreA(), scoreB = game.getTotalScoreB();
            String resultLine;
            if (game.suddenDeathWinner != GameState.NO_WINNER) {
                String winner = game.suddenDeathWinner == GameState.GROUP_A
                        ? game.groupAName : game.groupBName;
                resultLine = context.getString(
                        R.string.share_sudden_death_winner_format, safe(winner));
            } else if (scoreA > scoreB) {
                resultLine = context.getString(
                        R.string.share_winner_format, safe(game.groupAName));
            } else if (scoreB > scoreA) {
                resultLine = context.getString(
                        R.string.share_winner_format, safe(game.groupBName));
            } else {
                resultLine = context.getString(R.string.share_draw_label);
            }
            canvas.drawText(resultLine, PADDING + 10, 335, textPaint);

            // 8. Streak + rounds
            textPaint.setTextSize(20f);
            textPaint.setAlpha(180);
            String streakLine = context.getString(R.string.share_streak_format,
                    game.maxStreakA, game.maxStreakB, game.totalRounds);
            canvas.drawText(streakLine, PADDING + 10, 375, textPaint);

            // 9. Watermark
            textPaint.setTextSize(16f);
            textPaint.setAlpha(100);
            canvas.drawText(context.getString(R.string.share_hashtag_watermark),
                    W - PADDING - 110, H - PADDING + 10, textPaint);

            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    // ─── File + URI ──────────────────────────────────────────────────────────

    @Nullable
    private static Uri saveBitmapAndGetUri(@NonNull Context ctx, @NonNull Bitmap bmp) {
        try {
            File dir = new File(ctx.getCacheDir(), "images");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, "game_result_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return FileProvider.getUriForFile(ctx,
                    ctx.getPackageName() + AUTHORITY, file);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Share text ──────────────────────────────────────────────────────────

    private static String buildShareText(@NonNull Context context,
                                         @NonNull GameState game) {
        return context.getString(R.string.share_text_format,
                safe(game.groupAName), game.getTotalScoreA(),
                safe(game.groupBName), game.getTotalScoreB(),
                game.totalRounds);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static String safe(@Nullable String s) {
        return s != null ? s : "";
    }

    private static float measureText(String text, float size) {
        Paint p = new Paint();
        p.setTextSize(size);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        return p.measureText(text);
    }
}