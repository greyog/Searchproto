package com.greyogproducts.greyog.searchproto;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import static com.greyogproducts.greyog.searchproto.MainActivity.TAG;

/**
 * Created by mac on 02/03/2018.
 */

class ListViewAdapter extends BaseAdapter{
    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
//    private List<AnimalNames> namesList = null;
    private MyResponseResult list;

    public ListViewAdapter(Context context, MyResponseResult responseResult) {
        mContext = context;
//        this.namesList = namesList;
        inflater = LayoutInflater.from(mContext);
        this.list = new MyResponseResult();
        if (responseResult != null) this.list.all.addAll(responseResult.all);
    }

    public void setNewData(MyResponseResult responseResult) {
        this.list.all.clear();
        if (responseResult != null) this.list.all.addAll(responseResult.all);
        notifyDataSetChanged();
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return (list.all == null)? 0 : list.all.size();
    }

    @Override
    public MyResponseResult.All getItem(int position) {
        return list.all.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final TextView hId, hName, hSymbol;
//        final HashMap<String, ViewHolder> holderMap;
//        boolean set = false;
        if (view == null) {
            view = inflater.inflate(R.layout.listview_item, null);
        }

        // Set the results into TextViews
//        Log.d(TAG, "getView: " + list.all.get(position).symbol);
        hId = view.findViewById(R.id.pairId);
        hName = view.findViewById(R.id.name);
        hSymbol = view.findViewById(R.id.symbol);
        hId.setText(String.valueOf(list.all.get(position).pairID));
        hName.setText(list.all.get(position).name);
        hSymbol.setText(list.all.get(position).symbol);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitHelper.getInstance().doPairRequest(hId.getText().toString());
            }
        });
        return view;
    }

    // Filter Class
//    public void filter(String charText) {
//        charText = charText.toLowerCase(Locale.getDefault());
//        namesList.clear();
//        if (charText.length() == 0) {
//            namesList.addAll(arraylist);
//        } else {
//            for (AnimalNames wp : arraylist) {
//                if (wp.getAnimalName().toLowerCase(Locale.getDefault()).contains(charText)) {
//                    namesList.add(wp);
//                }
//            }
//        }
//        notifyDataSetChanged();
//    }


}
