package com.example.tt.backtrack;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/* Custom adapter that extends ArrayAdapter for use with my class ItemType
 * Gets the name of the item to be added, and if a picture was taken
 * uses picasso to load that image via uri
 */
public class ItemAdapter extends ArrayAdapter<ItemType>{
    private Context c;
    public ItemAdapter(Activity context, List<ItemType> items){
        super(context, 0, items);
        c = context;
    }


    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

        //what I'm working with
        ItemType item = getItem(position);
        String name = item.getName() .substring(0,item.getName().length()-1);
        TextView textView;
        ImageView imageView;

        //inflate base view if it has not yet happened
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_pic, parent, false);
        }

        //if a picture was taken(if not, path is set to null by default)
        //sets the imageView with Picasso
        if (!item.getPath().equals("null")) {
            imageView = (ImageView) convertView.findViewById(R.id.item_image_view);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Picasso.with(c)
                    .load(item.getUri())
                    .resize(90,50)
                    .into(imageView);
        }

        //sets the textview for the name of the item
        textView = (TextView) convertView.findViewById(R.id.list_item_textView);
        textView.setText(name);

        return convertView;
    }
}
