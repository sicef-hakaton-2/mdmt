package rs.mdmt.hakaton;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;

import static android.graphics.Color.BLUE;

public class NewShelterActivity extends Activity implements View.OnClickListener
{
    private static final int NEW = 0;
    private static final int EDIT = 1;
    private static final int VIEW  = 2;
    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private Bitmap photo;
    private ImageView imageView;
    private int type;
    private String shelterId = null;
    private double latitude;
    private double longitude;
    private JSONObject jsonObject;
    private String userId;
    private int shelterType;
    ImageView hospital;
    ImageView camp;
    ImageView house;
    Dialog dialog;

    private Task.Data serverTask = new Task.Data(new Task.Data.DataReadyCallback()
    {
        @Override
        public void dataReady()
        {
            try
            {
                initShelter(getData());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    })
    {
        @Override
        public void executeImpl()
        {

            ServerConnection.getShelter(shelterId);
        }
    };
    private Task uploadTask = new Task()
    {
        @Override
        public void execute()
        {
            if(type == NEW)
                ServerConnection.uploadShelter(jsonObject);
            else
                ServerConnection.updateShelter(jsonObject);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_shelter);

        hospital = (ImageView)findViewById(R.id.hospital_shelter);
        camp = (ImageView)findViewById(R.id.camp_shelter);
        house = (ImageView)findViewById(R.id.house_shelter);

        FloatingActionButton departure = (FloatingActionButton) findViewById(R.id.departure);
        departure.setOnClickListener(this);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 2);
        if (type != NEW)
        {
            shelterId = intent.getStringExtra("shelterId");
            TaskManager.getInstance().execute(serverTask);

        }
        if(type == EDIT || type == NEW)
        {
            FloatingActionButton addNewResourceButton = (FloatingActionButton) findViewById(R.id.add_new_resource);
            addNewResourceButton.setOnClickListener(this);
//            FloatingActionButton imageButton = (FloatingActionButton)findViewById(R.id.get_image);
//            imageButton.setOnClickListener(this);
            addNewResourceButton.setVisibility(View.VISIBLE);
//            imageButton.setVisibility(View.VISIBLE);
            FloatingActionButton uploadShelterBtn = (FloatingActionButton)findViewById(R.id.upload_shelter_button);
            uploadShelterBtn.setOnClickListener(this);
            uploadShelterBtn.setVisibility(View.VISIBLE);

            hospital.setOnClickListener(this);
            camp.setOnClickListener(this);
            house.setOnClickListener(this);

            if(type == NEW)
            {
                final TextView textViewShelterName = (TextView) findViewById(R.id.textview_shelter_name);
                final EditText editTextShelterName = (EditText) findViewById(R.id.editext_shelter_name);
                editTextShelterName.setVisibility(View.VISIBLE);
                textViewShelterName.setVisibility(View.GONE);

                editTextShelterName.setOnEditorActionListener(new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
                    {
                        if (i == EditorInfo.IME_ACTION_DONE)
                        {
                            textViewShelterName.setText(editTextShelterName.getText().toString());
                            textViewShelterName.setVisibility(View.VISIBLE);
                            editTextShelterName.setVisibility(View.GONE);
                        }
                        return false;
                    }
                });
                textViewShelterName.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        editTextShelterName.setText(textViewShelterName.getText().toString());
                        editTextShelterName.setVisibility(View.VISIBLE);
                        textViewShelterName.setVisibility(View.GONE);
                    }
                });
                latitude = intent.getDoubleExtra("latitude",0);
                longitude = intent.getDoubleExtra("longitude", 0);
                userId = intent.getStringExtra("userId");
                shelterType = 2;
                typeImageClick();
            }
        }

