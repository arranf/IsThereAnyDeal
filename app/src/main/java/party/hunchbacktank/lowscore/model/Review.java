package party.hunchbacktank.lowscore.model;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Review extends RealmObject {
    private String reviewTypeValue;
    private int score;
    private long total;
    private String link;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    // Dummy field to enable us to have setters/getters
    @Ignore
    private String reviewType;

    public void setReviewTypeValue(String reviewTypeValue) {
        this.reviewTypeValue = reviewTypeValue;
    }

    public String getReviewTypeValue() {
        return reviewTypeValue;
    }

    public void setReviewType(ReviewType reviewType) {
        setReviewTypeValue(reviewType.toString());
    }

    public ReviewType getReviewType() {
        return ReviewType.valueOf(getReviewTypeValue());
    }
}