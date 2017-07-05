package com.vale.velu.eiga2.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.facebook.stetho.Stetho;
import com.vale.velu.eiga2.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MovieListFragment mMovieListFragment;
    private boolean mDualPane;
    public static final String DUAL_PANE_KEY = "isDualPane";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);

        if (findViewById(R.id.detailFrame) != null)
            mDualPane = true;

        if (savedInstanceState == null)
            loadMovieListFragment();
    }

    private void loadMovieListFragment() {
        Bundle args = new Bundle();
        args.putBoolean(DUAL_PANE_KEY, mDualPane);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mMovieListFragment == null)
            mMovieListFragment = MovieListFragment.newInstance();

        mMovieListFragment.setArguments(args);
        ft.replace(R.id.frame, mMovieListFragment);
        ft.commit();
    }
}
