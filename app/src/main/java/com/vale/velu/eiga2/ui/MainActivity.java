package com.vale.velu.eiga2.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.stetho.Stetho;
import com.vale.velu.eiga2.R;
import com.vale.velu.eiga2.model.Movie;
import com.vale.velu.eiga2.ui.assist.BaseData;
import com.vale.velu.eiga2.ui.assist.IActionListener;
import com.vale.velu.eiga2.utils.Constants;

public class MainActivity extends AppCompatActivity implements IActionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MovieListFragment mMovieListFragment;
    private MovieDetailFragment mMovieDetailFragment;
    private boolean mDualPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);
        loadHome();

        if(findViewById(R.id.detailFrame) != null)
            mDualPane = true;

        Log.i(TAG, "onCreate: mDualPane " + mDualPane);

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
            mMovieDetailFragment = MovieDetailFragment.newInstance();

            mMovieDetailFragment.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            if(mDualPane){
                ft.replace(R.id.detailFrame, mMovieDetailFragment).commit();
            }
            else{
                ft.add(R.id.frame, mMovieDetailFragment)
                        .addToBackStack(null).commit();
            }

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
