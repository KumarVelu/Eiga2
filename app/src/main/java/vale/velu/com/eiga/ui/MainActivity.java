package vale.velu.com.eiga.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import vale.velu.com.eiga.R;
import vale.velu.com.eiga.model.Movie;
import vale.velu.com.eiga.ui.assist.BaseData;
import vale.velu.com.eiga.ui.assist.IActionListener;
import vale.velu.com.eiga.utils.Constants;

public class MainActivity extends AppCompatActivity implements IActionListener {

    private MovieListFragment mMovieListFragment;
    private MovieDetailFragment mMovieDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadHome();
        if (savedInstanceState != null) {
            Movie movie = (Movie) savedInstanceState.getSerializable(MovieDetailFragment.MOVIE_KEY);
            loadDetail(movie);
        }
    }

    private void loadHome() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mMovieListFragment == null)
            mMovieListFragment = MovieListFragment.newInstance(this);
        ft.replace(R.id.frame, mMovieListFragment);
        ft.commit();
    }

    private void loadDetail(Movie movie) {
        Bundle args = new Bundle();
        args.putSerializable(MovieDetailFragment.MOVIE_KEY, movie);
        if (movie != null) {
            if (mMovieDetailFragment == null) {
                mMovieDetailFragment = MovieDetailFragment.newInstance();
                mMovieDetailFragment.setArguments(args);
            } else
                mMovieDetailFragment.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frame, mMovieDetailFragment)
                    .addToBackStack(null).commit();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMovieDetailFragment != null && mMovieDetailFragment.getMovie() != null)
            outState.putSerializable(MovieDetailFragment.MOVIE_KEY, mMovieDetailFragment.getMovie());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAction(View v, BaseData data) {
        if (data.getDataType().equals(Constants.MOVIE)) {
            loadDetail((Movie) data);
        }
    }

    @Override
    public void onBackPressed() {
        if (mMovieDetailFragment != null && mMovieDetailFragment.isAdded()) {
            mMovieDetailFragment.onBackPressed();
        }
        super.onBackPressed();
    }
}
