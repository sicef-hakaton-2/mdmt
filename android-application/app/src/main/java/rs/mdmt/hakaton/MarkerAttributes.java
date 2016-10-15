package rs.mdmt.hakaton;

public class MarkerAttributes {

    private String markerId;
    private String userId;

    public MarkerAttributes(String userId, String markerId) {
        this.userId = userId;
        this.markerId = markerId.substring(10, markerId.length() - 2);
    }

    public String getMarkerId() {
        return markerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
