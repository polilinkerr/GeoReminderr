package com.yahoo.berniak.georeminderr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ReminderDetailsActivity extends Activity {


    public static final String EXTRA_EMPLOYEE_ID = "emp_id";
    public static final String EXTRA_MODE = "mode";

    public static final String MODE_NEW = "new";
    public static final String MODE_VIEW = "view";
    public static final String MODE_EDIT = "edit";

    private TextView titleField = null;
    private TextView descriptionField = null;
    private DataAccess dataAccess;
    private Button buttonTakePhoto = null;
    private ImageView viewImage = null;
    public static LatLng coordinate = null;
    public static String adress = "Ulica Pokątna :)";
    private TextView textCooridantes;
    private Button buttonToMap;
    private ImageView viewPhoto;

    private Uri photoUri;
    private final static int TAKE_PHOTO = 1;
    private final static String PHOTO_URI = "photoUri";



    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_reminder_details);
        titleField = (TextView) findViewById(R.id.fieldTitle);
        descriptionField = (TextView) findViewById(R.id.fieldDescription);
        textCooridantes = (TextView) findViewById(R.id.fieldCooridnates);
        buttonToMap = (Button) findViewById(R.id.byttonToMap);
        viewPhoto = (ImageView) findViewById(R.id.viewImage);
        viewPhoto.setImageResource(R.mipmap.ic_launcher);

        dataAccess = DataAccess.create(this);

        buttonTakePhoto = (Button) findViewById(R.id.buttonTakePhoto);
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoUri = getContentResolver().insert(EXTERNAL_CONTENT_URI, new ContentValues());
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        if (savedInstanceState != null){
            photoUri = (Uri) savedInstanceState.get(PHOTO_URI);

        }


        double[] tempLocation = getIntent().getDoubleArrayExtra("location");
        coordinate = new LatLng(tempLocation[0],tempLocation[1]);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) {
            mode = MODE_VIEW;

        }

        prepareMode();
        if (MODE_VIEW.equals(mode)) {
            loadData();
        }



    }

    protected void onStart() {
        super.onStart();
        if (!(coordinate == null)) {
            textCooridantes.setText("Location: " + HelpCalculation.round(coordinate.latitude,2) + " " + HelpCalculation.round(coordinate.longitude,2)+" adress:"+adress)      ;
        }
    }

    private void loadData() {
        Reminder e = dataAccess.getById(getIntent().getLongExtra(EXTRA_EMPLOYEE_ID, -1));
        if (e != null) {
            titleField.setText(e.getTitle());
            descriptionField.setText(e.getDescription());
            changeCoordinate(e.getLatitude(), e.getLongitude());
            textCooridantes.setText("Location: " + HelpCalculation.round(e.getLatitude(),4) + " " + HelpCalculation.round(e.getLongitude(),4)+e.getAdress());

            getActionBar().setTitle(e.getTitle());
        }
    }

    private void prepareMode() {
        boolean enabled = !(MODE_VIEW.equals(mode));

        titleField.setEnabled(enabled);
        descriptionField.setEnabled(enabled);
        buttonToMap.setEnabled(enabled);



        if (MODE_NEW.equals(mode)) {
            getActionBar().setTitle(R.string.new_employee);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.remind_details, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_edit).setVisible(MODE_VIEW.equals(mode));
        menu.findItem(R.id.action_delete).setVisible(MODE_VIEW.equals(mode));
        menu.findItem(R.id.action_commit).setVisible(!MODE_VIEW.equals(mode));
        menu.findItem(R.id.action_cancel).setVisible(!MODE_VIEW.equals(mode));

        return true;
    }

    ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                editClicked();
                break;
            case R.id.action_cancel:
                cancelClicked();
                break;
            case R.id.action_commit:
                commitClicked();
                break;
            case R.id.action_delete:
                deleteClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void commitClicked() {

        String CheckTitleField = titleField.getText().toString();
        if (CheckTitleField.matches("")){
            Toast.makeText(this, "You did not enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        Reminder e = new Reminder();
        e.setId(getIntent().getLongExtra(EXTRA_EMPLOYEE_ID, -1));
        e.setTitle(titleField.getText().toString());
        e.setDescription(descriptionField.getText().toString());
        e.setLongitude(coordinate.longitude);
        e.setLatitude(coordinate.latitude);
        ////
        e.setAdress(adress.toString());

        if (MODE_NEW.equals(mode)) {

            dataAccess.insert(e);
            setResult(MainActivity.DATA_RELOAD_NEEDED);
            finish();
        } else if (MODE_EDIT.equals(mode)) {
            dataAccess.update(e);
            setResult(MainActivity.DATA_RELOAD_NEEDED);
            mode = MODE_VIEW;

            loadData();
            invalidateOptionsMenu();
            prepareMode();
        }
    }

    private void cancelClicked() {
        if (MODE_NEW.equals(mode)) {
            setResult(MainActivity.SKIP_DATA_RELOAD);
            finish();
        } else if (MODE_EDIT.equals(mode)) {
            mode = MODE_VIEW;

            loadData();
            invalidateOptionsMenu();
            prepareMode();
        }
    }

    private void editClicked() {
        mode = MODE_EDIT;
        invalidateOptionsMenu();
        prepareMode();
    }

    private void deleteClicked() {
        //TODO: Implemente employee removal
        Reminder e = new Reminder();
        e.setId(getIntent().getLongExtra(EXTRA_EMPLOYEE_ID, -1));
        e.setTitle(titleField.getText().toString());
        e.setDescription(descriptionField.getText().toString());
        e.setLongitude(404);
        e.setLatitude(202);
        e.setAdress("ul Pokątna");

        dataAccess.delete(e.getId());
        setResult(MainActivity.DATA_RELOAD_NEEDED);
        loadData();
        invalidateOptionsMenu();
        prepareMode();
        finish();
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    private void changeCoordinate(double lat, double lng) {
        LatLng tmp = new LatLng(lat, lng);
        this.setCoordinate(tmp);
    }

    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != Activity.RESULT_OK || requestCode != TAKE_PHOTO ){
            return;
        }

        try{
            InputStream inputStream = getContentResolver().openInputStream(photoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            viewPhoto.setImageBitmap(bitmap);
        }catch (FileNotFoundException e){
            Log.e("ReminderDetailsActivity", "FileNotFound",e);
        }
    }
}
