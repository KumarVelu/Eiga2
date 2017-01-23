package com.vale.velu.eiga2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kumar_velu on 23-01-2017.
 */
public class MovieTrailer {

    @SerializedName("results")
    private List<Trailer> trailerList;

    public List<Trailer> getTrailerList() {
        return trailerList;
    }

    public void setTrailerList(List<Trailer> trailerList) {
        this.trailerList = trailerList;
    }

    @Override
    public String toString() {
        return "MovieTrailer{" +
                "trailerList=" + trailerList +
                '}';
    }
}
