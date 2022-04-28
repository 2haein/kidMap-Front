package com.example.safe_map.Login;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.safe_map.MainActivity;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.example.safe_map.R;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {
    private LoginButton login;
    private final String TAG = "LoginActivity";
    private SessionCallback sessionCallback = new SessionCallback();;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button ChildLoginButton = findViewById(R.id.child_login);
        login = findViewById(R.id.login);

        ChildLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ChildLoginActivity.class);
                startActivity(intent);
            }
        });

        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);
        session.checkAndImplicitOpen();

        login.setOnClickListener(v -> {
            if (Session.getCurrentSession().checkAndImplicitOpen()) {
                Log.d(TAG, "onClick: 로그인 세션살아있음");
                // 카카오 로그인 시도 (창이 안뜬다.)
                sessionCallback.sessionRequest();

            } else {
                Log.d(TAG, "onClick: 로그인 세션끝남");
                // 카카오 로그인 시도 (창이 뜬다.)
                session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);
            }
        });

        getAppKeyHash();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i("KAKAO_API", "onActivityResult " + requestCode+ ":"+ resultCode+"::"+ data);
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            sessionRequest();
        }

        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+exception.toString(), Toast.LENGTH_SHORT).show();
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        // 사용자 정보 요청
        public void sessionRequest() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                }

                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }

                    Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                }



                @Override
                public void onSuccess(MeV2Response result) {
                    Intent intent;
                    String childNum = fetchChildNum(ProfileData.getUserId());
                    if ( childNum.equals("")){
                        intent = new Intent(getApplicationContext(), Signup.class);
                        Log.i("자녀수", "wjwd: " + "("+ childNum + ")");
                    } else {
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        Log.i("자녀수", "이미 저장된 자녀수: " + "("+ childNum + ")");
                    }


                    Log.i("KAKAO_API", "사용자 아이디: " + result.getId());
                    String id = String.valueOf(result.getId());
                    UserAccount kakaoAccount = result.getKakaoAccount();

                    if (kakaoAccount != null) {
                        // 이메일
                        String email = kakaoAccount.getEmail();

                        // 프로필 (닉네임, 프로필 사진 경로
                        Profile profile = kakaoAccount.getProfile();
                        new ProfileData(id, profile.getNickname(), profile.getProfileImageUrl(), profile.getThumbnailImageUrl());
                        intent.putExtra("userId", id);
                        intent.putExtra("nickName", profile.getNickname());
                        intent.putExtra("profile", profile.getProfileImageUrl());
                        intent.putExtra("thumbnail", profile.getThumbnailImageUrl());

                        if(result.getKakaoAccount().hasEmail() == OptionalBoolean.TRUE)
                            intent.putExtra("email", result.getKakaoAccount().getEmail());
                        else
                            intent.putExtra("email", "none");

                        // login 함수안에 연결할 서버 IP주소 설정 후 주석 풀기, (설정 안할 시 서버 접속 오류 남)
                        login(id, profile.getNickname());

                        // LOGGING
                        if (profile ==null){
                            Log.d("KAKAO_API", "onSuccess:profile null ");
                        }else{
                            Log.d("KAKAO_API", "onSuccess:getProfileImageUrl "+profile.getProfileImageUrl());
                            Log.d("KAKAO_API", "onSuccess:getThumbnailImageUrl "+profile.getThumbnailImageUrl());
                            Log.d("KAKAO_API", "onSuccess:getNickname "+profile.getNickname());
                        }
                        if (email != null) {
                            Log.d("KAKAO_API", "onSuccess:email "+email);
                        }
                    }else{
                        Log.i("KAKAO_API", "onSuccess: kakaoAccount null");
                    }

                    startActivity(intent);
                    finish();
                }
            });
        }

        // 서버와 연동하기
        public void login(String userId, String userName) {
            Log.w("login","로그인 하는중");
            try {
                Log.w("앱에서 보낸값",userId+", "+userName);

                CustomTask task = new CustomTask();
                String result = task.execute(userId,userName).get();
                Log.w("받은값",result);

            } catch (Exception e) {
                Log.w("로그인 에러", e);
            }
        }

    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground의 매개변수 값이 여러개일 경우를 위해 배열로
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL(CommonMethod.ipConfig +"/api/member");  // 어떤 서버에 요청할지(localhost 안됨.)
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //데이터를 POST 방식으로 전송합니다.
                conn.setDoOutput(true);

                // 서버에 보낼 값 포함해 요청함.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "userId="+strings[0]+"&userName="+strings[1]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter에 담아 전송
                osw.flush();
                Log.i("통신 중", "test");
                // jsp와 통신이 잘 되고, 서버에서 보낸 값 받음.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    Log.i("통신 결과", "test1");
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                        Log.i("통신 결과", "test2");
                    }
                    Log.i("통신 결과", "test3"+buffer);
                    receiveMsg = buffer.toString();
                    Log.i("통신 결과", receiveMsg);
                } else {    // 통신이 실패한 이유를 찍기위한 로그
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 서버에서 보낸 값을 리턴합니다.
            return receiveMsg;
        }
    }

    public String fetchChildNum(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchChildNum";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .toString();
            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
//            Toast.makeText(Signup.this, "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, String.format("가져온 childNum: (%s)", rtnStr));
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
    }



    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }
}