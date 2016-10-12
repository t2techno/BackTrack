package com.example.tt.backtrack.data;

import android.provider.BaseColumns;

/*Defines table and column names for the Item database.*/
public class ItemContract {

    public ItemContract(){
    }
    public static final class ItemLocation implements BaseColumns {
        //location setting, latitude, longitude, city name
        public static final String TABLE_NAME = "item_list";

        //item name
        public static final String ITEM_NAME = "item_name";

        //latitude
        public static final String COLUMN_COORD_LAT = "coord_lat";

        //longitude
        public static final String COLUMN_COORD_LONG = "coord_long";

        //Image Uri
        public static final String COLUMN_IMAGE_PATH = "image_path";
    }
}
