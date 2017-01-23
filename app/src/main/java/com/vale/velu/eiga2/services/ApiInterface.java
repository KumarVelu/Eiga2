package com.vale.velu.eiga2.services;

import com.vale.velu.eiga2.model.MovieResult;
import com.vale.velu.eiga2.model.MovieReview;
import com.vale.velu.eiga2.model.MovieTrailer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by kumar_velu on 27-12-2016.
 */
public interface ApiInterface {

    @GET("/3/movie/{sort_criteria}")
    Call<MovieResult> getMovies(@Path("sort_criteria") String sortCriteria, @Query("api_key") String apiKey);

    @GET("/3/movie/{movie_id}/reviews")
    Call<MovieReview> getMovieReviews(@Path("movie_id") int movieId, @Query("api_key") String apiKey);

    @GET("/3/movie/{movie_id}/videos")
    Call<MovieTrailer> getMovieTrailers(@Path("movie_id") int movieId, @Query("api_key") String apiKey);
}
