package vn.edu.hcmus.fit.cntn15.bookswap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HistoryActivity extends Activity {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://171.244.43.48:13097")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    ServerAPI mServer = retrofit.create(ServerAPI.class);

    ListView list;
    String[] Action = {
            "Borrow",
            "Drop"
    };

    String[] Title = {
            "Harry Potter",
            "LOTR"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Call<UserBook[]> cHistory = mServer.getHistory(new HistoryUser(user.getUid()));
        cHistory.enqueue(new Callback<UserBook[]>() {
            @Override
            public void onResponse(Call<UserBook[]> call, Response<UserBook[]> response) {
                UserBook[] books = response.body();
                String[] action = new String[books.length];
                String[] title = new String[books.length];
                CustomList listAdapter = new CustomList(HistoryActivity.this, title, action);
                list = findViewById(R.id.history_list);
                list.setAdapter(listAdapter);
            }

            @Override
            public void onFailure(Call<UserBook[]> call, Throwable t) {
                call.cancel();
                Toast.makeText(getApplicationContext(), "Failed to fetch data from server", Toast.LENGTH_SHORT).show();
            }
        });
        /*
        CustomList listAdapter = new CustomList(HistoryActivity.this, Title, Action);
        list = findViewById(R.id.history_list);
        list.setAdapter(listAdapter);
        */
    }
}
