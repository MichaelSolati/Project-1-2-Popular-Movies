package com.michaelsolati.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mkslt04 on 5/1/16.
 */

public class MovieObject implements Parcelable {
    private String name;
    private String id;
    private String poster;
    private String release;
    private String rating;
    private String summary;
    private String trailerUrl;

    MovieObject(String movieName, String movieId, String moviePoster, String movieRelease, String movieRating, String movieSummary) {

        name = movieName;
        id = movieId;
        poster = "http://image.tmdb.org/t/p/w500/" + moviePoster;
        release = movieRelease;
        rating = movieRating;
        summary = movieSummary;

        FetchTrailerTask trailerTask = new FetchTrailerTask();
        trailerTask.execute(id);
    }

    protected MovieObject(Parcel in) {
        name = in.readString();
        id = in.readString();
        poster = in.readString();
        release = in.readString();
        rating = in.readString();
        summary = in.readString();
        trailerUrl = in.readString();
    }

    public static final Creator<MovieObject> CREATOR = new Creator<MovieObject>() {
        @Override
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        @Override
        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };

    public String getName() { return name; }

    public String getId() {
        return id;
    }

    public String getPoster() {
        return poster;
    }

    public String getRelease() {
        return release;
    }

    public String getRating() {
        return rating;
    }

    public String getSummary() {
        return summary;
    }

    public String getTrailerUrl() { return trailerUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(poster);
        dest.writeString(release);
        dest.writeString(rating);
        dest.writeString(summary);
        dest.writeString(trailerUrl);
    }

    public class FetchTrailerTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... movieId) {
            final String urlBase = "http://api.themoviedb.org/3/movie/" + movieId[0] + "/videos?";
            final String appIdParam = "api_key";
            String responseBody = null;

            Uri builtUri = Uri.parse(urlBase).buildUpon()
                    .appendQueryParameter(appIdParam, PrivateVariables.getAppId())
                    .build();
            String url = builtUri.toString();

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();

                Call call = client.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    responseBody = response.body().string();
                } else {
                    return null;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
            }

            try {
                // These are the names of the JSON objects that need to be extracted.
                final String OWM_RESULTS = "results";
                final String key = "key";

                JSONObject movieJson = new JSONObject(responseBody);
                JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);
                JSONObject movie = movieArray.getJSONObject(0);
                String movieTrailer = movie.getString(key);

                trailerUrl = "https://www.youtube.com/watch?v=" + movieTrailer;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {

        }
    }
}
