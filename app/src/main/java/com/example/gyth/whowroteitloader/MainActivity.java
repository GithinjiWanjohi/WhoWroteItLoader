package com.example.gyth.whowroteitloader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
                implements LoaderManager.LoaderCallbacks<String>{

    private EditText mBookInput;
    private TextView mAuthorText;
    private TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing the views
        mBookInput = (EditText) findViewById(R.id.bookInput);
        mAuthorText = (TextView)findViewById(R.id.authorText);
        mTitleText = (TextView) findViewById(R.id.titleText);

        // Used to reconnect to the loader when configuration is changed
        if(getSupportLoaderManager().getLoader(0)!=null){
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new BookLoader(this, args.getString("queryString"));
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        try{
            // Convert the response into a JSON object
            JSONObject jsonObject = new JSONObject(data);
            // Get the JSON Array of book items
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            // Iterate through the results
            for(int i = 0; i<itemsArray.length(); i++){
                JSONObject book = itemsArray.getJSONObject(i);
                String title = null;
                String authors = null;
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                try{
                    title = volumeInfo.getString("title");
                    authors = volumeInfo.getString("authors");
                } catch (Exception e){
                    e.printStackTrace();
                }

                // Update TextViews if both title and author exist then return
                if (title != null && authors != null){
                    mTitleText.setText(title);
                    mAuthorText.setText(authors);
                    return;
                } else{
                    mTitleText.setText(R.string.no_result_found);
                    mAuthorText.setText("");
                }
            }
        }catch (Exception e){
            // If onPostExecute does not receive a proper JSON string,
            // update the UI to show failed results
            mTitleText.setText(R.string.no_result_found);
            mAuthorText.setText("");
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    public void searchBooks(View view) {
        String queryString = mBookInput.getText().toString();

        // hide keyboard once search button is clicked
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        // Initialize the connection variables
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        //Check if there is an active network connection to the internet
        if(networkInfo != null && networkInfo.isConnected() && queryString.length()!=0){
            // Call restartLoader() while passing a string
            // got from the EditText in the Bundle
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);
            getSupportLoaderManager().restartLoader(0, queryBundle, this);

            mTitleText.setText(R.string.loading);
            mAuthorText.setText("");
        } else{
            if(queryString.length() == 0){
                mTitleText.setText("");
                mAuthorText.setText(R.string.enter_search_term);
            }else{
                mTitleText.setText("");
                mAuthorText.setText(R.string.check_network_connection);
            }
        }
    }
}
