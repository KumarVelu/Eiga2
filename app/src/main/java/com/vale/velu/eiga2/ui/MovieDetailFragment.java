package com.vale.velu.eiga2.ui;


import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
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
import com.vale.velu.eiga2.model.MovieTrailer;
import com.vale.velu.eiga2.model.Review;
import com.vale.velu.eiga2.model.Trailer;
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
public class MovieDetailFragment extends BaseFragment implements View.OnClickListener {
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
    @Bind(R.id.play_fab)
    FloatingActionButton playTrailerFab;
    @Bind(R.id.share_fab)
    FloatingActionButton shareFab;
    @Bind(R.id.review_1)
    CardView mReviewView1;
    @Bind(R.id.review_2)
    CardView mReviewView2;
    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Context mContext;
    private Movie mMovie;
    boolean mIsFavourite;

    int mFabId;

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
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
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
        checkIfFavourite();
        setFavIcon();
        setUiWithMovieDetails();
    }

    private void initializeUi() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMovie = null;
                closeFragment();
            }
        });
        favFab.setOnClickListener(this);
        playTrailerFab.setOnClickListener(this);
        shareFab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        mFabId = view.getId();
        switch (mFabId) {
            case R.id.fav_fab:
                mIsFavourite = !mIsFavourite;
                setFavIcon();
                addRemoveFromFavourite();
                break;

            case R.id.play_fab:
                fetchMovieTrailer();
                break;

            case R.id.share_fab:
                fetchMovieTrailer();
                break;
        }
    }

    private void setFavIcon() {
        if (mIsFavourite) {
            favFab.setImageResource(R.drawable.ic_heart_filled);
        } else
            favFab.setImageResource(R.drawable.ic_heart_empty);
    }

    private ContentValues prepareMovieCv() {
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

    private void fetchMovieReviews() {

        if (Utils.isInternetOn(mContext)) {
            showProgressDialog();
            ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);

            Call<MovieReview> movieReviewCall = apiInterface.getMovieReviews(mMovie.getId(),
                    BuildConfig.API_KEY);

            movieReviewCall.enqueue(new Callback<MovieReview>() {
                @Override
                public void onResponse(Call<MovieReview> call, Response<MovieReview> response) {

                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        dismissProgressDialog();
                        setUiWithMovieReviews(response.body().getReviewList());
                    }
                }

                @Override
                public void onFailure(Call<MovieReview> call, Throwable t) {

                }
            });
        } else {
            dismissProgressDialog();
            Utils.showSnackBar(mCoordinatorLayout, getString(R.string.no_internet_connection_to_show_reviews));
        }

    }

    private void setUiWithMovieReviews(List<Review> reviewList) {

        // We will be showing two reviews out of many
        if (reviewList != null && reviewList.size() > 0) {
            if (reviewList.size() >= 2) {
                displayMovieReview(0, reviewList.get(0));
                displayMovieReview(1, reviewList.get(1));
            } else {
                // if there is only 1 review we will show only that
                displayMovieReview(0, reviewList.get(0));
            }
        }
        else{
            Toast.makeText(mContext, getString(R.string.no_reviews), Toast.LENGTH_LONG).show();
        }

    }

    private void displayMovieReview(int position, Review review) {
        CardView reviewView;
        if (position == 0) {
            reviewView = mReviewView1;
            reviewView.findViewById(R.id.review_heading).setVisibility(View.VISIBLE);
            reviewView.findViewById(R.id.review_heading_line).setVisibility(View.VISIBLE);

        } else {
            reviewView = mReviewView2;
        }

        if (reviewView != null) {
            reviewView.setVisibility(View.VISIBLE);
            ((TextView) reviewView.findViewById(R.id.author)).setText(review.getAuthor());
            ((TextView) reviewView.findViewById(R.id.content)).setText(review.getContent());
        }
    }

    private void fetchMovieTrailer() {
        if (Utils.isInternetOn(mContext)) {
            showProgressDialog();
            ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);

            Call<MovieTrailer> movieTrailerCall = apiInterface.getMovieTrailers(mMovie.getId(),
                    BuildConfig.API_KEY);

            movieTrailerCall.enqueue(new Callback<MovieTrailer>() {
                @Override
                public void onResponse(Call<MovieTrailer> call, Response<MovieTrailer> response) {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        MovieTrailer movieTrailer = response.body();
                        dismissProgressDialog();
                        if (mFabId == R.id.play_fab)
                            setUiWithTrailerDialog(movieTrailer.getTrailerList());
                        else if (mFabId == R.id.share_fab)
                            shareMovieTrailer(movieTrailer.getTrailerList());
                    }
                }

                @Override
                public void onFailure(Call<MovieTrailer> call, Throwable t) {

                }
            });
        } else {
            Utils.showSnackBar(mCoordinatorLayout, getString(R.string.no_internet));
        }
    }

    private void setUiWithTrailerDialog(final List<Trailer> trailerList) {
        if (trailerList != null && trailerList.size() > 0) {
            String[] trailerNames = new String[trailerList.size()];
            for (int i = 0; i < trailerList.size(); i++) {
                trailerNames[i] = trailerList.get(i).getName();
            }
            AlertDialog.Builder alertDialog = new AlertDialog
                    .Builder(mContext).setTitle(getString(R.string.trailers))
                    .setItems(trailerNames, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            playMovieTrailer(trailerList.get(position).getKey());
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private void playMovieTrailer(String key) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(Constants.BASE_URL_VIDEO + key));

        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            startActivity(webIntent);
        }
    }

    private void shareMovieTrailer(List<Trailer> trailerList) {
        if (trailerList != null && trailerList.size() > 0) {

            String trailerVideoText = "Hey check out this awesome trailer " +
                    Constants.BASE_URL_VIDEO + trailerList.get(0).getKey();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, trailerVideoText);

            Intent shareIntent = Intent.createChooser(intent, "Share trailer via ");

            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                startActivity(shareIntent);
            }
        }
    }

    private void checkIfFavourite() {
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.buildMovieUri(mMovie.getId()),
                null, null, null, null);

        if(cursor != null && cursor.getCount() > 0)
            mIsFavourite = true;
        else
            mIsFavourite = false;

        cursor.close();
    }

    private void addRemoveFromFavourite(){
        if(mIsFavourite){
            //add to fav(store in db)
            mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, prepareMovieCv());
            Utils.showSnackBar(mCoordinatorLayout, mMovie.getTitle() + " added to favourite ");
        }
        else{
            // remove form fav
            Uri uri = MovieEntry.buildMovieUri(mMovie.getId());
            mContext.getContentResolver().delete(uri, null, null);
            Utils.showSnackBar(mCoordinatorLayout, mMovie.getTitle() + " removed from favourite ");
        }
    }
}
