package com.michaelsolati.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

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

    MovieObject(String movieName, String movieId, String moviePoster, String movieRelease, String movieRating, String movieSummary) {

        name = movieName;
        id = movieId;
        poster = "http://image.tmdb.org/t/p/w500/" + moviePoster;
        release = movieRelease.substring(5,7)+"/"+movieRelease.substring(8,10)+"/"+movieRelease.substring(2,4);
        rating = movieRating;
        summary = movieSummary;
    }

    protected MovieObject(Parcel in) {
        name = in.readString();
        id = in.readString();
        poster = in.readString();
        release = in.readString();
        rating = in.readString();
        summary = in.readString();
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
    }
}
