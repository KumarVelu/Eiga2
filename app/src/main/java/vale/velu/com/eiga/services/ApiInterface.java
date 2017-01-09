package vale.velu.com.eiga.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vale.velu.com.eiga.model.MovieResult;

/**
 * Created by kumar_velu on 27-12-2016.
 */
public interface ApiInterface {

    @GET("/3/movie/{sort_criteria}")
    Call<MovieResult> getMovies(@Path("sort_criteria") String sortCriteria, @Query("api_key") String apiKey);

}
