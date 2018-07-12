package vn.edu.hcmus.fit.cntn15.bookswap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class CustomList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] title;
    private final String[] action;

    public CustomList(Activity context,
                      String[] title, String[] action) {
        super(context, R.layout.row_item, action);
        this.context = context;
        this.title = title;
        this.action = action;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.row_item, null, true);
        TextView txtTitle = rowView.findViewById(R.id.Title);
        TextView txtAction = rowView.findViewById(R.id.Action);

        txtTitle.setText(title[position]);
        txtAction.setText(action[position]);
        return rowView;
    }

}
