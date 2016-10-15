package rs.mdmt.hakaton;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Tomasevic on 15.11.2015.
 */
public class DeparturesAdapter extends ArrayAdapter<Departure>
{
    Context mCon;
    List<Departure> departures;

    public DeparturesAdapter(Context context, List<Departure> resource)
    {
        super(context, -1, resource);
        mCon = context;
        departures = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) mCon.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.departure_layout, parent, false);

        TextView source = (TextView)row.findViewById(R.id.source);
        TextView destination = (TextView)row.findViewById(R.id.destination);
        TextView time = (TextView)row.findViewById(R.id.time);
        TextView seats = (TextView)row.findViewById(R.id.seats);

        source.setText(departures.get(position).getSource());
        destination.setText(departures.get(position).getDestination());
        time.setText(departures.get(position).getTime());
        seats.setText(String.valueOf(departures.get(position).getSeats()));

        return row;
    }
}
