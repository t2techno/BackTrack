package com.example.tt.backtrack;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.tt.backtrack.data.ItemContract;
import com.example.tt.backtrack.data.ItemDbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* Populates a list view with all the name of saved items, as well as a thumbnail if image taken
 *
 * Clicking brings up dialog,giving the option of walking or driving directions, which then sends
 * intent with gps data to Google Maps app
 *
 * Long-clicking deletes the item info from database, the picture from storage, and reloads updated
 * View
 */
public class ItemList extends AppCompatActivity implements Dialogs.DeleteDialog.NoticeDialogListener,
                            Dialogs.NavigateDialog.NoticeDialogListener {
    private static final String TAG = "TAGGY";

    //custom view adapter
    ItemAdapter mItemAdapter;

    //List used within the custom adapter
    List<ItemType> items = new ArrayList<>();

    //Instance of Asynctask class for retrieving database off main thread
    FetchReadableList fetchRead = new FetchReadableList();

    //global cursor, used in setAdapter
    Cursor c;

    //global database, used in onLongClicked for resetting adapter after deletion
    SQLiteDatabase db;

    //global ItemType item to be deleted
    ItemType delete;

    //global ItemType to be navigated
    ItemType navigate_item;

    ///creating context variable
    Context mContext = this;

    //global fragment manager for DialogFrag
    FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getting the setup that normally comes onCreateView within a fragment
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.activity_item_list, null);

        mItemAdapter = new ItemAdapter(this, items);

        ListView listView = (ListView) rootView.findViewById(R.id.itemList);
        listView.setAdapter(mItemAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Log.v(TAG, "Click");
                navigate_item = mItemAdapter.getItem(position);
                Dialogs.NavigateDialog navigateDialog = Dialogs.NavigateDialog.newInstance(navigate_item.getUri().toString());
                navigateDialog.show(fm, "navigate");
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Log.v(TAG, "Long Click");
                delete = mItemAdapter.getItem(position);
                Dialogs.DeleteDialog deleteDialog = new Dialogs.DeleteDialog();
                deleteDialog.show(fm, "delete");
                return true;
            }
        });
        fetchRead.execute();
        setContentView(rootView);
    }

    //calls itemDelete function to delete the item from the database and resets adapter
    @Override
    public void onDeleteDialogPositiveClick(){
        //Log.v(TAG, "DeleteClickItemListClass");
        itemDelete(delete);
        setAdapter(db);
    }

    //Sets the direction mode for intent uri string and sends to navigate function
    @Override
    public void onNavigateDialogWalkClick(){
        String mode = "&mode=w";
        navigate(mode);
    }

    @Override
    public void onNavigateDialogDriveClick(){
        String mode = "&mode=d";
        navigate(mode);
    }

    //gets destination gps location from list item that was saved
    //creates intent with mode and the destination data, sends it to Google Maps
    public void navigate(String mode){
        double destLatitude = navigate_item.getLat();
        double destLongitude = navigate_item.getLong();

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(destLatitude) + ", "
                + String.valueOf(destLongitude) + mode);
        Log.v(TAG, String.valueOf(gmmIntentUri));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mContext.startActivity(mapIntent);
    }

    //opens a dbHelp, and uses that to open the item database
    //sets the global database equal to it and calls setAdapter with resulting database
    public class FetchReadableList extends AsyncTask<Void, Void, SQLiteDatabase> {

        @Override
        protected SQLiteDatabase doInBackground(Void... voids) {
            //Log.v(TAG, "AsynctaskItemList")
            ItemDbHelper mDbHelper = new ItemDbHelper(mContext);
            SQLiteDatabase results = mDbHelper.getReadableDatabase();
            return results;
        }

        @Override
        protected void onPostExecute(SQLiteDatabase results){
            db = results;
            setAdapter(results);
        }
    }

    //closes cursor if open
    //same with db database
    @Override
    public void onPause(){
        super.onPause();
        if(c != null){
            //Log.v(TAG, "Closing cursor");
            c.close();
        }

        if(db != null){
           // Log.v(TAG, "Closing database");
            db.close();
        }
    }

    //if item list or adapter are populated, clears them first
    //gets a cursor to the database, then walks through and takes each item
    //adds each item to adapter in turn
    public void setAdapter(SQLiteDatabase db){
        //Log.v(TAG, "setAdapter");
        final int NAME = 1;
        final int LAT = 2;
        final int LONG = 3;
        final int PATH = 4;

        //if we are resetting the lists, clear them first
        if(items != null){
            items.clear();
        }
        if(mItemAdapter != null){
            mItemAdapter.clear();
        }

        c = db.query(ItemContract.ItemLocation.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        if(c.getCount() > 0){
            c.moveToFirst();
            ItemType item = new ItemType(c.getString(NAME), c.getDouble(LAT), c.getDouble(LONG), c.getString(PATH));
            mItemAdapter.add(item);
            while (c.moveToNext()) {
                item = new ItemType(c.getString(NAME), c.getDouble(LAT), c.getDouble(LONG), c.getString(PATH));
                mItemAdapter.add(item);
            }
        }
    }

    //deletes the associated line from the database
    //as well as the associated image file
    public void itemDelete(ItemType del){
        //Log.v(TAG,"itemDelete");

        //delete item from database
        db.delete(ItemContract.ItemLocation.TABLE_NAME,
                ItemContract.ItemLocation.ITEM_NAME + "= ?",
                new String[]{ del.getName() });

        //delete the image file
        if(!del.getPath().equals("null")){
            File image = new File(del.getPath());
            boolean works = image.delete();
            if(!works){
                Log.e(TAG, "deletion - " + works);
            }
        }
    }
}
