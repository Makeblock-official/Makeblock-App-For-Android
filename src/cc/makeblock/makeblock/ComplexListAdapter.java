package cc.makeblock.makeblock;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.transition.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ComplexListAdapter extends BaseAdapter {
	private List<ComplexItem> list = null;
    private Context context = null;
    private List<Map<String,Object>>listItems;
    private LayoutInflater mInflater = null;
    public MainActivity delegate;
    public ComplexListAdapter(Context context,List<ComplexItem> list) {
    	this.list = list;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return list.size();
    }
 
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {    
    	final ComplexItem item = list.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_complex, null);
        }
         
        TextView titleTxt1,titleTxt2,titleTxt3;
        TextView dateTxt1,dateTxt2,dateTxt3;
        ImageButton iv1,iv2,iv3;
        iv1 =(ImageButton)convertView.findViewById(R.id.imageButton_1);
        iv2 =(ImageButton)convertView.findViewById(R.id.imageButton_2);
        iv3 =(ImageButton)convertView.findViewById(R.id.imageButton_3);
        titleTxt1 = (TextView)convertView.findViewById(R.id.list_complex_title_1);
        titleTxt2 = (TextView)convertView.findViewById(R.id.list_complex_title_2);
        titleTxt3 = (TextView)convertView.findViewById(R.id.list_complex_title_3);
        titleTxt1.setText(item.get(MainActivity.ITEM_TITLE_1));
        titleTxt2.setText(item.get(MainActivity.ITEM_TITLE_2));
        titleTxt3.setText(item.get(MainActivity.ITEM_TITLE_3));

        if(item.getInteger(MainActivity.ITEM_IMAGE_1)!=-1){
        	iv1.setImageResource(item.getInteger(MainActivity.ITEM_IMAGE_1));
        }
        if(item.getInteger(MainActivity.ITEM_IMAGE_2)!=-1){
        	iv2.setImageResource(item.getInteger(MainActivity.ITEM_IMAGE_2));
        }
        if(item.getInteger(MainActivity.ITEM_IMAGE_3)!=-1){
        	iv3.setImageResource(item.getInteger(MainActivity.ITEM_IMAGE_3));
        	iv3.setVisibility(View.VISIBLE);
        }else{
        	iv3.setVisibility(View.INVISIBLE);
        }
        OnClickListener l1 = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				delegate.openExample(item.getInteger(MainActivity.ITEM_INDEX_1));
			}
		};
        iv1.setOnClickListener(l1);
        OnClickListener l2 = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				delegate.openExample(item.getInteger(MainActivity.ITEM_INDEX_2));
			}
		};
        iv2.setOnClickListener(l2);
        OnClickListener l3 = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				delegate.openExample(item.getInteger(MainActivity.ITEM_INDEX_3));
			}
		};
        iv3.setOnClickListener(l3);
        return convertView;
         
        
         
    }
 
}