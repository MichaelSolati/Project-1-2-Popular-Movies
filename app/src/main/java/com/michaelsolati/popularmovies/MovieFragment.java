package com.michaelsolati.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private ArrayAdapter<String> movieNames;
    List<Object> movieObjects = new ArrayList<>();
    private GridView mGridView;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        getMovies();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            getMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = ((GridView) rootView.findViewById(R.id.movie_grid));

        String[] movieArray = {
                "Error Getting Data, Try Again Later"
        };

        ArrayList<String> movieList = new ArrayList<String>(Arrays.asList(movieArray));

        movieNames = new CustomAdapter(getActivity(), movieList);

        mGridView.setAdapter(movieNames);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MovieObject movieObject = (MovieObject) movieObjects.get(i);
                String movieName = movieObject.getName();
                Toast.makeText(getActivity(), movieName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), com.michaelsolati.popularmovies.DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movieObject);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void getMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_popularity));

        if (sort == getString(R.string.pref_units_release)) {
            sort = getString(R.string.pref_units_release);
        }

        moviesTask.execute(sort);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... sortInput) {
            // Will contain the raw JSON response as a string.
            String movieString = null;

            final String appId = "YOUR_API_KEY";
            final String releaseDate = Calendar.getInstance().get(Calendar.YEAR)+"12-31";
            final String sort = sortInput[0];

            final String urlBase = "https://api.themoviedb.org/3/discover/movie/?";
            final String appIdParam = "api_key";
            final String releaseDateFilter = "primary_release_date.lte";
            final String sortParam = "sort_by";

            Uri builtUri = Uri.parse(urlBase).buildUpon()
                    .appendQueryParameter(appIdParam, appId)
                    .appendQueryParameter(releaseDateFilter, releaseDate)
                    .appendQueryParameter(sortParam, sort)
                    .build();
            String url = builtUri.toString();

            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();

                Call call = client.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    movieString = response.body().string();
                } else {
                    return null;
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }

            try {
                return readMovieData(movieString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if (results != null) {
                movieNames.clear();
                movieNames.addAll(results);
            }
        }

        /**
         * Take the String representing the complete movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] readMovieData(String movieString)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_TITLE = "original_title";
            final String OWM_ID = "id";
            final String OWM_POSTER = "poster_path";
            final String OWM_RELEASE = "release_date";
            final String OWM_RATING = "vote_average";
            final String OWM_SUMMARY = "overview";


            JSONObject movieJson = new JSONObject(movieString);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);
            List<String> moviePosters = new ArrayList<>();
            for (int i = 0; i < movieArray.length(); i++) {
                // Get the JSON object representing the day
                JSONObject movie = movieArray.getJSONObject(i);
                String movieTitle = movie.getString(OWM_TITLE);
                String movieId = movie.getString(OWM_ID);
                String moviePoster = movie.getString(OWM_POSTER);
                String movieRelease = movie.getString(OWM_RELEASE);
                String movieRating = movie.getString(OWM_RATING);
                String movieSummary = movie.getString(OWM_SUMMARY);

                MovieObject movieObject = new MovieObject(movieTitle, movieId, moviePoster, movieRelease, movieRating, movieSummary);

                if (moviePoster.matches("(.*)jpg(.*)")) {
                    movieObjects.add(movieObject);
                    moviePosters.add(moviePoster);
                }
            }

            String[] resultStrs = moviePosters.toArray(new String[moviePosters.size()]);

            return resultStrs;
        }
    }
}
