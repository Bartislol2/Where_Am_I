package pl.gawryszewski.am_projekt;

public class PostModel {
    private String id;
    private String url;
    private double latitude;
    private double longitude;
    private String address;

    public PostModel(String id, String url, double latitude, double longitude, String address) {
        this.id = id;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
    //toString


    @Override
    public String toString() {
        return "Post id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", address='" + address ;
    }

    //getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
