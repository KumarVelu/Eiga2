package vale.velu.com.eiga.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kumar_velu on 27-12-2016.
 */
public class MovieResult {

    @SerializedName("results")
    private List<Movie> movieList;

    public List<Movie> getMovieList() {
        return movieList;
    }

    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @Override
    public String toString() {
        return "MovieResult{" +
                "movieList=" + movieList +
                '}';
    }
}
