package pl.gawryszewski.am_projekt;


public class User {
    private String name;
    private String id;
    private String profilePicPath;
    public User(String n, String id, String path)
    {
        this.name = n;
        this.id = id;
        this.profilePicPath = path;
    }
    public String getId(){
        return id;
    }
    public String getProfilePicPath()
    {
        return profilePicPath;
    }
    public String getName() {
        return name;
    }
}
