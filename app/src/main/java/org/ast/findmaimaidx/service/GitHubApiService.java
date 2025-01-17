package org.ast.findmaimaidx.service;

import org.ast.findmaimaidx.been.Release;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GitHubApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    Call<Release> getLatestRelease(@Path("owner") String owner, @Path("repo") String repo);
}
