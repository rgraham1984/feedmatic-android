package com.example.rgraham.test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    EditText urlText;
    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Set text field to myUrl on XML
         *
         * Set text view to main_textView on XML
         *
         * Enable scrolling for main_textView
         */
        urlText = (EditText) findViewById(R.id.myUrl);
        textView = (TextView) findViewById(R.id.main_textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }


    /**
     * When use clicks the search button, calls AsyncTask
     * <p/>
     * Before attempting to fetch the URL, makes sure that there is a network connection.
     * <p/>
     * If ActiveNetwork is present; download url string entered into urlText field
     * <p/>
     * If ActiveNetwork is not present; display error message
     */

    public void myClickHandler(View view) {
        String stringUrl = urlText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            urlText.setText(R.string.url_error_message);
        }

    }

    /**
     * Declare progressDialog Member Variable.
     * <p/>
     * Override the onPreExecute method. Define the progressDialog and provide context, string and
     * cancelable boolean as argument.
     * <p/>
     * Override doInBackground method. Set it to return output of downloadURL method. If it encounters
     * an error have it display error message
     * <p/>
     * Declare and define downloadURL method. It takes url passed by user as the argument. It then
     * uses HttpURLConnection to open and establish the connection. After this it passes the url as
     * a string of bytes and maximum length to the readIt method
     * <p/>
     * Declare and define the readIt method. It takes url as an InputStream and maximum length of
     * bytes accepted as the argument. Uses reader to read characters in url. This method then creates
     * a character array to act as a string buffer. This buffer limits the number of characters
     * displayed to 2040. It returns this to the downloadURL method
     * <p/>
     * Override the onPostExecute method to return the result to the UI and to dismiss the progress
     * dialog.
     */

    public class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Downloading... ", true);
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... urls) {

            String noTermEntered = "Unable to retrieve web page. URL may be invalid.";

            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return noTermEntered;
            }

        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            int len = 2048;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                is = conn.getInputStream();

                return readIt(is, len);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream, int len) throws IOException {
            Reader reader;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] stringBuffer = new char[len];
            reader.read(stringBuffer);
            return new String(stringBuffer);
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
            progressDialog.dismiss();
        }


    }


}




