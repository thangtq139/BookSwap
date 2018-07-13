package vn.edu.hcmus.fit.cntn15.bookswap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ServerAPI {
    @GET("/book/get")
    Call<BookInfo[]> fetchBookData();

    @POST("/user/give")
    Call<BookURI> giveBook(@Body SentBook body);

    @POST("/user/take")
    Call<GetStatus> takeBook(@Body TakenBook body);

    @GET("/user/history")
    Call<UserBook[]> getHistory(@Body HistoryUser body);
}
