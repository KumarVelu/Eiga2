package vale.velu.com.eiga.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import java.net.HttpURLConnection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vale.velu.com.eiga.BuildConfig;
import vale.velu.com.eiga.R;
import vale.velu.com.eiga.adapter.MovieListAdapter;
import vale.velu.com.eiga.model.Movie;
import vale.velu.com.eiga.model.MovieResult;
import vale.velu.com.eiga.services.ApiInterface;
import vale.velu.com.eiga.services.ServiceGenerator;
import vale.velu.com.eiga.ui.assist.IActionListener;
import vale.velu.com.eiga.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MovieListFragment.class.getSimpleName();

    @Bind(R.id.recylerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.movie_list_layout)
    LinearLayout mMovieListLayout;
    @Bind(R.id.no_internet_layout)
    RelativeLayout mNoInternetLayout;
    @Bind(R.id.retry)
    Button retryBtn;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.coordinatorLayout)
    View mRootView;

    private Context mContext;
    private MovieListAdapter mMovieListAdapter;

    private IActionListener mParentListener;


    public static MovieListFragment newInstance(IActionListener listener) {
        MovieListFragment fragment = new MovieListFragment();
        fragment.mParentListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
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
        initializeUi();
        fetchMovies();
    }

    private void initializeUi() {
        mMovieListAdapter = new MovieListAdapter(mContext, new MovieListAdapter.MovieListAdapterOnClickHandler() {
            @Override
            public void onClick(Movie movie) {
                if (mParentListener != null)
                    mParentListener.onAction(null, movie);
            }
        });
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        mRecyclerView.setAdapter(mMovieListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(mContext).
                registerOnSharedPreferenceChangeListener(this);
    }

    private void fetchMovies() {

        mSwipeRefreshLayout.setRefreshing(true);

        if (Utils.isInternetOn(mContext)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            String sortCriteria = sp.getString(getString(R.string.sort_order_pref_key),
                    getString(R.string.default_sort_order));

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
            Snackbar.make(getView(), getString(R.string.no_internet), Snackbar.LENGTH_LONG).show();
        }
    }
}
