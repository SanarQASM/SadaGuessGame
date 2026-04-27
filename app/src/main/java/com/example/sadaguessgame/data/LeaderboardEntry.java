package com.example.sadaguessgame.data;

/**
 * Represents one group's cumulative stats in the local leaderboard.
 * Persisted as a JSON array in SharedPreferences under "leaderboard_prefs".
 *
 * A "group" is identified by its exact name (case-insensitive lookup).
 * Stats accumulate across ALL finished games.
 */
public class LeaderboardEntry {

    public String groupName   = "";
    public int    totalWins   = 0;
    public int    totalLosses = 0;
    public int    totalDraws  = 0;
    public int    currentWinStreak = 0;
    public int    maxWinStreak     = 0;
    public int    totalPointsScored = 0;
    public int    gamesPlayed       = 0;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String groupName) {
        this.groupName = groupName;
    }

    // ─── Derived helpers ──────────────────────────────────────────────────────

    public double getWinRate() {
        if (gamesPlayed == 0) return 0.0;
        return (double) totalWins / gamesPlayed * 100.0;
    }

    public String getWinRateFormatted() {
        return String.format("%.0f%%", getWinRate());
    }

    /** Called when this group wins a game. */
    public void recordWin(int pointsScored) {
        totalWins++;
        gamesPlayed++;
        totalPointsScored += pointsScored;
        currentWinStreak++;
        if (currentWinStreak > maxWinStreak) maxWinStreak = currentWinStreak;
    }

    /** Called when this group loses a game. */
    public void recordLoss(int pointsScored) {
        totalLosses++;
        gamesPlayed++;
        totalPointsScored += pointsScored;
        currentWinStreak = 0;
    }

    /** Called when this game ends in a draw. */
    public void recordDraw(int pointsScored) {
        totalDraws++;
        gamesPlayed++;
        totalPointsScored += pointsScored;
        currentWinStreak = 0;
    }

    /**
     * Sort key: wins DESC, then win-rate DESC, then total points DESC.
     */
    public int getSortScore() {
        return totalWins * 1000 + (int) getWinRate() * 10 + (totalPointsScored / Math.max(1, gamesPlayed));
    }
}