package com.example.rgraham.test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    EditText urlText;
    TextView myTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlText = (EditText) findViewById(R.id.myUrl);
        myTextView = (TextView) findViewById(R.id.main_textView);

    }

    /* This method stores the URL entered by the user in a variable and checks for a network
    * connection. Passes the URL to an AsycTask to download if network detected. Displays an error
    * message if network is not detected.
    */

    public void myClickHandler(View view) {
        String stringUrl = urlText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadXmlTask().execute(stringUrl);
        } else {
            myTextView.setText(R.string.url_error_message);
        }

    }

    /* Implementation of AsyncTask used to manage the downloading of the URL passed from the
    * myClickHandler method. Displays a spinner while downloading page and displays whats returned
    * by the loadXmlFromNetwork method in a webview. Displays errors for Xml and Network exceptions
    */

    public class DownloadXmlTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Downloading... ", true);
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            } catch (IOException e) {
                return getResources().getString(R.string.url_error_message);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setContentView(R.layout.activity_main);
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadData(result, "text/html", null);
        }

    }


    /**
     * Takes the string passed to it by the AysncTask and uses the downloadUrl method to download
     * web content. It then begins the process of parsing and combines it with HTML markup. It
     * returns an HTML string
     */

    public String loadXmlFromNetwork(String urlString) throws IOException, XmlPullParserException {
        InputStream stream = null;

        XmlParser xmlParser = new XmlParser();
        List<XmlParser.Item> items = null;
        String title = null;
        String url = null;
        String description = null;

        StringBuilder htmlString = new StringBuilder();

        try {
            stream = downloadUrl(urlString);
            items = xmlParser.parse(stream);


        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        // This loops through the items list and combines each item with HTML markup.
        // Each item is displayed in the UI as a link that
        // optionally includes a text description.

        for (XmlParser.Item item : items) {
            htmlString.append("<p><a href=` ");
            htmlString.append(item.link);
            htmlString.append("`>");
            htmlString.append(item.title);
            htmlString.append("</a></p>");
            htmlString.append(item.description);

        }
        return htmlString.toString();
    }

    // This method takes the URL passed to it by loadXmlFromNetwork, makes the http network connection
    // and returns an input stream.

    public InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(30000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    /**
     * This class parses the input stream and passes them to the readFeed method. It then takes
     * whats returned by the readFeed method and places it in list containing Items
     */

    public class XmlParser {
        private String ns = null;

        public List<Item> parse(InputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readFeed(parser);
            } finally {
                in.close();
            }
        }

        /**
         * This method that reads through the different nodes on the XML file. It first
         * looks for a END_TAG. If there is no END_TAG then it looks for a START_TAG and begins the
         * parsing process. When it finds an item it passes it to the readItem method. It takes
         * whats returned by the readItem method and passes it back to the parse list.
         */

        public List<Item> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
            List<Item> items = new ArrayList<Item>();

            parser.require(XmlPullParser.START_TAG, ns, "feed");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the item tag
                if (name.equals("item")) {
                    items.add(readItem(parser));
                } else {
                    skip(parser);
                }
            }
            return items;
        }

        // This class represents a single item (post) in the xml feed.
        public class Item {
            private String title;
            private String link;
            private String description;

            private Item(String title, String link, String description) {
                this.title = title;
                this.link = link;
                this.description = description;
            }
        }

        // Parses the contents of an item. If it encounters a title, description, or link tag and
        // hands them off to their respective methods for processing. Otherwise it skips the tag
        public Item readItem(XmlPullParser parser) throws XmlPullParserException, IOException {

            parser.require(XmlPullParser.START_TAG, ns, "item");
            String title = null;
            String link = null;
            String description = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("title")) {
                    title = readTitle(parser);
                } else if (name.equals("link")) {
                    link = readLink(parser);
                } else if (name.equals("description")) {
                    description = readDescription(parser);
                } else {
                    skip(parser);
                }

            }
            return new Item(title, link, description);

        }

        // Processes title tags in the feed
        public String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "title");
            String title = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "title");
            return title;
        }

        // Processes link tags in the feed
        public String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "link");
            String link = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "link");
            return link;
        }

        // Processes description tags in the feed
        public String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "description");
            String description = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "description");
            return description;
        }

        // For the tags title and description, extracts their text values
        public String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        // Skips tags the parser isn't interested in. Uses depth to handle nested tags.
        public void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }


}
