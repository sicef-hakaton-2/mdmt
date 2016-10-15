package rs.mdmt.hakaton;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by DjoleObli on 11/15/2015.
 */
public class Route {

    private String fromId;
    private String toId;

    public Route() {
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }
}
