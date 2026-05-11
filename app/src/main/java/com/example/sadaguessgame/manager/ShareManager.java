package com.example.sadaguessgame.manager;

import android.annotation.SuppressLint;
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
 *
 * v3: Plain-text body is now a beautiful human-readable format,
 *     NOT raw JSON. All strings loaded via context.getString().
 */
public class ShareManager {

    private static final int    W         = 800;
    private static final int    H         = 500;
    private static final float  PADDING   = 40f;
    private static final String AUTHORITY = ".fileprovider";

    // ─── Public API ──────────────────────────────────────────────────────────

    public static boolean shareGameSummary(@NonNull Context context,
                                           @NonNull GameState game) {
        Bitmap bmp = buildBitmap(context, game);
        if (bmp == null) return false;

        Uri uri = saveBitmapAndGetUri(context, bmp);
        if (uri == null) return false;

        String shareText = buildBeautifulShareText(context, game);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent,
                context.getString(R.string.share_chooser_title)));
        return true;
    }

    // ─── Beautiful plain-text share body ─────────────────────────────────────

    /**
     * Produces a readable, emoji-rich share message.
     * Example output:
     *
     *   🎮 SadaGuess Game Result
     *
     *   🏅 Team Alpha  —  12 pts
     *   🏅 Team Beta   —   9 pts
     *
     *   📊 Rounds played: 10
     *   🔥 Best streak — A: 4 | B: 2
     *
     *   🏆 Winner: Team Alpha!
     *
     *   #SadaGuess #GuessingGame
     */
    @SuppressLint("StringFormatMatches")
    @NonNull
    private static String buildBeautifulShareText(@NonNull Context context,
                                                  @NonNull GameState game) {
        int scoreA = game.getTotalScoreA();
        int scoreB = game.getTotalScoreB();

        // Determine result line
        String resultLine;
        if (game.suddenDeathWinner != GameState.NO_WINNER) {
            String sdWinner = game.suddenDeathWinner == GameState.GROUP_A
                    ? safe(game.groupAName) : safe(game.groupBName);
            resultLine = context.getString(
                    R.string.share_sudden_death_line_format, sdWinner);
        } else if (scoreA > scoreB) {
            resultLine = context.getString(
                    R.string.share_winner_line_format, safe(game.groupAName));
        } else if (scoreB > scoreA) {
            resultLine = context.getString(
                    R.string.share_winner_line_format, safe(game.groupBName));
        } else {
            resultLine = context.getString(R.string.share_draw_line);
        }

        return context.getString(
                R.string.share_text_format,
                safe(game.groupAName), scoreA,
                safe(game.groupBName), scoreB,
                game.totalRounds,
                game.maxStreakA,
                game.maxStreakB,
                resultLine);
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
                    new int[]{0xFF0F2137, 0xFF1D426F, 0xFF2A5590},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, W, H, bgPaint);

            // 2. Card overlay
            Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cardPaint.setColor(0x22FFFFFF);
            canvas.drawRoundRect(
                    new RectF(PADDING, PADDING, W - PADDING, H - PADDING),
                    24f, 24f, cardPaint);

            Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
            text.setColor(Color.WHITE);
            text.setTypeface(Typeface.DEFAULT_BOLD);

            // 3. App title
            text.setTextSize(22f);
            text.setAlpha(180);
            canvas.drawText(context.getString(R.string.share_app_name),
                    PADDING + 12, PADDING + 36, text);

            // 4. Group names
            text.setAlpha(255);
            text.setTextSize(32f);
            canvas.drawText(safe(game.groupAName), PADDING + 12, 140, text);
            text.setTextSize(24f);
            float centerX = W / 2f;
            canvas.drawText(context.getString(R.string.share_vs_separator),
                    centerX - measureText(context.getString(R.string.share_vs_separator), 24f) / 2,
                    135, text);
            text.setTextSize(32f);
            float bNameW = measureText(safe(game.groupBName), 32f);
            canvas.drawText(safe(game.groupBName),
                    W - PADDING - 12 - bNameW, 140, text);

            // 5. Scores
            int scoreA = game.getTotalScoreA();
            int scoreB = game.getTotalScoreB();
            text.setTextSize(72f);
            text.setColor(0xFFB2A2D0);
            canvas.drawText(String.valueOf(scoreA), PADDING + 12, 250, text);
            text.setColor(0xFF9CDFEA);
            float bScoreW = measureText(String.valueOf(scoreB), 72f);
            canvas.drawText(String.valueOf(scoreB),
                    W - PADDING - 12 - bScoreW, 250, text);

            // 6. Divider
            Paint div = new Paint();
            div.setColor(0x55FFFFFF);
            div.setStrokeWidth(1.5f);
            canvas.drawLine(PADDING, 275, W - PADDING, 275, div);

            // 7. Result line
            text.setTextSize(24f);
            text.setColor(Color.WHITE);
            String resultLine;
            if (game.suddenDeathWinner != GameState.NO_WINNER) {
                String w = game.suddenDeathWinner == GameState.GROUP_A
                        ? safe(game.groupAName) : safe(game.groupBName);
                resultLine = context.getString(R.string.share_sudden_death_winner_format, w);
            } else if (scoreA > scoreB) {
                resultLine = context.getString(
                        R.string.share_winner_format, safe(game.groupAName));
            } else if (scoreB > scoreA) {
                resultLine = context.getString(
                        R.string.share_winner_format, safe(game.groupBName));
            } else {
                resultLine = context.getString(R.string.share_draw_label);
            }
            canvas.drawText(resultLine, PADDING + 12, 315, text);

            // 8. Streak + rounds
            text.setTextSize(18f);
            text.setAlpha(180);
            String streakLine = context.getString(R.string.share_streak_format,
                    game.maxStreakA, game.maxStreakB, game.totalRounds);
            canvas.drawText(streakLine, PADDING + 12, 355, text);

            // 9. Watermark
            text.setTextSize(14f);
            text.setAlpha(100);
            String wm = context.getString(R.string.share_hashtag_watermark);
            canvas.drawText(wm,
                    W - PADDING - measureText(wm, 14f) - 8,
                    H - PADDING + 8, text);

            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    // ─── File + URI ──────────────────────────────────────────────────────────

    @Nullable
    private static Uri saveBitmapAndGetUri(@NonNull Context ctx,
                                           @NonNull Bitmap bmp) {
        try {
            File dir = new File(ctx.getCacheDir(), "images");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir,
                    "game_result_" + System.currentTimeMillis() + ".png");
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

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @NonNull
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