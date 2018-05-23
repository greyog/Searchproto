package com.greyogproducts.greyog.searchproto;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import static com.greyogproducts.greyog.searchproto.MainActivity.TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements SearchView.OnQueryTextListener, RetrofitHelper.OnResponseListener{
//    private String[] animalNameList;
    private ListView list;
    private SearchView editsearch;
//    ArrayList<AnimalNames> arraylist = new ArrayList<AnimalNames>();
    private ListViewAdapter adapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Generate sample data

//        animalNameList = new String[]{"Lion", "Tiger", "Dog",
//                "Cat", "Tortoise", "Rat", "Elephant", "Fox",
//                "Cow","Donkey","Monkey"};

        // Locate the ListView in listview_main.xml
        list = (ListView) view.findViewById(R.id.listView);

//        for (int i = 0; i < animalNameList.length; i++) {
//            AnimalNames animalNames = new AnimalNames(animalNameList[i]);
//            // Binds all strings into an array
//            arraylist.add(animalNames);
//        }

        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(this.getContext(), null);

        // Binds the Adapter to the ListView
        list.setAdapter(adapter);

        // Locate the EditText in listview_main.xml
        editsearch = (SearchView) view.findViewById(R.id.mySearchView);
        editsearch.setOnQueryTextListener(this);

        RetrofitHelper.getInstance().setOnResponseListener(this);


        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        RetrofitHelper.getInstance().doRequest(text);
        return false;
    }


    @Override
    public void onResponse(MyResponseResult responseResult) {
        adapter.setNewData(responseResult);
    }

    @Override
    public void onResponseTechData(String raw) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle("Title here");

        WebView wv = new WebView(this.getContext());
        wv.loadData(raw, null, null);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(wv);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
