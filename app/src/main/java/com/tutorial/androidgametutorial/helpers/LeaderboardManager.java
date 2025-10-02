package com.tutorial.androidgametutorial.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardManager {
    private static final String PREFS_NAME = "GameLeaderboard";
    private static final String KEY_SCORES = "scores";
    private static final String KEY_DATES = "dates";
    private static final int MAX_ENTRIES = 6;

    private SharedPreferences prefs;
    private Context context;

    public LeaderboardManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static class LeaderboardEntry {
        public int score;
        public String date;
        public int rank;

        public LeaderboardEntry(int score, String date, int rank) {
            this.score = score;
            this.date = date;
            this.rank = rank;
        }
    }

    public void addScore(int killCount) {
        List<Integer> scores = getScoresList();
        List<String> dates = getDatesList();

        // Thêm điểm mới với ngày hiện tại
        scores.add(killCount);
        dates.add(getCurrentDate());

        // Sắp xếp điểm từ cao xuống thấp
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            entries.add(new LeaderboardEntry(scores.get(i), dates.get(i), 0));
        }

        // Sắp xếp theo điểm số giảm dần
        Collections.sort(entries, (a, b) -> Integer.compare(b.score, a.score));

        // Giữ lại tối đa MAX_ENTRIES bản ghi
        if (entries.size() > MAX_ENTRIES) {
            entries = entries.subList(0, MAX_ENTRIES);
        }

        // Cập nhật rank
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).rank = i + 1;
        }

        // Lưu lại vào SharedPreferences
        saveEntries(entries);
    }

    public List<LeaderboardEntry> getTop6Scores() {
        List<Integer> scores = getScoresList();
        List<String> dates = getDatesList();

        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(scores.size(), dates.size()); i++) {
            entries.add(new LeaderboardEntry(scores.get(i), dates.get(i), i + 1));
        }

        // Sắp xếp theo điểm số giảm dần
        Collections.sort(entries, (a, b) -> Integer.compare(b.score, a.score));

        // Cập nhật rank sau khi sắp xếp
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).rank = i + 1;
        }

        return entries.subList(0, Math.min(entries.size(), MAX_ENTRIES));
    }

    public boolean isNewRecord(int killCount) {
        List<Integer> scores = getScoresList();
        if (scores.isEmpty()) return true;

        Collections.sort(scores, Collections.reverseOrder());
        return killCount > scores.get(0);
    }

    public int getBestScore() {
        List<Integer> scores = getScoresList();
        if (scores.isEmpty()) return 0;

        Collections.sort(scores, Collections.reverseOrder());
        return scores.get(0);
    }

    private List<Integer> getScoresList() {
        String scoresStr = prefs.getString(KEY_SCORES, "");
        List<Integer> scores = new ArrayList<>();

        if (!scoresStr.isEmpty()) {
            String[] scoreArray = scoresStr.split(",");
            for (String score : scoreArray) {
                try {
                    scores.add(Integer.parseInt(score.trim()));
                } catch (NumberFormatException e) {
                    // Bỏ qua điểm số không hợp lệ
                }
            }
        }
        return scores;
    }

    private List<String> getDatesList() {
        String datesStr = prefs.getString(KEY_DATES, "");
        List<String> dates = new ArrayList<>();

        if (!datesStr.isEmpty()) {
            String[] dateArray = datesStr.split(";");
            for (String date : dateArray) {
                dates.add(date.trim());
            }
        }
        return dates;
    }

    private void saveEntries(List<LeaderboardEntry> entries) {
        StringBuilder scoresBuilder = new StringBuilder();
        StringBuilder datesBuilder = new StringBuilder();

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);

            if (i > 0) {
                scoresBuilder.append(",");
                datesBuilder.append(";");
            }

            scoresBuilder.append(entry.score);
            datesBuilder.append(entry.date);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SCORES, scoresBuilder.toString());
        editor.putString(KEY_DATES, datesBuilder.toString());
        editor.apply();
    }

    private String getCurrentDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new java.util.Date());
    }

    public void clearLeaderboard() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_SCORES);
        editor.remove(KEY_DATES);
        editor.apply();
    }
}
