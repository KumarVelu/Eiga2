package com.vale.velu.eiga2.ui;


import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vale.velu.eiga2.BuildConfig;
import com.vale.velu.eiga2.R;
import com.vale.velu.eiga2.data.MovieContract.MovieEntry;
import com.vale.velu.eiga2.model.Movie;
import com.vale.velu.eiga2.model.MovieReview;
import com.vale.velu.eiga2.model.Review;
import com.vale.velu.eiga2.services.ApiInterface;
import com.vale.velu.eiga2.services.ServiceGenerator;
import com.vale.velu.eiga2.utils.Constants;
import com.vale.velu.eiga2.utils.Utils;

import java.net.HttpURLConnection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends BaseFragment implements View.OnClickListener{
    public static final String MOVIE_KEY = "movie";
    private static final String TAG = MovieDetailFragment.class.getSimpleName();

    Toolbar mToolbar;
    @Bind(R.id.backdrop)
    ImageView backDropImage;
    @Bind(R.id.poster_image)
    ImageView posterImage;
    @Bind(R.id.title)
    TextView tvTitle;
    @Bind(R.id.release_date)
    TextView tvReleaseDate;
    @Bind(R.id.rating)
    TextView tvRating;
    @Bind(R.id.plot_synopsis)
    TextView tvPlotSynopsis;
    @Bind(R.id.fav_fab)
    FloatingActionButton favFab;
    @Bind(R.id.review_1)
    CardView mReviewView1;
    @Bind(R.id.review_2)
    CardView mReviewView2;
    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Context mContext;
    private Movie mMovie;

    public static MovieDetailFragment newInstance() {
        return new MovieDetailFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mMovie = (Movie) getArguments().getSerializable(MOVIE_KEY);
        }
        Log.i(TAG, "onCreate: mMovie ==> " + mMovie);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    protected void updateArgs(Bundle args) {
        if (args.getSerializable(MOVIE_KEY) != null) {
            mMovie = (Movie) args.getSerializable(MOVIE_KEY);
            getArguments().putSerializable(MOVIE_KEY, mMovie);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        return rootView;
    }


    private void setUiWithMovieDetails() {

        Picasso.with(mContext).load(Constants.POSTER_PATH_PREFIX + mMovie.getPosterPath()).
                into(posterImage);
        Picasso.with(mContext).load(Constants.BACKDROP_PATH_PREFIX + mMovie.getBackDropPath()).
                into(backDropImage);

        mCollapsingToolbarLayout.setTitle(mMovie.getTitle().trim());
        tvTitle.setText(mMovie.getTitle());
        tvReleaseDate.setText(Utils.formatReleaseDate(mMovie.getReleaseDate()));
        tvRating.setText(mMovie.getRating());
        tvPlotSynopsis.setText(mMovie.getPlotSynopsis());

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUi();
        fetchMovieReviews();
        setUiWithMovieDetails();
    }

    private void initializeUi(){
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMovie = null;
                closeFragment();
            }
        });
        favFab.setOnClickListener(this);

    }

    @Override
    protected void onBackPressed() {
        super.onBackPressed();
        mMovie = null;
    }

    public Movie getMovie() {
        return mMovie;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fav_fab:
                Toast.makeText(mContext, "Fab clicked", Toast.LENGTH_LONG).show();
                mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, prepareMovieCv());
                break;
        }
    }

    private ContentValues prepareMovieCv(){
        ContentValues movieCv = new ContentValues();
        movieCv.put(MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
        movieCv.put(MovieEntry.COLUMN_TITLE, mMovie.getTitle());
        movieCv.put(MovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
        movieCv.put(MovieEntry.COLUMN_RATING, mMovie.getRating());
        movieCv.put(MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
        movieCv.put(MovieEntry.COLUMN_BACKDROP_PATH, mMovie.getBackDropPath());
        movieCv.put(MovieEntry.COLUMN_PLOT_SYNOPSIS, mMovie.getPlotSynopsis());

        return movieCv;
    }

    private void fetchMovieReviews(){

        if(Utils.isInternetOn(mContext)){
            showProgresDialog();
            ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);

            Call<MovieReview> movieReviewCall = apiInterface.getMovieReviews(mMovie.getId(),
                    BuildConfig.API_KEY);

            movieReviewCall.enqueue(new Callback<MovieReview>() {
                @Override
                public void onResponse(Call<MovieReview> call, Response<MovieReview> response) {

                    if(response.code() == HttpURLConnection.HTTP_OK){
                        hideProgressDialog();
                        setUiWithMovieReviews(response.body().getReviewList());
                    }

                }

                @Override
                public void onFailure(Call<MovieReview> call, Throwable t) {

                }
            });
        }
        else{
            hideProgressDialog();
            Utils.showSnackBar(mCoordinatorLayout, getString(R.string.no_internet_connection_to_show_reviews));
        }

    }

    private void setUiWithMovieReviews(List<Review> reviewList){

        // We will be showing two reviews out of many
        if(reviewList != null && reviewList.size() > 0){

            if(reviewList.size() >= 2){
                displayMovieReview(0, reviewList.get(0));
                displayMovieReview(1, reviewList.get(1));
            }
            else {
                // if there is only 1 review we will show only that
                displayMovieReview(0, reviewList.get(0));
            }
        }

    }

    private void displayMovieReview(int position, Review review){

        CardView reviewView;

        if(position == 0){
            reviewView = mReviewView1;
            reviewView.findViewById(R.id.review_heading).setVisibility(View.VISIBLE);
            reviewView.findViewById(R.id.review_heading_line).setVisibility(View.VISIBLE);

        }else{
            reviewView = mReviewView2;
        }

        if(reviewView != null){
            reviewView.setVisibility(View.VISIBLE);
            ((TextView)reviewView.findViewById(R.id.author)).setText(review.getAuthor());
            ((TextView)reviewView.findViewById(R.id.content)).setText(review.getContent());
        }

    }
}
