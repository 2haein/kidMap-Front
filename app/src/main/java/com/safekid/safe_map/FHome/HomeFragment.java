package com.safekid.safe_map.FHome;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.Login.ChildLoginActivity;
import com.safekid.safe_map.R;
import com.safekid.safe_map.RecyclerDecoration;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import org.json.JSONException;

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
    private RecyclerDecoration spaceDecoration;

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
                if (fetchErrandChecking(ProfileData.getUserId()).equals("true")){
                    Intent intent = new Intent(getActivity(), AddMissionActivity.class); //fragment라서 activity intent와는 다른 방식
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "현재 심부름 중입니다", Toast.LENGTH_LONG).show();

                }

            }
        });

        view.findViewById(R.id.imageView3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.youtube.com/watch?v=Ju-A4LxHxK4&ab_channel=%EB%A6%AC%ED%8B%80%ED%99%88%EB%9F%B0LittleHomeLearn";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        view.findViewById(R.id.imageView4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.youtube.com/watch?v=kjzFCRKdjkU&ab_channel=%ED%82%A4%EB%93%9C%ED%82%A4%EC%A6%88%EB%84%B7";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        //spaceDecoration = new RecyclerDecoration(10);
        //mRecyclerView.addItemDecoration(spaceDecoration);
        /* initiate adapter */
        mRecyclerAdapter = new ErrandRecyclerAdapter(getActivity());

        /* adapt data */
        mErrandHome = new ArrayList<>();
        /*for(int i=1;i<=5;i++){
            mErrandHome.add(new errandHome(Integer.toString(i), Integer.toString(i), Integer.toString(i),Integer.toString(i)));
        }*/
        //mErrandHome.add(new errandHome("첫째아이", "2022-02-22", "빵 사오기","뚜레쥴"));
        //mErrandHome.add(new errandHome("첫째아이", "2022-02-23", "빵 사오기","뚜레쥴"));
        //mErrandHome.add(new errandHome("둘째아이", "2022-02-24", "빵 사오기","뚜레쥴"));
        //mErrandHome.add(new errandHome("첫째아이", "2022-02-25", "빵 사오기","뚜레쥴"));
        //mErrandHome.add(new errandHome("둘째아이", "2022-02-26", "빵 사오기","뚜레쥴"));




        // 자녀 목록 불러오기
        String[] array = fetchUUID(ProfileData.getUserId());

        // 자녀별 심부름 목록 불러오기
        for(int i=0; i < array.length; i++){
            String childInfo = fetchChild(array[i]);
            try {
                JSONObject Alldata = new JSONObject(childInfo);
                String childName = Alldata.getString("childName");

                System.out.println("* errand *");
                JSONArray errandData = (JSONArray) Alldata.getJSONArray("errand");
                for(int j=0; j < errandData.length(); j++){
                    //Log.i("정보정보 ", errandData.getString(j));
                    JSONObject key = (JSONObject) errandData.getJSONObject(j);
                    //Log.i("하나 ", key.getString("target_name"));

                    String e_date = key.getString("e_date");
                    String e_content = key.getString("e_content");
                    String target_name = key.getString("target_name");
                    String target_latitude = key.getString("target_latitude");
                    String target_longitude = key.getString("target_longitude");
                    String start_name = key.getString("start_name");
                    String start_latitude = key.getString("start_latitude");
                    String start_longitude = key.getString("start_longitude");
                    String quest = key.getString("quest");

                    //List<String> questList = new ArrayList<String>();


                    if (quest != "") {
                        //JSONArray questData = (JSONArray) key.getJSONArray("quest");

                        String[] date = e_date.split("T");
                        mErrandHome.add(new errandHome(childName, date[0], e_content,target_name,start_name, quest));

                    } else {
                        String[] date = e_date.split("T");
                        mErrandHome.add(new errandHome(childName, date[0], e_content,target_name,start_name));

                    }
                    //SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
                    //Date tempDate = format.parse(e_date);

                    //String date = format.format(tempDate);



                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        mRecyclerAdapter.setErrandHome(mErrandHome);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        //updateErrandChecking();

        return view;
    }

    public String fetchErrandChecking(String parent_id){
        String url = CommonMethod.ipConfig + "/api/fetchErrandChecking";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", parent_id)
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
        String[] result = new String[0];

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
            Log.i("wkwkkwk" , rtnStr);
            //String result2 = rtnStr.substring(1, rtnStr.length() - 1);
            //Log.i("data22 " , result2);

            /*result = result2.split(",");
            for (int i=0; i<result.length; i++){
                result[i] = result[i].substring(1, result[i].length() - 1);
            }*/
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }
    //리스트로 반환
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
            Log.i("data " , data);
            String result2 = data.substring(1, data.length() - 1);
            Log.i("data22 " , result2);
            result = result2.split(",");
            for (int i=0; i<result.length; i++){
                result[i] = result[i].substring(1, result[i].length() - 1);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}