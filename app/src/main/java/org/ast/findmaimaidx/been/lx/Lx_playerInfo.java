package org.ast.findmaimaidx.been.lx;

import com.google.gson.annotations.SerializedName;

public class Lx_playerInfo {
        @SerializedName("success")
        private boolean success;
        @SerializedName("code")
        private int code;
        @SerializedName("data")
        private Data data;

        // Getters and Setters

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            @SerializedName("name")
            private String name;
            @SerializedName("rating")
            private int rating;
            @SerializedName("friend_code")
            private long friendCode;
            @SerializedName("trophy")
            private Trophy trophy;
            @SerializedName("course_rank")
            private int courseRank;
            @SerializedName("class_rank")
            private int classRank;
            @SerializedName("star")
            private int star;
            @SerializedName("icon")
            private Icon icon;
            @SerializedName("upload_time")
            private String uploadTime;

            // Getters and Setters

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getRating() {
                return rating;
            }

            public void setRating(int rating) {
                this.rating = rating;
            }

            public long getFriendCode() {
                return friendCode;
            }

            public void setFriendCode(long friendCode) {
                this.friendCode = friendCode;
            }

            public Trophy getTrophy() {
                return trophy;
            }

            public void setTrophy(Trophy trophy) {
                this.trophy = trophy;
            }

            public int getCourseRank() {
                return courseRank;
            }

            public void setCourseRank(int courseRank) {
                this.courseRank = courseRank;
            }

            public int getClassRank() {
                return classRank;
            }

            public void setClassRank(int classRank) {
                this.classRank = classRank;
            }

            public int getStar() {
                return star;
            }

            public void setStar(int star) {
                this.star = star;
            }

            public Icon getIcon() {
                return icon;
            }

            public void setIcon(Icon icon) {
                this.icon = icon;
            }

            public String getUploadTime() {
                return uploadTime;
            }

            public void setUploadTime(String uploadTime) {
                this.uploadTime = uploadTime;
            }
        }

        public static class Trophy {
            @SerializedName("id")
            private int id;
            @SerializedName("name")
            private String name;
            @SerializedName("color")
            private String color;

            // Getters and Setters

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }
        }

        public static class Icon {
            @SerializedName("id")
            private int id;
            @SerializedName("name")
            private String name;
            @SerializedName("genre")
            private String genre;

            // Getters and Setters

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getGenre() {
                return genre;
            }

            public void setGenre(String genre) {
                this.genre = genre;
            }
        }
    }
