package com.mah.ex.lifelogdata;

/**
 * Created by Girondins on 2017-04-08.
 */

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;

/**
 * Created by Girondins on 30/01/17.
 */
public class ApiConnector {
    private ExecuteThread thread;
    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    String TAG = ((Object) this).getClass().getSimpleName();
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
    private Long refresh_expires;
    private String headerValue;
    private volatile boolean hasHeader = false;
    private volatile boolean isForUpload = true;
    private final String CLIENT_ID ="0f3f8bd6-892c-4dbd-9776-49e498f29e84";
    private final String CLIENT_SECRET = "8CVAsHt_Ylbu4_cKJIhZrDdSOiM";
    private String authCode;
    private double bmr;




    public ApiConnector(String authCode){
        thread = new ExecuteThread();
        thread.start();
        this.authCode = authCode;
        authorize(authCode);
    }

    /**
     * Making service call
     *
     * @url - url to make request
     * @method - http request method
     */
    private String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }

    /**
     * Making service call
     *
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     */
    private String makeServiceCall(String url, int method,
                                   List<NameValuePair> params) {
        try {
            // http client
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 2000;
            //  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 2000;
            //   HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params

                if(hasHeader == true){
                    httpPost.setHeader("Authorization", headerValue);
                    hasHeader = false;
                    Log.d("Authen",headerValue);
                }
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);


            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                Log.e("Request: ", "> " + url);
                HttpGet httpGet = new HttpGet(url);

                if(hasHeader == true){
                    httpGet.setHeader("Authorization", headerValue);
                    hasHeader = false;
                    Log.d("Authen",headerValue);
                }

                httpResponse = httpClient.execute(httpGet);

            }
            if (httpResponse != null) {
                httpEntity = httpResponse.getEntity();
            } else {
                Log.e(TAG, "httpResponse is null");
            }
            response = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;

    }

    public void extractAuth(String access){
        try {
            accessToken = (String)((JSONObject) JSONValue.parse(access)).get("access_token");
            tokenType = (String) ((JSONObject) JSONValue.parse(access)).get("token_type");
            expiresIn = (Long)((JSONObject) JSONValue.parse(access)).get("expires_in");
            refreshToken = (String)((JSONObject) JSONValue.parse(access)).get("refresh_token");
            refresh_expires = (Long)((JSONObject) JSONValue.parse(access)).get("refresh_token_expires_in");
            Log.d("extracting","Access " + accessToken);
            Log.d("extracting","Type " + tokenType);
            Log.d("extracting","Expires " + expiresIn);
            Log.d("extracting","Refresh Token " + refreshToken);
            Log.d("extracting","Refresh Expires " + refresh_expires);
            headerValue = "Bearer " + accessToken;
            hasHeader = true;
            getPersonal();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void extractRenew(String access){
        try {
            accessToken = (String)((JSONObject) JSONValue.parse(access)).get("access_token");
            tokenType = (String) ((JSONObject) JSONValue.parse(access)).get("token_type");
            expiresIn = (Long)((JSONObject) JSONValue.parse(access)).get("expires_in");
            refreshToken = (String)((JSONObject) JSONValue.parse(access)).get("refresh_token");
            refresh_expires = (Long)((JSONObject) JSONValue.parse(access)).get("refresh_token_expires_in");
            Log.d("extracting","Access " + accessToken);
            Log.d("extracting","Type " + tokenType);
            Log.d("extracting","Expires " + expiresIn);
            Log.d("extracting","Refresh Token " + refreshToken);
            Log.d("extracting","Refresh Expires " + refresh_expires);
            headerValue = "Bearer " + accessToken;
            hasHeader = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivites();
    }


    public void extract(String activityInfo){
        int stepsCount = 0, communicationCount = 0, browsingCount = 0;
        Double aeeCount = 0.00;
        String type;


        try {
            JSONArray extractResult = (JSONArray) ((JSONObject) JSONValue.parse(activityInfo)).get("result");
            Log.d("TESTING ACTIVITY ",extractResult.size() + "");
            for(int i = 0; i<extractResult.size(); i++) {
                JSONObject results = (JSONObject) extractResult.get(i);
                type = (String) results.get("type");

                switch (type){
                    case "physical":
                        JSONObject details = (JSONObject) results.get("details");
                        Log.d("Details ", details.toString());
                        JSONArray steps = (JSONArray) details.get("steps");
                        JSONArray aee = (JSONArray) details.get("aee");
                        if(steps != null) {
                            for (int j = 0; j < steps.size(); j++) {
                                stepsCount += (Long) steps.get(j);
                            }
                        }
                        if(aee != null) {
                            for (int y = 0; y < aee.size(); y++) {
                                aeeCount += (Double) aee.get(y);
                            }
                        }

                        break;

                    case "application":
                        int totalSeconds;
                        String startTime = (String) results.get("startTime");
                        String endTime = (String) results.get("endTime");
                        String subType = (String) results.get("subtype");
                        String subStart = startTime.substring(11,19);
                        String subEnd = endTime.substring(11,19);
                        String splitStart[] = subStart.split(":");
                        String splitEnd[] = subEnd.split(":");
                        int startSeconds = convertToSeconds(Integer.parseInt(splitStart[0]),Integer.parseInt(splitStart[1]),Integer.parseInt(splitStart[2]));
                        int endSeconds = convertToSeconds(Integer.parseInt(splitEnd[0]),Integer.parseInt(splitEnd[1]),Integer.parseInt(splitEnd[2]));

                        if(endSeconds<startSeconds){
                        totalSeconds = (endSeconds + (24*3600))-startSeconds;
                        }else
                        totalSeconds = endSeconds-startSeconds;

                        Log.d("END: " + endSeconds,"Start: " + startSeconds );

                        switch(subType){
                            case "communication":
                                communicationCount += totalSeconds;
                                Log.d("COMMUNCIATION: " , communicationCount +" ");
                                break;

                            case "browsing":
                                browsingCount += totalSeconds;
                                break;
                        }

                        Log.d("Testing Application", subType.toString() + " start: " + subStart + " end: " + subEnd + " Seconds Spent: " + totalSeconds);
                        break;
                }
                Log.d("Aee is "  , "" + aeeCount);




            }

            Dataset dt = new Dataset(stepsCount,communicationCount,browsingCount,countDayCals(aeeCount));

            System.out.println("Steps: " + dt.getSteps() + "\n Com: " + dt.getCom() + "\n Brow: " + dt.getBros() + "\n Calories: " + dt.getCals());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public int countDayCals(double aee){
        int cals;
        cals = (int) (aee + (bmr*24));
        return cals;

    }

    public void authorize(String accessCode){
        thread.execute(new Authenticate());
    }

    public void getActivites(){
        thread.execute(new GetActivites());
    }

    public void getPersonal()
    {
        thread.execute(new GetPersonalInfo());
    }


    public int convertToSeconds(int hour, int minute, int seconds){
        int toSeconds;
        toSeconds = (hour*3600) + (minute*60) + seconds;

        return toSeconds;
    }



    public void renewToken(){
        Log.d(" RENEW UPDATE", isForUpload + "");
        if(isForUpload == false){
            Thread t = new Thread(new RenewToken());
            t.start();
            Log.d(" RENEW UPDATE", isForUpload + "");
        }else {
            thread.execute(new RenewToken());
            Log.d(" FAAALSE", isForUpload + "");
        }
    }



    private class Authenticate implements Runnable {

        @Override
        public void run() {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("client_id",CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
            nameValuePairs.add(new BasicNameValuePair("grant_type","authorization_code"));
            nameValuePairs.add(new BasicNameValuePair("code",authCode));
            String res = makeServiceCall("https://platform.lifelog.sonymobile.com/oauth/2/token",2,nameValuePairs);
            //       Log.d("UserToken Success",res);
            extractAuth(res);
        }
    }

    private class RenewToken implements Runnable {

        @Override
        public void run() {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("client_id",CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
            nameValuePairs.add(new BasicNameValuePair("grant_type","refresh_token"));
            nameValuePairs.add(new BasicNameValuePair("refresh_token",refreshToken));

            Log.d("RE REFF", refreshToken + " HEADER  " + headerValue + " is Ture" + hasHeader);
            String res = makeServiceCall("https://platform.lifelog.sonymobile.com/oauth/2/refresh_token",2,nameValuePairs);
            Log.d("RefreshToken Success" + isForUpload,res);
            extractRenew(res);
        }
    }

    public void extractPersonal(String personalInfo){
        String userName,birthday;
        Double height,weight,bmr;
        try {
            JSONArray extractResult = (JSONArray) ((JSONObject) JSONValue.parse(personalInfo)).get("result");
            JSONObject results = (JSONObject) extractResult.get(0);
            Log.d("TESTING ",results.toString());
            userName = (String) results.get("username");
            birthday = (String) results.get("birthday");
            height = (Double) results.get("height");
            weight = (Double) results.get("weight");
            bmr = (Double) results.get("bmr");

            Log.d("Personal Extract", "Username: " + userName +
                    "\n Birthday: " + birthday +
                    "\n Height: " + height +
                    "\n Weight: " + weight +
                    "\n Bmr: " + bmr);

            this.bmr = bmr;

        } catch (Exception e) {
            e.printStackTrace();
        }
        renewToken();
    }


    private class GetActivites implements Runnable {


        @Override
        public void run() {
            DateTimeZone theTimeZone = DateTimeZone.forID( DateTimeZone.forTimeZone(TimeZone.getDefault()).toString());
            DateTime now = DateTime.now( theTimeZone );
            String timeZ;
            timeZ = now.toString().substring(23);
            timeZ = timeZ.replace(":","");
            timeZ = timeZ.replace("+","%2B");

          //  String res = makeServiceCall("https://platform.lifelog.sonymobile.com/v1/users/me/activities?start_time="+ this.yesterdays +"T00:00:01.000"+ timeZ +"&end_time="+ this.today +"T00:00:01.000" + timeZ,1);
            String res = makeServiceCall("https://platform.lifelog.sonymobile.com/v1/users/me/activities?start_time="+ "2017-04-01" +"T00:00:01.000"+ timeZ + "&end_time=2017-04-09T23:59:59.000" + timeZ ,1);
          //  String res = makeServiceCall("https://platform.lifelog.sonymobile.com/v1/users/me/activities?start_time="+ "2017-04-09" +"T00:00:01.000"+ timeZ,1);
            Log.d("Act", res);
            Log.d("EXtract ", " JEPP");
            extract(res);
            //  renewToken();
        }
    }

    private class GetPersonalInfo implements Runnable {

        @Override
        public void run() {
            String res = makeServiceCall("https://platform.lifelog.sonymobile.com/v1/users/me/",1);
            Log.d("Me", res);
            extractPersonal(res);
            //  renewToken();
        }
    }



}
