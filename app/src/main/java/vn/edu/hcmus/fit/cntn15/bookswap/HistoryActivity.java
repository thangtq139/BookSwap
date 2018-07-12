package vn.edu.hcmus.fit.cntn15.bookswap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;


public class HistoryActivity extends Activity {
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

        CustomList listAdapter = new CustomList(HistoryActivity.this, Title, Action);
        list = findViewById(R.id.history_list);
        list.setAdapter(listAdapter);
    }
}
