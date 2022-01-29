package com.sales.numax.utility;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by bms0013 on 19/04/17.
 */

public class JsonHelper {
    public String getJsonString(String wcfUrl) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet request = new HttpGet();
        request.setURI(new URI(wcfUrl));
        HttpResponse response = httpClient.execute(request);
        return parseHttpResponse(response);
    }

    public String parseHttpResponse(HttpResponse response) throws Exception {
        String jsonString="";
        int status = response.getStatusLine().getStatusCode();
        if (status == 200) {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = bReader.readLine()) != null) {
                sb.append(line + NL);
            }
            jsonString = sb.toString();
            bReader.close();
        }
        return jsonString;

    }

    public JSONObject getJson(String wcfUrl, String jsonObjName) throws Exception {
        String jsonString=getJsonString(wcfUrl);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getJSONObject(jsonObjName);
    }

    public JSONArray getJsons(String wcfUrl, String jsonObjName) throws Exception {
        JSONObject jsonObject=new JSONObject(getJsonString(wcfUrl));
        return jsonObject.getJSONArray(jsonObjName);

    }

    public String doPost(String wcfUrl, JSONObject jsonObject) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post=new HttpPost();
        //HttpEntity httpEntity;
        StringEntity stringEntity=new StringEntity(jsonObject.toString());
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        //httpEntity=stringEntity;
        post.setEntity(stringEntity);
        post.setURI(new URI(wcfUrl));
        post.setHeader("Content-type", "application/json");
        if (!Global.IsHosted){
            post.addHeader("Host", "localhost");
        }
        HttpResponse response=httpClient.execute(post);
        return parseHttpResponse(response);
    }


    public String doPostMethod(String wcfUrl, JSONObject jsonObject) throws Exception {
        String result="ERROR";
        try {
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost post = new HttpPost();
            //HttpEntity httpEntity;
            StringEntity stringEntity = new StringEntity(jsonObject.toString());
            stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            //httpEntity=stringEntity;
            post.setEntity(stringEntity);
            post.setURI(new URI(wcfUrl));
            post.setHeader("Content-type", "application/json");
            if (!Global.IsHosted) {
                post.addHeader("Host", "localhost");
            }
            HttpResponse response = httpClient.execute(post);

            result = parseHttpResponse(response);


        }
        catch (Exception ex){

        }

        return result;
    }
    public String doGet(String wcfUrl) throws Exception {

        HttpParams httpParameters = new BasicHttpParams();
        int timeOutConnection = 50000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeOutConnection);
        int timeOutSocket = 50000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeOutSocket);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);

        HttpGet getHttp=new HttpGet();
        getHttp.setURI(new URI(wcfUrl));
        if (!Global.IsHosted){
            getHttp.addHeader("Host", "localhost");
        }
        getHttp.setHeader("Accept", "application/json");
        getHttp.setHeader("Content-type", "application/json");
        httpClient.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
        HttpResponse response=httpClient.execute(getHttp);

        return parseHttpResponse(response);
    }
    public String doGetWithImage(String wcfUrl) {
        String result="ERROR";
        HttpClient httpClient = null;
        HttpResponse response = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            int timeOutConnection = 50000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeOutConnection);
            int timeOutSocket = 50000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeOutSocket);
            httpClient = new DefaultHttpClient(httpParameters);

            HttpGet getHttp = new HttpGet();
            getHttp.setURI(new URI(wcfUrl));
            if (!Global.IsHosted) {
                getHttp.addHeader("Host", "localhost");
            }
            getHttp.setHeader("Accept", "application/json");
            getHttp.setHeader("Content-type", "application/json");
            httpClient.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
            response = httpClient.execute(getHttp);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return result;
    }
}
