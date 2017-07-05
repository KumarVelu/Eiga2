package com.vale.velu.eiga2.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.vale.velu.eiga2.BuildConfig;
import com.vale.velu.eiga2.R;
import com.vale.velu.eiga2.adapter.MovieListAdapter;
import com.vale.velu.eiga2.data.MovieContract.MovieEntry;
import com.vale.velu.eiga2.model.Movie;
import com.vale.velu.eiga2.model.MovieResult;
import com.vale.velu.eiga2.services.ApiInterface;
import com.vale.velu.eiga2.services.ServiceGenerator;
import com.vale.velu.eiga2.utils.Utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>,
        MovieListAdapter.IMovieClickListener {

    private static final String TAG = MovieListFragment.class.getSimpleName();

    @Bind(R.id.recylerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.movie_list_layout)
    LinearLayout mMovieListLayout;
    @Bind(R.id.no_internet_layout)
    RelativeLayout mNoInternetLayout;
    @Bind(R.id.no_fav_layout)
    RelativeLayout mNoFavMovieLayout;
    @Bind(R.id.retry)
    Button retryBtn;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;

    private boolean mDualPane;
    private Context mContext;
    private MovieListAdapter mMovieListAdapter;
    private static final int MOVIE_LOADER = 0;
    private String mSortCriteria;

    private final String[] MOVIE_COLUMNS = {
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_BACKDROP_PATH,
            MovieEntry.COLUMN_PLOT_SYNOPSIS
    };

    // these indices are tied to MOVIE_COLUMNS, if MOVIE_COLUMNS
    // changes this must changes as well
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_TITLE = 1;
    private static final int COL_POSTER_PATH = 2;
    private static final int COL_RATING = 3;
    private static final int COL_RELEASE_DATE = 4;
    private static final int COL_BACKDROP_PATH = 5;
    private static final int COL_PLOT_SYNOPSIS = 6;

    public static MovieListFragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mDualPane = getArguments().getBoolean(MainActivity.DUAL_PANE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mToolbar.inflateMenu(R.menu.main_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        startActivity(new Intent(mContext, SettingsActivity.class));
                        return true;
                }
                return false;
            }
        });

        retryBtn.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        getActivity().getSupportLoaderManager().initLoader(MOVIE_LOADER, null, this);
        initializeUi();
        fetchMovies();
    }

    private void initializeUi() {
        mMovieListAdapter = new MovieListAdapter(mContext, this);

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        else
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        mRecyclerView.setAdapter(mMovieListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(mContext).
                registerOnSharedPreferenceChangeListener(this);
    }

    private void fetchMovies() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSortCriteria = sp.getString(getString(R.string.sort_order_pref_key),
                getString(R.string.default_sort_order));

        if (mSortCriteria.equals(getString(R.string.favourite))) {
            fetchMoviesFromDb();
        } else {
            fetchMoviesFromApi(mSortCriteria);
        }
    }

    private void fetchMoviesFromApi(String sortCriteria) {

        mSwipeRefreshLayout.setRefreshing(true);
        if (Utils.isInternetOn(mContext)) {
            ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
            Call<MovieResult> movieListCall = apiInterface.getMovies(sortCriteria, BuildConfig.API_KEY);
            movieListCall.enqueue(new Callback<MovieResult>() {
                @Override
                public void onResponse(Call<MovieResult> call, Response<MovieResult> response) {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        setUiWithMovieList(response.body().getMovieList());
                    }
                }

                @Override
                public void onFailure(Call<MovieResult> call, Throwable t) {
                    Log.d(TAG, "onFailure => " + t.getMessage());
                }
            });
        } else {
            mMovieListLayout.setVisibility(View.GONE);
            mNoInternetLayout.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void setUiWithMovieList(List<Movie> movieList) {
        mSwipeRefreshLayout.setRefreshing(false);
        mNoInternetLayout.setVisibility(View.GONE);
        mNoFavMovieLayout.setVisibility(View.GONE);
        mMovieListLayout.setVisibility(View.VISIBLE);
        mMovieListAdapter.swapData(movieList);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sort_order_pref_key))) {
            fetchMovies();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(mContext).
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.retry:
                fetchMovies();
                break;
        }
    }

    @Override
    public void onRefresh() {

        if (Utils.isInternetOn(mContext)) {
            fetchMovies();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Utils.showSnackBar(mCoordinatorLayout, getString(R.string.no_internet));
        }
    }

    private void fetchMoviesFromDb() {
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS, null, null, null);

        List<Movie> movieList = getMovieListFromCursor(cursor);
        if (movieList != null)
            setUiWithMovieList(movieList);
        else
            showNoMovieMarkedAsFav();

    }

    private void showNoMovieMarkedAsFav() {
        mMovieListLayout.setVisibility(View.GONE);
        mNoFavMovieLayout.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private List<Movie> getMovieListFromCursor(Cursor cursor) {

        List<Movie> movieList = null;
        if (cursor != null && cursor.getCount() > 0) {
            movieList = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(COL_MOVIE_ID));
                movie.setTitle(cursor.getString(COL_MOVIE_TITLE));
                movie.setBackDropPath(cursor.getString(COL_BACKDROP_PATH));
                movie.setRating(cursor.getString(COL_RATING));
                movie.setPosterPath(cursor.getString(COL_POSTER_PATH));
                movie.setPlotSynopsis(cursor.getString(COL_PLOT_SYNOPSIS));
                movie.setReleaseDate(cursor.getString(COL_RELEASE_DATE));

                movieList.add(movie);
                cursor.moveToNext();
            }
        }
        return movieList;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri favMovieUri = MovieEntry.CONTENT_URI;
        return new CursorLoader(mContext, favMovieUri,
                MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mSortCriteria != null && mSortCriteria.equals(getString(R.string.favourite))) {
            List<Movie> movieList = getMovieListFromCursor(data);
            if (movieList != null)
                setUiWithMovieList(movieList);
            else
                showNoMovieMarkedAsFav();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onMovieClick(Movie movie) {
        Bundle args = new Bundle();
        args.putSerializable(MovieDetailFragment.MOVIE_KEY, movie);
        MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance();
        movieDetailFragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (mDualPane) {
            ft.replace(R.id.detailFrame, movieDetailFragment).commit();
        } else {
            ft.add(R.id.frame, movieDetailFragment)
                    .addToBackStack(null).commit();
        }
    }
}
