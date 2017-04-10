package com.mah.ex.lifelogdata;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private WebView wb;
    private Button saveBtn;
    private static final String CALLBACK_URL = "https://localhost";
    private final String CLIENT_ID ="0f3f8bd6-892c-4dbd-9776-49e498f29e84";
    private LinkedList<Dataset> dataCluster = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wb = (WebView) findViewById(R.id.webviewID);
        saveBtn = (Button) findViewById(R.id.saveBtnID);
        setupWebClient();
        wb.loadUrl("https://platform.lifelog.sonymobile.com/oauth/2/authorize?client_id="+CLIENT_ID+ "&scope=lifelog.profile.read+lifelog.activities.read+lifelog.locations.read");

    }

    public void setupWebClient(){
        wb.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if(url.contains("localhost")) {
                    wb.setVisibility(View.GONE);

                    Log.d("code",url);
                    Uri uri =  Uri.parse( url );;
                    final String codice=(uri.getQueryParameter("code").toString());
                    Log.d("codice", codice);
                    new ApiConnector(codice);
                    //          thread.execute(new GetUserToken(codice));
                    return true;
                }
                Log.d("Returing False", url);
                // return true; //Indicates WebView to NOT load the url;
                return false; //Allow WebView to load url
            }
        });
    }

    public void addDataset(Dataset dt){
        dataCluster.add(dt);
    }

}
