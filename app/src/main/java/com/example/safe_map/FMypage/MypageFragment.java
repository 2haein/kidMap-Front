package com.example.safe_map.FMypage;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safe_map.FHome.AddMissionActivity;
import com.example.safe_map.Login.ChildnumItem;
import com.example.safe_map.Login.LoginActivity;
import com.example.safe_map.Login.Signup;
import com.example.safe_map.Login.StdRecyclerAdapter;
import com.example.safe_map.MainActivity;
import com.example.safe_map.R;
import com.example.safe_map.callback.SessionCallback;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MypageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MypageFragment extends Fragment {
    private SessionCallback sessionCallback = new SessionCallback();
    private final String TAG = "Mypage logout";
    public String phone = "";
    public String home_longitude = "";
    public String home_latitude = "";
    EditText brand_phone;

    private RecyclerView mRecyclerView;
    private StdRecyclerAdapter mRecyclerAdapter;
    private ArrayList<ChildnumItem> mChildnum;

    public MypageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootview = (ViewGroup)inflater.inflate(R.layout.fragment_mypage, container, false);
        Button logout = (Button)rootview.findViewById(R.id.logout);;


        logout.setOnClickListener(v -> {
            UserManagement.getInstance()
                    .requestLogout(new LogoutResponseCallback() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            super.onSessionClosed(errorResult);
                            Log.d(TAG, "onSessionClosed: " + errorResult.getErrorMessage());
                        }
                        @Override
                        public void onCompleteLogout() {
                            if (sessionCallback != null) {
                                Session.getCurrentSession().removeCallback(sessionCallback);
                            }
                            Log.d(TAG, "onCompleteLogout:logout ");
                        }
                    });
            Intent i = new Intent(getContext(), LoginActivity.class);
            this.startActivity(i);

        });
        //자녀수 불러오기
        TextView mp_childnum = (TextView) rootview.findViewById(R.id.mp_childnum);
        String childnum = fetchChildNum(ProfileData.getUserId());
        mp_childnum.setText(childnum);

        //자녀 목록 불러오기
        mRecyclerView = (RecyclerView) rootview.findViewById(R.id.recyclerview4);
        /* initiate adapter */
        mRecyclerAdapter = new StdRecyclerAdapter(getContext());

        /* adapt data */
        mChildnum = new ArrayList<>();
        /*for(int i=1;i<=5;i++){
            mChildnum.add(new ChildnumItem(i+"명"));
        }*/
        mChildnum.add(new ChildnumItem("첫째아이"));
        mChildnum.add(new ChildnumItem("둘째아이"));
        mChildnum.add(new ChildnumItem("셋째아이"));

        mRecyclerAdapter.setChildNum(mChildnum);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        mRecyclerAdapter.setOnItemClickListener(new StdRecyclerAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View a_view, int a_position) {
                //Toast.makeText(getApplicationContext(), a_position, Toast.LENGTH_LONG).show();
            }
        });

        //전화번호 불러오기
        TextView phonenum = (TextView) rootview.findViewById(R.id.mp_phonenum);
        if (fetchPhone(ProfileData.getUserId()) == "") {
            phonenum.setText("등록된 전화번호가 없습니다.");
        }else{
            String phone = fetchPhone(ProfileData.getUserId());
            Log.d("phonenum", "Phonenumber : "+ phone);
            phonenum.setText(phone);
        }

        //전화번호 입력하기
        brand_phone = (EditText) rootview.findViewById(R.id.edit_phone);
        brand_phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //전화번호 수정하기 버튼 클릭
        //rootview.findViewById(R.id.phonenum_m).setOnClickListener(buttonClickListener);
        rootview.findViewById(R.id.phonenum_m).setOnClickListener(v -> {
            try {
                phone = brand_phone.getText().toString();
                registerPhone(phone);
                Toast.makeText(getContext().getApplicationContext(), "전화번호가 저장되었습니다", Toast.LENGTH_LONG).show();
            } catch(NumberFormatException e) {
                phone = "";
                Toast.makeText(getContext().getApplicationContext(), "전화번호를 다시 입력해주세요", Toast.LENGTH_LONG).show();
            }
            });

        //집주소 등록하기
        rootview.findViewById(R.id.addr_m).setOnClickListener(v -> {
            try {
                //phone = brand_phone.getText().toString();
                //Toast.makeText(getContext().getApplicationContext(), "집 주소가 저장되었습니다", Toast.LENGTH_LONG).show();
            } catch(NumberFormatException e) {
                home_latitude = "";
                home_longitude = "";
                Toast.makeText(getContext().getApplicationContext(), "집 주소를 다시 입력해주세요", Toast.LENGTH_LONG).show();
            }
        });
        return rootview;
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
//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, String.format("가져온 childNum: (%s)", rtnStr));
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }
    public void registerPhone(String phone){
        String url = CommonMethod.ipConfig + "/api/registerTelNum";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("telNum", phone)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();

//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, String.format("등록한 Phonenum: " + phone));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String fetchPhone(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchTelNum";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, String.format("가져온 Phonenum: (%s)", rtnStr));

        }catch(Exception e){
            e.printStackTrace();
        }

        return rtnStr;

    }

    public void registerHome(String latitude, String longitude){
        String url = CommonMethod.ipConfig + "/api/registerTelNum";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("telNum", phone)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();

//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, String.format("등록한 Phonenum: " + phone));

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}