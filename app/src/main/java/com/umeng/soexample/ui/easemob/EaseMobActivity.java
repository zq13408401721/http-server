package com.umeng.soexample.ui.easemob;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.umeng.soexample.R;
import com.umeng.soexample.base.BaseAdapter;
import com.umeng.soexample.module.data.EMUserInfo;
import com.umeng.soexample.utils.SpUtils;
import com.umeng.soexample.utils.UserInfoManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EaseMobActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EaseMobActivity.class.getSimpleName();

    private Button btnLogin;
    RecyclerView recyUserList;
    List<EMUserInfo> userList;
    FriendsAdapter friendsAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ease_mob);

        btnLogin = findViewById(R.id.btn_login);
        recyUserList = findViewById(R.id.recy_userList);




        btnLogin.setOnClickListener(this);

        initUserList();

    }

    private void initUserList(){
        userList = UserInfoManager.getInstance().getAllUsers();
        friendsAdapter = new FriendsAdapter(this,userList);
        recyUserList.setLayoutManager(new LinearLayoutManager(this));
        recyUserList.setAdapter(friendsAdapter);
        friendsAdapter.addListClick(new BaseAdapter.IListClick() {
            @Override
            public void itemClick(int pos) {
                String userid = userList.get(pos).getUid();
                Intent intent = new Intent(EaseMobActivity.this,ChatActivity.class);
                intent.putExtra("touserid",userList.get(pos).getUid());
                startActivity(intent);
            }
        });

        //处理点击条目中的按钮
        friendsAdapter.addItemViewClick(new BaseAdapter.IItemViewClick<String>() {
            @Override
            public void itemViewClick(int viewid, String data) {
                Intent intent = new Intent(EaseMobActivity.this,UserDetailActivity.class);
                intent.putExtra("username",data);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void login(){
    //登录
        String username = "2002a_2";
        String password = "123456";
        Log.i(TAG,"环信login");
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"登录成功");
                getFriends();
            }

            @Override
            public void onError(int code, String error) {
                Log.i(TAG,"onError:"+error);
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.i(TAG,"status:"+status);
            }
        });
    }

    /**
     * 获取好友
     */
    private void getFriends() {
        try {
            List<String> friends = EMClient.getInstance().contactManager().getAllContactsFromServer();
            List<EMUserInfo> list = new ArrayList<>();
            for(String item:friends){
                EMUserInfo user = new EMUserInfo();
                user.setUid(item);
                String header = SpUtils.getInstance().getString(item);
                if(!TextUtils.isEmpty(header)){
                    user.setHeader(header);
                }
                list.add(user);
            }
            UserInfoManager.getInstance().addUsers(list);
            if(friends != null){
                recyUserList.post(new Runnable() {
                    @Override
                    public void run() {
                        friendsAdapter.notifyDataSetChanged();
                    }
                });
            }
        }catch (HyphenateException e){
            e.printStackTrace();
        }

    }
}