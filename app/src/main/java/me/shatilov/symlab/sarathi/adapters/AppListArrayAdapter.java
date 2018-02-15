package me.shatilov.symlab.sarathi.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

import me.shatilov.symlab.sarathi.R;
import me.shatilov.symlab.sarathi.model.MiddleBoxModel;

/**
 * Created by Kirill on 18-Jan-18.
 */

public class AppListArrayAdapter extends ArrayAdapter<MiddleBoxModel> {
    private final Context context;
    private final List<MiddleBoxModel> values;
    private final int resource;

    public AppListArrayAdapter(@NonNull Context context, int resource, @NonNull List<MiddleBoxModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.values = objects;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View rowView, @NonNull ViewGroup parent) {
        if (rowView == null) {
            rowView = View.inflate(context, resource, null);
        }
        Switch _switch = rowView.findViewById(R.id.app_switch);

        _switch.setText(values.get(position).getAppName());
        _switch.setChecked(false);

        _switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            /*
             * TODO: Configure service according to selected tumblers.
             */
            Toast.makeText(context, "Sarathi is " + (isChecked ? "enabled" : "disabled") + " for " + values.get(position).getAppName(), Toast.LENGTH_LONG).show();
        });

        return rowView;
    }
}
