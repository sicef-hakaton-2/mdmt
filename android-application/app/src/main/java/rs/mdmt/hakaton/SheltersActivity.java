package rs.mdmt.hakaton;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SheltersActivity extends ListActivity
{

    public class RowAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final ArrayList<String> values;
        private final ArrayList<Integer> images;

        public RowAdapter(Context context, ArrayList<String> values,
                          ArrayList<Integer> images) {
            super(context, R.layout.row_layout, values);
            this.context = context;
            this.values = values;
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row_layout, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.shelter_name);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.shelter_image);
            textView.setText(values.get(position));

            imageView.setImageResource(images.get(position));

            return rowView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelters);

//        final ArrayList<Shelter> shelters = new ArrayList<>();

        final ArrayList<String> shelters = new ArrayList<>();
        final ArrayList<Integer> images = new ArrayList<>();

        Task.Data task = new Task.Data(new Task.Data.DataReadyCallback()
        {
            @Override
            public void dataReady()
            {
                JSONArray data = getData();

                try
                {
                    for (int i = 0; i < data.length(); ++i)
                    {
                        JSONObject obj = data.getJSONObject(i);
                        shelters.add(obj.getString("name"));

                        String type = obj.getString("type");

                        int id = Application.getContext().getResources().getIdentifier(type, "mipmap",
                                Application.getContext().getPackageName());

                        images.add(id);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

//                setListAdapter(new ArrayAdapter<String>(SheltersActivity.this, android.R.layout.simple_list_item_1, shelters));

                setListAdapter(new RowAdapter(SheltersActivity.this, shelters, images));
            }
        })
        {
            @Override
            public void executeImpl()
            {
                ServerConnection.getData();
            }
        };

        TaskManager.getInstance().execute(task);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_shelter);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(SheltersActivity.this, MapsActivity.class);
                i.putExtra("callType", true);

                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shelters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
//        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
    }
}
