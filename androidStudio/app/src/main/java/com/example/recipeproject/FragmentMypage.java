package com.example.recipeproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentMypage extends Fragment {
    Button profile_modify;
    TextView shoppingList, myRefrigerator, scrap, name;
    ImageView imageView;

    MainActivity.MyDBHelper dbHelper;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        profile_modify = (Button) view.findViewById(R.id.btnProfile);
        shoppingList = (TextView) view.findViewById(R.id.shoppinglist);
        myRefrigerator = (TextView) view.findViewById(R.id.refrigerator);
        scrap = (TextView) view.findViewById(R.id.scrap);
        name = (TextView) view.findViewById(R.id.name);
        imageView = (ImageView) view.findViewById(R.id.imageView);


        dbHelper = new MainActivity.MyDBHelper(getActivity(), "recipe", null, 1);

        //        다른 액티비티에서 아이디 사용
        SharedPreferences sharedPref = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        String id = sharedPref.getString("userId", "");  // 저장된 아이디 불러오기

        // 사용자 ID로 닉네임 가져오기
        String nickname = dbHelper.getNicknameById(id);
        if (nickname != null) {
            name.setText(nickname);
        } else {
            name.setText("닉네임을 찾을 수 없습니다.");
        }


        profile_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });
        shoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), shopping.class);
                startActivity(intent);
            }
        });
        myRefrigerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MyRefrigeratorActivity.class);
                startActivity(intent);
            }
        });
        scrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ScrapListActivity.class);
                startActivity(intent);
            }
        });



        // Inflate the layout for this fragment
        return view;
    }
}