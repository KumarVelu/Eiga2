package com.vale.velu.eiga2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.vale.velu.eiga2.data.MovieContract.MovieEntry;

/**
 * Created by kumar_velu on 22-01-2017.
 */
public class MovieProvider extends ContentProvider {

    private DbHelper mDbHelper;
    private static final int MOVIE = 100;
    private static final int MOVIE_WITH_ID = 101;

    public static UriMatcher sUriMatcher = buildUriMathcher();

    static UriMatcher buildUriMathcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE + "/#",
                MOVIE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor returnCursor;

        switch (sUriMatcher.match(uri)){

            case MOVIE:
                returnCursor = db.query(MovieEntry.TABLE_NAME,
                        projection,
                        null, null, null, null,
                        sortOrder);
                break;

            case MOVIE_WITH_ID:
                returnCursor = db.query(MovieEntry.TABLE_NAME,
                        new String[]{MovieEntry.COLUMN_MOVIE_ID},
                        MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{MovieEntry.getMovieIdFromUri(uri)},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)){
            case MOVIE:
                long _id = db.insert(MovieEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)){
            case MOVIE_WITH_ID:
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME,
                        MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{MovieEntry.getMovieIdFromUri(uri)});
                getContext().getContentResolver().notifyChange(MovieEntry.CONTENT_URI, null);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
