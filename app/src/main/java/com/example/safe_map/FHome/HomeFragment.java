package com.example.safe_map.FHome;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.safe_map.Login.ChildLoginActivity;
import com.example.safe_map.Login.ChildnumItem;
import com.example.safe_map.Login.Signup;
import com.example.safe_map.Login.StdRecyclerAdapter;
import com.example.safe_map.MainActivity;
import com.example.safe_map.R;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private ImageButton childButton2;
    private Button addmissionbutton;

    private RecyclerView mRecyclerView;
    private ErrandRecyclerAdapter mRecyclerAdapter;
    private ArrayList<errandHome> mErrandHome;

    public HomeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        childButton2 = (ImageButton) view.findViewById(R.id.childButton2); //fragment에서 findViewByid는 view.을 이용해서 사용

        childButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChildLoginActivity.class); //fragment라서 activity intent와는 다른 방식
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                getActivity().finish();
            }
        });

        addmissionbutton = (Button) view.findViewById(R.id.addmission); //fragment에서 findViewByid는 view.을 이용해서 사용

        addmissionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddMissionActivity.class); //fragment라서 activity intent와는 다른 방식
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        /* initiate adapter */
        mRecyclerAdapter = new ErrandRecyclerAdapter(getActivity());

        /* adapt data */
        mErrandHome = new ArrayList<>();
        /*for(int i=1;i<=5;i++){
            mErrandHome.add(new errandHome(Integer.toString(i), Integer.toString(i), Integer.toString(i),Integer.toString(i)));
        }*/
        mErrandHome.add(new errandHome("첫째아이", "2022-02-22", "빵 사오기","뚜레쥴"));
        mErrandHome.add(new errandHome("첫째아이", "2022-02-23", "빵 사오기","뚜레쥴"));
        mErrandHome.add(new errandHome("둘째아이", "2022-02-24", "빵 사오기","뚜레쥴"));
        mErrandHome.add(new errandHome("첫째아이", "2022-02-25", "빵 사오기","뚜레쥴"));
        mErrandHome.add(new errandHome("둘째아이", "2022-02-26", "빵 사오기","뚜레쥴"));


        mRecyclerAdapter.setErrandHome(mErrandHome);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        return view;
    }

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

        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }

    public String fetchChild(String UUID){
        String url = CommonMethod.ipConfig + "/api/fetchChild";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
//           Log.i(TAG, String.format("가져온 Phonenum: (%s)", rtnStr));
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }
}