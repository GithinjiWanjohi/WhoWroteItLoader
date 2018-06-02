package com.example.gyth.whowroteitloader;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    // base URI for the Books API
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    // Parameter for the search string
    private static final String QUERY_PARAM = "q";
    // Parameter that limits search results
    private static final String MAX_RESULTS = "maxResults";
    // Parameter to filter by print type
    private static final String PRINT_TYPE = "printType";

    static String getBookInfo(String queryString){
            // helps to connect and read the incoming data
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

        // contain raw response form query and return it
        String bookJSONString = null;

        try{
            // Building up the query URI
            Uri builtURI = Uri.parse(BOOK_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, queryString)
                    .appendQueryParameter(MAX_RESULTS, "10")
                    .appendQueryParameter(PRINT_TYPE, "books")
                    .build();

            // Converting the URI to an URL
            URL requestURL = new URL(builtURI.toString());

            // Opening the URL connection and making the request
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Reading the response using an InputStream and a StringBuffer
            // then converting it to a string
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                // Nothing to be done
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null){
                /*Since it's JSON, adding a newline isn't necessary(won't affect
                 * parsing) but makes debugging a lot easier if you print out the
                 * completed buffer for debugging*/
                buffer.append(line + "\n");
            }
            if(buffer.length() == 0){
                // Stream empty. No point in parsing
                return null;
            }
            bookJSONString = buffer.toString();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(LOG_TAG, bookJSONString);
        return bookJSONString;
    }
}
