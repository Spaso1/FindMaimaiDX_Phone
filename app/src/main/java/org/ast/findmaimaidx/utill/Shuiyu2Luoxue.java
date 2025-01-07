package org.ast.findmaimaidx.utill;

import org.ast.findmaimaidx.been.Lx_chart;
import org.ast.findmaimaidx.been.PlayerData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Shuiyu2Luoxue {
    public static ArrayList<Lx_chart> shuiyu2luoxue(PlayerData playerData) {
        ArrayList<Lx_chart> lx_charts = new ArrayList<>();
        for (int i = 0; i < playerData.getCharts().getDx().size(); i++) {
            Lx_chart lx_chart = new Lx_chart();
            if(playerData.getCharts().getDx().get(i).getSongId() > 10000) {
                lx_chart.setId(playerData.getCharts().getDx().get(i).getSongId() - 10000);
            }else {
                lx_chart.setId(playerData.getCharts().getDx().get(i).getSongId());
            }
            lx_chart.setSong_name(playerData.getCharts().getDx().get(i).getTitle());
            lx_chart.setLevel(playerData.getCharts().getDx().get(i).getLevel());
            lx_chart.setLevel_index(playerData.getCharts().getDx().get(i).getLevel_index());
            lx_chart.setAchievements(playerData.getCharts().getDx().get(i).getAchievements());
            lx_chart.setDx_rating(playerData.getCharts().getDx().get(i).getRa());
            lx_chart.setDx_score(playerData.getCharts().getDx().get(i).getDxScore());
            lx_chart.setFs(playerData.getCharts().getDx().get(i).getFs());
            lx_chart.setFc(playerData.getCharts().getDx().get(i).getFc());
            if(lx_chart.getFc().equals("")) {
                lx_chart.setFc(null);
            }
            if(lx_chart.getFs().equals("")) {
                lx_chart.setFs(null);
            }
            lx_chart.setRate(playerData.getCharts().getDx().get(i).getRate());
            if(playerData.getCharts().getDx().get(i).getType().equals("SD")) {
                lx_chart.setType("standard");
            }else if(playerData.getCharts().getDx().get(i).getType().equals("DX")) {
                lx_chart.setType("dx");
            }
            // 获取当前时间戳
            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            // 格式化时间为ISO 8601字符串
            String iso8601String = now.toString().split("\\.")[0] + "Z";
            lx_chart.setUpload_time(iso8601String);
            lx_charts.add(lx_chart);
        }
        for (int i = 0; i < playerData.getCharts().getSd().size(); i++) {
            Lx_chart lx_chart = new Lx_chart();
            if(playerData.getCharts().getSd().get(i).getSongId() > 10000) {
                lx_chart.setId(playerData.getCharts().getSd().get(i).getSongId() - 10000);
            }else {
                lx_chart.setId(playerData.getCharts().getSd().get(i).getSongId());
            }
            lx_chart.setSong_name(playerData.getCharts().getSd().get(i).getTitle());
            lx_chart.setLevel(playerData.getCharts().getSd().get(i).getLevel());
            lx_chart.setLevel_index(playerData.getCharts().getSd().get(i).getLevel_index());
            lx_chart.setAchievements(playerData.getCharts().getSd().get(i).getAchievements());
            lx_chart.setDx_rating(playerData.getCharts().getSd().get(i).getRa());
            lx_chart.setDx_score(playerData.getCharts().getSd().get(i).getDxScore());
            lx_chart.setFs(playerData.getCharts().getSd().get(i).getFs());
            lx_chart.setFc(playerData.getCharts().getSd().get(i).getFc());
            if(lx_chart.getFc().equals("")) {
                lx_chart.setFc(null);
            }
            if(lx_chart.getFs().equals("")) {
                lx_chart.setFs(null);
            }
            lx_chart.setRate(playerData.getCharts().getSd().get(i).getRate());
            if (playerData.getCharts().getSd().get(i).getType().equals("SD")) {
                lx_chart.setType("standard");
            } else if (playerData.getCharts().getSd().get(i).getType().equals("DX")) {
                lx_chart.setType("dx");
            }
            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            String iso8601String = now.toString().split("\\.")[0] + "Z";
            lx_chart.setUpload_time(iso8601String);
            lx_charts.add(lx_chart);
        }
        return lx_charts;
    }
}
