package vn.edu.hcmus.fit.cntn15.bookswap;

public class SentBook {
    public String book_name;
    public String book_info;
    public String user;
    public double Lat;
    public double Lng;

    SentBook(String iUser, String iBook_name, String iBook_info, double iLat, double iLng) {
        book_name = iBook_name;
        book_info = iBook_info;
        user = iUser;
        Lat = iLat;
        Lng = iLng;
    }
}
