package org.astral.findmaimaiultra.utill;

import org.astral.findmaimaiultra.service.GitHubApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitHubApiClient {
    private static final String BASE_URL = "https://api.github.com/";
    private static Retrofit retrofit = null;

    public static GitHubApiService getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GitHubApiService.class);
    }
}
