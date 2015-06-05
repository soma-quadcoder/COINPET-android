package com.quadcoder.coinpet.page.splash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ImageView;

import com.quadcoder.coinpet.MainActivity;
import com.quadcoder.coinpet.PropertyManager;
import com.quadcoder.coinpet.R;
import com.quadcoder.coinpet.database.DBManager;
import com.quadcoder.coinpet.model.ParentQuest;
import com.quadcoder.coinpet.model.Quest;
import com.quadcoder.coinpet.model.Quiz;
import com.quadcoder.coinpet.model.SystemQuest;
import com.quadcoder.coinpet.network.NetworkManager;
import com.quadcoder.coinpet.network.response.UpdatedData;

public class SplashActivity extends Activity {

    private static final int DELAY_TIME = 800;
    Handler mHandler = new Handler();
    AnimationDrawable anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgvPet = (ImageView)findViewById(R.id.imgvPet);
        anim = (AnimationDrawable)imgvPet.getDrawable();
        anim.start();

//        checkUpdatedData();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                startActivity(new Intent(SplashActivity.this, TutorialActivity.class));
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, DELAY_TIME);

    }

    private void checkUpdatedData() {
        int pkQuiz = PropertyManager.getInstance().getPkQuiz();
        int pkQuest = PropertyManager.getInstance().getPkQuest();
        NetworkManager.getInstance().getUpdatedData(this, pkQuiz, pkQuest, new NetworkManager.OnNetworkResultListener<UpdatedData>() {
            @Override
            public void onResult(UpdatedData res) {
                if(res.needUpdate) {
                    if(res.systemQuiz.size() != 0) {
                        for(Quiz record : res.systemQuiz) {
                            DBManager.getInstance().insertQuiz(record);
                        }
                        Quiz last = res.systemQuiz.get(res.systemQuiz.size() - 1);
                        PropertyManager.getInstance().setPkQuiz(last.pk_std_quiz);
                    }
                    if(res.systemQuest.size() != 0) {
                        for(SystemQuest record : res.systemQuest) {
                            SystemQuest newOne = record;
                            newOne.state = Quest.CREATED;
                            DBManager.getInstance().insertSystemQuest(newOne);
                        }
                        SystemQuest last = res.systemQuest.get(res.systemQuiz.size() - 1);
                        PropertyManager.getInstance().setPkQuest(last.pk_std_que);
                    }
                    if(res.parentsQuest.size() != 0) {
                        for(ParentQuest record : res.parentsQuest) {
                            DBManager.getInstance().insertParentQuest(record);
                        }
                        ParentQuest last = res.parentsQuest.get(res.parentsQuest.size() - 1);
                        PropertyManager.getInstance().setPkPQuest(last.pk_parents_quest);
                    }
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                startActivity(new Intent(SplashActivity.this, TutorialActivity.class));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                }, DELAY_TIME);
            }

            @Override
            public void onFail(UpdatedData res) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                startActivity(new Intent(SplashActivity.this, TutorialActivity.class));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                }, DELAY_TIME);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkManager.getInstance().cancelRequests(this);
    }
}
