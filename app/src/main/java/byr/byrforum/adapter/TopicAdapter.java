package byr.byrforum.adapter;

import android.content.Context;
import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;

import byr.byrforum.R;
import byr.byrforum.javabean.OneFloorPic;

/**
 * Created by suiyue on 2016/1/27 0027.
 */
public class TopicAdapter extends BaseAdapter{

    private final String TAG = "TopicAdapter";

    private List<OneFloorPic> oneFloors;
    private LayoutInflater layoutInflater;
    private Context context;

    public TopicAdapter(Context context, List<OneFloorPic> oneFloors) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.oneFloors = oneFloors;
    }

    @Override
    public int getCount() {
        return oneFloors.size();
    }

    @Override
    public Object getItem(int position) {
        return oneFloors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        OneFloorPic oneFloor = (OneFloorPic) getItem(position);

        View view = layoutInflater.inflate(R.layout.one_item, null);

        TextView one_author = (TextView) view.findViewById(R.id.one_author);
        TextView one_floor = (TextView) view.findViewById(R.id.one_floor);
        TextView one_content = (TextView) view.findViewById(R.id.one_content);
        ViewGroup one_pictures = (ViewGroup) view.findViewById(R.id.pic);

        one_author.setText(oneFloor.getAuthor());
        one_floor.setText(oneFloor.getFloor());
        one_content.setText(oneFloor.getContent());

        ArrayList<Bitmap> bitmaps = oneFloor.getPics();

        for(int i=0; i<bitmaps.size(); i++) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            imageView.setImageBitmap(bitmaps.get(i));
            one_pictures.addView(imageView);

        }
        return view;
    }


    public List<OneFloorPic> getOneFloors() {
        return oneFloors;
    }

    public void setOneFloors(List<OneFloorPic> oneFloors) {
        this.oneFloors = oneFloors;
    }
}