//        imageView = (ImageView)findViewById(R.id.image_view);


    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        switch(id)
        {
            case R.id.add_new_resource:
                handleAdding(null,0);
                break;
//            case R.id.get_image:
//                getImage();
//                break;
            case R.id.upload_shelter_button:
                sendData();
                startActivity(new Intent(this, MapsActivity.class));
                break;
            case R.id.hospital_shelter:
                shelterType = 1;
                typeImageClick();
                break;
            case R.id.camp_shelter:
                shelterType = 2;
                typeImageClick();
                break;
            case R.id.house_shelter:
                shelterType = 3;
                typeImageClick();
                break;
            case R.id.departure:
//                if(dialog != null)
//                {
                    Task.Data serverTaskRoutes = new Task.Data(new Task.Data.DataReadyCallback()
                    {
                        @Override
                        public void dataReady()
                        {
                            populateDepartures(getData());

                            if (dialog != null)
                                dialog.show();
                            else
                                Toast.makeText(NewShelterActivity.this, "No routes!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    {
                        @Override
                        public void executeImpl()
                        {
                            ServerConnection.getRoutes();

                        }
                    };
                    TaskManager.getInstance().execute(serverTaskRoutes);
//                }
//                else
                break;

        }
    }
    private void getImage()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose");
        builder.setMessage("Where from do you want to take your image?");
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener()
        {




            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            photo = (Bitmap) data.getExtras().get("data");
        }
        else if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();

            String selectedImagePath = cursor.getString(columnIndex);

            photo = BitmapFactory.decodeFile(selectedImagePath);
        }
      //  photo = Bitmap.createScaledBitmap(photo, photo.getWidth() / 10, photo.getHeight() / 10, true);
        imageView.setImageBitmap(photo);
    }
    private void initShelter(JSONArray array)
    {
        try
        {
            ((TextView)findViewById(R.id.textview_shelter_name)).setText(array.getJSONObject(0).getString("name"));
            String tmp = array.getJSONObject(0).getString("type");
            switch (tmp){
                case "hospital":
                    shelterType = 1;
                    break;
                case "camp":
                    shelterType = 2;
                    break;
                case "house":
                    shelterType = 3;
                    break;
            }
            typeImageClick();
            JSONArray resources = array.getJSONObject(0).getJSONArray("resources");
            for (int i = 0; i < resources.length(); ++i)
            {

                JSONObject obj = resources.getJSONObject(i);
                String name = obj.keys().next();
                handleAdding(name, obj.getInt(name));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    private void handleAdding(String name, int amount)
    {
        LinearLayout layout = (LinearLayout)findViewById(R.id.resource_layout);
        final View view = getLayoutInflater().inflate(R.layout.resource_layout, null);
        final EditText editTextName = (EditText)view.findViewById(R.id.edit_text_name);
        final TextView textViewName = (TextView)view.findViewById(R.id.text_view_name);
        final TextView textViewAmount = ((TextView)view.findViewById(R.id.text_view_amount));
        SeekBar seekBar = (SeekBar)view.findViewById(R.id.seek_bar_amount);

        if(VIEW == type)
            seekBar.setClickable(false);

        if(name != null)
        {
            textViewName.setText(name);
            seekBar.setProgress(amount);
            textViewAmount.setText(String.valueOf(amount));

        }
        else
        {
            textViewName.setVisibility(View.GONE);
            editTextName.setVisibility(View.VISIBLE);
        }
        editTextName.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(!b){
                    textViewName.setText(editTextName.getText().toString());
                    textViewName.setVisibility(View.VISIBLE);
                    editTextName.setVisibility(View.GONE);
                }
            }
        });
        editTextName.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_DONE)
                    editTextName.clearFocus();
                return false;
            }
        });
        if (type == NEW || type == EDIT)
            textViewName.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    editTextName.setText(textViewName.getText().toString());
                    editTextName.setVisibility(View.VISIBLE);
                    textViewName.setVisibility(View.GONE);
                }
            });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                if(i == seekBar.getMax())
                    seekBar.setMax(seekBar.getMax() + 100);
                textViewAmount.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        layout.addView(view);
    }
    private void sendData()
    {
        try
        {
             jsonObject = new JSONObject();
            if(type == EDIT)
                jsonObject.put("_id",shelterId);
             jsonObject.put("name",((TextView)findViewById(R.id.textview_shelter_name)).getText().toString() );
            String tmp = "";
            switch (shelterType)
            {
                case 1:
                    tmp = "hospital";
                    break;
                case 2:
                    tmp = "camp";
                    break;
                case 3:
                    tmp = "house";
                    break;
            }

            jsonObject.put("type",tmp );
            jsonObject.put("userId",userId);
             jsonObject.put("latitude",latitude);
             jsonObject.put("longitude",longitude);
             JSONArray dataArray = new JSONArray();
            LinearLayout layout = (LinearLayout)findViewById(R.id.resource_layout);

            for(int i = 0; i < layout.getChildCount(); i++)
            {
                 String resName = ((TextView)((LinearLayout) layout.getChildAt(i)).getChildAt(1)).getText().toString();
                 int resAmount = Integer.parseInt(((TextView) ((LinearLayout) layout.getChildAt(i)).getChildAt(3)).getText().toString());
                 JSONObject obj = new JSONObject();
                 obj.put(resName, resAmount);
                 dataArray.put(obj);
            }
            jsonObject.put("resources",dataArray);

            Log.d("jsonobj", jsonObject.toString());
            TaskManager.getInstance().execute(uploadTask);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    private void typeImageClick()
    {
        switch (shelterType)
        {
            case 1:
//                hospital.setBackgroundColor(Color.parseColor("#4A8CF7"));
//                camp.setBackgroundColor(Color.WHITE);
//                house.setBackgroundColor(Color.WHITE);
                hospital.setImageResource(R.drawable.myhospital);
                camp.setImageResource(R.drawable.camp);
                house.setImageResource(R.drawable.house);
                break;

            case 2:
                hospital.setImageResource(R.drawable.hospital);
                camp.setImageResource(R.drawable.mycamp);
                house.setImageResource(R.drawable.house);
//                hospital.setBackgroundColor(Color.WHITE);
//                camp.setBackgroundColor(Color.parseColor("#4A8CF7"));
//                house.setBackgroundColor(Color.WHITE);
                break;
            case 3:
                hospital.setImageResource(R.drawable.hospital);
                camp.setImageResource(R.drawable.camp);
                house.setImageResource(R.drawable.myhouse);
//                hospital.setBackgroundColor(Color.WHITE);
//                camp.setBackgroundColor(Color.WHITE);
//                house.setBackgroundColor(Color.parseColor("#4A8CF7"));
                break;
        }
    }

    public void populateDepartures(JSONArray array)
    {
        ArrayList<Departure> deps = new ArrayList<>();
        Departure d = new Departure();

        for(int i=0; i<array.length(); i+=2)
        {
            try
            {
                JSONObject obj1 = array.getJSONObject(i);
                JSONObject obj2 = array.getJSONObject(i+1);

                JSONObject shelter1 = obj1.getJSONObject("shelter");
                JSONObject shelter2 = obj2.getJSONObject("shelter");

                String _id = shelter1.getString("_id");
                _id = _id.substring(10, _id.length() - 2);
                if(_id.equals(shelterId))
                {
                    d.setSource(shelter1.getString("name"));
                    d.setDestination(shelter2.getString("name"));
                    d.setTime(obj1.getString("departure"));
                    d.setSeats(obj1.getInt("seats"));

                    deps.add(d);
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        if(!deps.isEmpty())
        {
            dialog = new Dialog(NewShelterActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.departures_dialog_layout);

            ListView listView = (ListView) dialog.findViewById(R.id.departures);

            DeparturesAdapter adapter = new DeparturesAdapter(this, deps);
            listView.setAdapter(adapter);
        }
    }

}
