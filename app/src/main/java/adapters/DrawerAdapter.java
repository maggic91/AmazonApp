package adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.orma.amazonapp.R;

import java.util.ArrayList;

/**
 * Created by elvis.pobric on 26/05/2017.
 */

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {

    private Context context;

    public DrawerAdapter(Context context, ArrayList<DrawerItem> users) {
        super(context, 0, users);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DrawerItem row = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);

        ImageView img_left = (ImageView) convertView.findViewById(R.id.itemIcon);
        RobotoTextView txt_text = (RobotoTextView) convertView.findViewById(R.id.itemNameTxt);

        if(row.icon != null) {
            img_left.setImageResource(row.icon);

            if(row.drawable != null){
                img_left.setBackgroundResource(row.drawable);
            }
        }else{
            img_left.setVisibility(View.GONE);
        }

        if(row.icon == null && row.drawable == null){
            txt_text.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        txt_text.setText(row.text);

        //if(row.text_extra.trim().equals("")) {
          //  txt_extra.setVisibility(View.GONE);
        //}else{
          //  txt_extra.setVisibility(View.VISIBLE);
            //txt_extra.setText(row.text_extra.trim());
        //}

        return convertView;
    }
}