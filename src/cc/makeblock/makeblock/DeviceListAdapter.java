package cc.makeblock.makeblock;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter {  
	  
    public static int PROGRESS;  
    private Context context;  
    private List<String> mData;  
    private int mResource;  
    private LayoutInflater mLayoutInflater;  
      
    public DeviceListAdapter(Context context, List<String> list, int resource){  
          
        this.context = context;  
        this.mData = list;  
        this.mResource = resource;  
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);  
    }  
      
    public void updateData(List<String> list){
        this.mData = list;  
    }
    public int getCount() {  
          
        return this.mData.size();  
    }  
  
    public Object getItem(int position) {  
          
        return this.mData.get(position);  
    }  
  
    public long getItemId(int position) {  
          
        return position;  
    }  
    public View getView(int position, View contentView, ViewGroup parent) {  
          
        contentView = this.mLayoutInflater.inflate(this.mResource, parent, false);    
  

        TextView titleView = (TextView) contentView.findViewById(R.id.device_item_title);  
        TextView descriptionView = (TextView) contentView.findViewById(R.id.device_item_description); 
        String msg[] = this.mData.get(position).toString().split(" ");
        titleView.setText(msg[0]); 
        if(msg.length>2){
        	descriptionView.setText(msg[1]+" "+msg[2]); 
        }else{
        	descriptionView.setText(msg[1]);
        }
        return contentView;  
    }  
}  