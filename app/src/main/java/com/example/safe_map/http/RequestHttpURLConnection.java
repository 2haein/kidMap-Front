package com.example.safe_map.http;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class RequestHttpURLConnection {


    public static String sendREST(String sendUrl, String jsonValue) throws IllegalStateException {
        String inputLine = null;
        StringBuffer outResult = new StringBuffer();
        try{
            URL url = new URL(sendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true); conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            OutputStream os = conn.getOutputStream();
            os.write(jsonValue.getBytes("UTF-8"));
            os.flush(); // 리턴된 결과 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((inputLine = in.readLine()) != null) {
                outResult.append(inputLine);
            }
            conn.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
        return outResult.toString();
    }

    public static class NetworkAsyncTask extends AsyncTask<Void, Void, String> {

        private String url;
        private String jsonValue;

        public NetworkAsyncTask(String url, String jsonValue) {

            this.url = url;
            this.jsonValue = jsonValue;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            result = sendREST(url, jsonValue); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }


    }


}
