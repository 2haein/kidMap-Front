package com.safekid.safe_map.FMypage;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
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

import com.safekid.safe_map.FHome.AddMissionActivity;
import com.safekid.safe_map.FHome.AddressApiActivity;
import com.safekid.safe_map.Login.ChildLoginActivity;
import com.safekid.safe_map.Login.ChildnumItem;
import com.safekid.safe_map.Login.LoginActivity;
import com.safekid.safe_map.Login.StdRecyclerAdapter;
import com.safekid.safe_map.NetworkStatus;
import com.safekid.safe_map.R;
import com.safekid.safe_map.callback.SessionCallback;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.http.RequestHttpURLConnection;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MypageFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class MypageFragment extends Fragment {
    private SessionCallback sessionCallback = new SessionCallback();
    private final String TAG = "Mypage logout";
    public String phone = "";
    public double home_longitude, home_latitude;
    public String[] UUIDArray;
    EditText brand_phone;
    TextView edit_addr;
    List<Address> address = null;

    private RecyclerView mRecyclerView;
    private StdRecyclerAdapter mRecyclerAdapter;
    private ArrayList<ChildnumItem> mChildnum;

    private static final int SEARCH_ADDRESS_ACTIVITY = 30000;

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
        UUIDArray = fetchUUID(ProfileData.getUserId());

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
        final String childnum1 = fetchChildNum(ProfileData.getUserId());
        final int childNum = Integer.parseInt(childnum1);
        /* adapt data */
        mChildnum = new ArrayList<>();
        for(int i = 1; i<= childNum; i++){
            if (i==1){
                mChildnum.add(new ChildnumItem("첫째아이",UUIDArray[0]) );
            } else if (i==2){
                mChildnum.add(new ChildnumItem("둘째아이", UUIDArray[1]));
            } else if (i==3){
                mChildnum.add(new ChildnumItem("셋째아이", UUIDArray[2]));
            } else if (i==4){
                mChildnum.add(new ChildnumItem("넷째아이", UUIDArray[3]));
            } else if (i==5) {
                mChildnum.add(new ChildnumItem("다섯째아이", UUIDArray[4]));
            } else {
                mChildnum.add(new ChildnumItem("자녀가 없습니다", "X"));
            }
        }

        mRecyclerAdapter.setChildNum(mChildnum);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        mRecyclerAdapter.setOnItemClickListener(new StdRecyclerAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View a_view, int a_position) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getContext());
                dlg.setTitle("자녀 ID 복사하기");
                dlg.setMessage("자녀 ID를 복사하시겠습니까?");
                dlg.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // 처리할 코드 작성
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                int selectChild = a_position;
                                Toast.makeText(getContext(), "클립보드에 자녀 ID 복사", Toast.LENGTH_SHORT).show();

                                ClipData clip = ClipData.newPlainText("label", UUIDArray[selectChild]);
                                if (clipboard == null || clip == null) return;
                                clipboard.setPrimaryClip(clip);
                            }
                        });
                dlg.show();
            }
        });

        mRecyclerAdapter.setOnItemLongClickListener(new StdRecyclerAdapter.OnItemLongClickEventListener(){
            public void onItemLongClick(View a_view, int a_position){

            }
        });

        //전화번호 불러오기
        TextView phonenum = (TextView) rootview.findViewById(R.id.mp_phonenum);
        String phone1 = fetchPhone(ProfileData.getUserId());
        if (phone1.equals("")) {
            phonenum.setText("등록된 전화번호가 없습니다.");
        }else{
            Log.d("phonenum", "Phonenumber : "+ phone1);
            phonenum.setText(phone1);
        }

        //전화번호 입력하기
        brand_phone = (EditText) rootview.findViewById(R.id.edit_phone);
        brand_phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        //전화번호 수정하기 버튼 클릭
        rootview.findViewById(R.id.phonenum_m).setOnClickListener(v -> {
            try {
                phone = brand_phone.getText().toString();
                registerPhone(phone);
            } catch(NumberFormatException e) {
                phone = "";
                Toast.makeText(getContext().getApplicationContext(), "전화번호를 다시 입력해주세요", Toast.LENGTH_LONG).show();
            }
        });

        edit_addr = rootview.findViewById(R.id.mp_address);

        //집주소 입력하기
        rootview.findViewById(R.id.addr_search).setOnClickListener(v -> {
            try {
                Log.i("주소설정페이지", "주소입력창 클릭");
                int status = NetworkStatus.getConnectivityStatus(getContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                    Log.i("주소설정페이지", "주소입력창 클릭");
                    Intent i = new Intent(getContext(), AddressApiActivity.class);
                    // 화면전환 애니메이션 없애기
                    // overridePendingTransition(0, 0);
                    // 주소결과
                    getActivity().startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
                    //MainActivity.moveToAddressApi();
                }else {
                    Toast.makeText(getContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }} catch(NumberFormatException e) {
                    home_latitude = 0;
                    home_longitude = 0;
                    Toast.makeText(getContext().getApplicationContext(), "집 주소를 다시 입력해주세요", Toast.LENGTH_LONG).show();
            }
        });

        //집주소 등록하기
        rootview.findViewById(R.id.addr_m).setOnClickListener(v -> {
            try {
                //registerHome();
            } catch (NumberFormatException e) {
                home_latitude = 0;
                home_longitude = 0;
                Toast.makeText(getContext().getApplicationContext(), "집 주소를 다시 입력해주세요", Toast.LENGTH_LONG).show();
            }
        });
        return rootview;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("test", "onActivityResult");
        String data = intent.getExtras().getString("data");
        if (data != null) {
            Log.i("test", "data:" + data);
            edit_addr.setText(data);
            Geocoder geocoder = new Geocoder(getContext());
            try {
                address = geocoder.getFromLocationName(data, 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (address != null) {
                if (address.size() == 0) {
                    Log.i("nonono", "0");
                    Toast.makeText(getContext(), "해당되는 주소의 위도, 경도 값을 찾을 수 없습니다", Toast.LENGTH_LONG);
                } else {
                    Address addr = address.get(0);
                    Log.i("address 변환 ok", String.valueOf(addr.getLatitude()));
                    home_latitude = addr.getLatitude();
                    home_longitude = addr.getLongitude();
                    Toast.makeText(getContext(), "해당되는 주소의 위도, 경도 값을 설정하였습니다", Toast.LENGTH_LONG);
                }
            }
        }
    }

    public String[] fetchUUID(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchUUID";
        List<String> arrayList = new ArrayList<String>();
        String data = "";
        String[] result = new String[0];
        try{
            String jsonString = new JSONObject()
                    .put("userId", userId)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            data = networkTask.execute().get();
            Log.i("111 " , data);
            String parsingData = data.substring(1, data.length() - 1);
            Log.i("112 " , parsingData);
            result = parsingData.split(",");
            for (int i=0; i<result.length; i++){
                result[i] = result[i].substring(1, result[i].length() - 1);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //super.onActivityResult(requestCode, resultCode, intent);
        Log.i("test", "onActivityResult");
        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String data = intent.getExtras().getString("data");
                    if (data != null) {
                        Log.i("test", "data:" + data);
                        edit_addr.setText(data);
                        Geocoder geocoder = new Geocoder(getContext());
                        try {
                            address = geocoder.getFromLocationName(data, 10);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (address != null) {
                            if (address.size() == 0){
                                Log.i("nonono","0");
                                Toast.makeText(getContext(),"해당되는 주소의 위도, 경도 값을 찾을 수 없습니다",Toast.LENGTH_LONG);
                            } else {
                                Address addr = address.get(0);
                                Log.i("address 변환 ok" , String.valueOf(addr.getLatitude()));
                                home_latitude = addr.getLatitude();
                                home_longitude = addr.getLongitude();
                                Toast.makeText(getContext(),"해당되는 주소의 위도, 경도 값을 설정하였습니다",Toast.LENGTH_LONG);
                            }
                        }
                    }
                }
                break;
        }
    }*/

    public String fetchChildNum(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchChildNum";
        String rtnStr= "";
        try{
            String jsonString = new JSONObject()
                    .put("userId", userId)
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
            Toast.makeText(getContext().getApplicationContext(), "전화번호가 저장되었습니다", Toast.LENGTH_LONG).show();
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
                    .put("userId", userId)
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

    public void registerHome(String userId, String latitude, String longitude){
        String url = CommonMethod.ipConfig + "/api/registerTelNum";

        try{
            String jsonString = new JSONObject()
                    .put("userId", userId)
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
