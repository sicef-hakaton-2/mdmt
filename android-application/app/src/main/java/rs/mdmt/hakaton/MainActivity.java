package rs.mdmt.hakaton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener
{
    ImageView refugees, shelter;

    private SharedPreferences preferences;

    public static class UserType {
        public static final int REFUGEE = 0;
        public static final int HELPER = 1;

        private UserType() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refugees = (ImageView)findViewById(R.id.refugees);
        shelter = (ImageView)findViewById(R.id.shelter);
        refugees.setOnClickListener(this);
        shelter.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if(id == R.id.new_shelter)
        {
            Intent i = new Intent(this, NewShelterActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view)
    {
        final SharedPreferences prefs = Application.getContext()
                .getSharedPreferences(Application.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();

        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        switch(view.getId())
        {
            case R.id.refugees:
//                Intent refIntent = new Intent(MainActivity.this, MapsActivity.class);
//                if (!prefs.contains("mode"))
                    edit.putInt("mode", Application.NEED_HELP).commit();
//                startActivity(refIntent);
                break;
            case R.id.shelter:
//                Intent shelterIntent = new Intent(MainActivity.this, MapsActivity.class);
//                if (!prefs.contains("mode"))
                    edit.putInt("mode", Application.HELPER).commit();
//                startActivity(shelterIntent);
                break;
        }

        startActivity(intent);


    }
}
