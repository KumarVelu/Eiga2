package com.vale.velu.eiga2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kumar_velu on 22-01-2017.
 */
public class MovieReview {

    @SerializedName("results")
    private List<Review> reviewList;

    public List<Review> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @Override
    public String toString() {
        return "MovieReview{" +
                "reviewList=" + reviewList +
                '}';
    }
}
