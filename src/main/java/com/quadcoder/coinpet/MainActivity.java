package com.quadcoder.coinpet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quadcoder.coinpet.audio.AudioEffect;
import com.quadcoder.coinpet.bluetooth.BluetoothManager;
import com.quadcoder.coinpet.bluetooth.BTConstants;
import com.quadcoder.coinpet.bluetooth.BluetoothUtil;
import com.quadcoder.coinpet.logger.Log;
import com.quadcoder.coinpet.logger.LogWrapper;
import com.quadcoder.coinpet.network.NetworkManager;
import com.quadcoder.coinpet.network.response.Res;
import com.quadcoder.coinpet.page.common.GoalSettingActivity;
import com.quadcoder.coinpet.page.freinds.FriendsActivity;
import com.quadcoder.coinpet.page.mypet.MyPetActivity;
import com.quadcoder.coinpet.page.quest.QuestActivity;
import com.quadcoder.coinpet.page.quiz.QuizActivity;
import com.quadcoder.coinpet.page.setting.SettingActivity;
import com.quadcoder.coinpet.page.tutorial.TutorialActivity;


public class MainActivity extends Activity {

    static final int REQUEST_ENABLE_BT = 1;
    BluetoothDevice mDevice;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBtAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothManager mChatService = null;

    private StringBuffer mOutStringBuffer;

    TextView tvNowMoney;
    boolean isDoneFirstQuest;
    static final int REQUEST_CODE_GOAL_SETTING_ACTIVITY = 10;

    @Override
    protected void onStart() {
        super.onStart();

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothManager.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        startAnimation();

        initializeLogging();
    }

    public static final String TAG = "MainActivity";

    /** Set up targets to receive log data */
    public void initializeLogging() {
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        Log.i(TAG, "Ready");
    }

    boolean isRegistered = false;

    public void discovery(){
        mBtAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        isRegistered = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mChatService != null) {
            mChatService.stop();
        }

        if(isRegistered)
            unregisterReceiver(mReceiver);
        NetworkManager.getInstance().cancelRequests(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.


    }

    void startAnimation() {
        ((AnimationDrawable)imgvPet.getDrawable()).start();

        Animation animCloud1 = AnimationUtils.loadAnimation(this, R.anim.cloud1_anim);
        imgvCloud1.startAnimation(animCloud1);
        Animation animCloud2 = AnimationUtils.loadAnimation(this, R.anim.cloud2_anim);
        imgvCloud2.startAnimation(animCloud2);
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        imgvMail.startAnimation(shake);
    }

    ImageView imgvPet;
    ImageView imgvCloud1;
    ImageView imgvCloud2;
    ImageView imgvMailBg;
    ImageView imgvMail;
    ProgressBar pbarExp;
    TextView tvExpText;
    FrameLayout frameTalk;
    FrameLayout frameQuest;
    ImageView imgvLevelup;
    TextView tvTalk;
    ImageView imgvHeart;
    TextView tvLevel;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //블루투스 연결 권장 다이얼로그 호출 결과
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChatService();
                    mChatService.setState(BluetoothManager.STATE_BT_ENABLED);
                    connectBt();
                    PropertyManager.getInstance().setBtRequested(true);
                    Log.d("phangji bt", "bt enabled result ok");

                } else if (requestCode == RESULT_CANCELED) {
                    PropertyManager.getInstance().setBtRequested(false);
                    Log.d("phangji bt", "bt enabled canceled");
                } else {    //블루투스를 켜시겠습니까? 아니요. OR 취소
//                    mChatService.setState(BluetoothManager.STATE_NONE);
                    PropertyManager.getInstance().setBtRequested(false);
                    Log.d("phangji bt", "BT not enabled");
                }
                break;
            case REQUEST_CODE_GOAL_SETTING_ACTIVITY:
                if(resultCode == Activity.RESULT_OK) {
                    isDoneFirstQuest = data.getBooleanExtra(GoalSettingActivity.RESULT_GOAL_SET, false);
                    if(isDoneFirstQuest) {
                        frameQuest.setVisibility(View.INVISIBLE);
                        frameTalk.setVisibility(View.VISIBLE);
                        int gage = 70;
                        pbarExp.setProgress(gage);
                        tvExpText.setText(gage + "/100");

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                frameTalk.setVisibility(View.INVISIBLE);
                                frameQuest.setVisibility(View.VISIBLE);
                            }
                        }, 2000);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    String pnMsg = null;
    void makePnMsg() {
        final char[] registerPn = new char[20];
        registerPn[0] = 'S';
        registerPn[1] = 0x01;
        registerPn[2] = 16;
        registerPn[19] = 'E';
        char[] pn = "1234123412341234".toCharArray();
        for(int i=3; i<19; i++) {
            registerPn[i] = pn[i-3];
        }
        pnMsg = new String(registerPn);
        Log.d("registerPn", pnMsg);
    }

    void connectBt() {
        if(mChatService.getState() == BluetoothManager.STATE_BT_ENABLED) {
            mDevice = mChatService.searchPaired();

            if (mDevice == null) {  //페어링된 적이 없다면,
                discovery();
            }
            mChatService.connect(mDevice);
        }
    }

    boolean isLevelup;

    void setMainLayout() {

        tvNowMoney = (TextView)findViewById(R.id.tvNowMoney);
        imgvCloud1 = (ImageView)findViewById(R.id.imgvCloud1);
        imgvCloud2 = (ImageView)findViewById(R.id.imgvCloud2);
        imgvPet = (ImageView)findViewById(R.id.imgvPet);
        imgvMailBg = (ImageView)findViewById(R.id.imgvMailBg);
        imgvMail = (ImageView)findViewById(R.id.imgvMail);
        pbarExp = (ProgressBar)findViewById(R.id.pbarExp);
        tvExpText = (TextView)findViewById(R.id.tvExpText);
        frameTalk = (FrameLayout)findViewById(R.id.frameTalk);
        frameQuest = (FrameLayout)findViewById(R.id.frameQuest);
        tvTalk = (TextView)findViewById(R.id.tvTalk);
        imgvHeart = (ImageView)findViewById(R.id.imgvHeart);
        imgvLevelup = (ImageView)findViewById(R.id.imgvLevelup);
        tvLevel = (TextView)findViewById(R.id.tvLevel);
        Typeface font = Typeface.createFromAsset(getAssets(), com.quadcoder.coinpet.page.common.Constants.FONT_NORMAL);
        tvTalk.setTypeface(font);

        final AudioEffect audio = new AudioEffect(AudioEffect.CARTOON_BOING);

        //펫 이미지 탭 했을 때
        imgvPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLevelup) {
                    imgvPet.setImageResource(R.drawable.pet_happy_anim);
                    ((AnimationDrawable)imgvPet.getDrawable()).start();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imgvPet.setImageResource(R.drawable.pet_default_anim);
                            ((AnimationDrawable) imgvPet.getDrawable()).start();
                        }
                    }, 1000);

                }
                audio.play();
            }
        });

        //퀘스트 아이콘 탭했을 때
        imgvMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDoneFirstQuest) {
                    Intent i = new Intent(MainActivity.this, GoalSettingActivity.class);
                    startActivity(i);
                } else {
                    imgvHeart.setVisibility(View.INVISIBLE);
                    frameQuest.setVisibility(View.INVISIBLE);
                    tvTalk.setVisibility(View.VISIBLE);
                    frameTalk.setVisibility(View.VISIBLE);
                    tvTalk.setText("첫 동전 넣기");
                }

            }
        });

        // MyPet
        ImageView mainBtn = (ImageView)findViewById(R.id.imgvMyPet);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyPetActivity.class));
            }
        });

        // Quiz
        mainBtn = (ImageView)findViewById(R.id.imgvCashbook);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QuizActivity.class));
            }
        });

        // Quest
        mainBtn = (ImageView)findViewById(R.id.imgvQuest);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QuestActivity.class));
            }
        });

        // Freinds
        mainBtn = (ImageView)findViewById(R.id.imgvReward);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FriendsActivity.class));
            }
        });

        // Sync
        mainBtn = (ImageView)findViewById(R.id.imgvSync);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //임시로 Tutorial 실행
                startActivity(new Intent(MainActivity.this, TutorialActivity.class));
            }
        });

        // Setting
        mainBtn = (ImageView)findViewById(R.id.imgvSetting);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMainLayout();

        Log.d("phangji bt", "onCreat isBtRequested" + PropertyManager.getInstance().isBtReqested());
        if(PropertyManager.getInstance().isBtReqested())
            setBtEnvironment();
    }



    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //선택한 디바이스를 받아오면
                    if(device.getName() != null &&device.getName().equals(mChatService.SERVICE_NAME)){
                        Toast.makeText(MainActivity.this, device.getName() + " discovered", Toast.LENGTH_SHORT).show();
                        mDevice = device;
                        mBtAdapter.cancelDiscovery();
                        mChatService.setState(BluetoothManager.STATE_DISCOVERING);
                    }
            }
        }
    };

    /**
     *  Bluetooth 환경 설정
     */

    public void setBtEnvironment() {
        //Bluetooth 환경 설정
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) {
            Toast.makeText(MainActivity.this, "블루투스를 지원하지 않는 휴대폰입니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else if(!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if(mChatService == null) {
            setupChatService();
            mChatService.setState(BluetoothManager.STATE_BT_ENABLED);
            connectBt();
        }
        else {
            mChatService.setState(BluetoothManager.STATE_BT_ENABLED);
            connectBt();
        }
    }

    private void setupChatService() {
        Log.d(TAG, "setupChatService()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothManager(MainActivity.this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }



    /**
     * The Handler that gets information back from the BluetoothChatManager
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BTConstants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, "state connected", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "state connecting", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:
                            Toast.makeText(MainActivity.this, "state none", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case BTConstants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(MainActivity.this, "Me : " + writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case BTConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(MainActivity.this, "MainActivity / Device : " + readMessage, Toast.LENGTH_SHORT).show();

                    if(readBuf != null && readBuf[1] == BluetoothUtil.Opcode.READ_MONEY) {   // 동전입력 프로토콜
                        int[] num = new int[3];
                        for(int i=3; i<=5; i++) {
                            num[i-3] = readBuf[i];
                            if(num[i-3] < 0) {
                                num[i-3] += 256;
                            }
                        }
                        Log.d("TAG", num[0] + " " + num[1] + " " + num[2] + " ");
                        final int money = num[0] * 256 * 256 + num[1] * 256 + num[2];
                        int sum = Integer.parseInt(tvNowMoney.getText().toString()) + money;
                        tvNowMoney.setText("" + sum);
                        PropertyManager.getInstance().mGoal.now_cost = sum;

                        NetworkManager.getInstance().sendCoin(MainActivity.this, money, new NetworkManager.OnNetworkResultListener<Res>() {
                            @Override
                            public void onResult(Res res) {

                                if(!isLevelup) {
                                    imgvPet.setImageResource(R.drawable.pet_happy_anim);
                                    ((AnimationDrawable)imgvPet.getDrawable()).start();
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            imgvPet.setImageResource(R.drawable.pet_default_anim);
                                            ((AnimationDrawable)imgvPet.getDrawable()).start();
                                        }
                                    }, 1000);

                                    imgvLevelup.setVisibility(View.VISIBLE);
                                    Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.mail_bg_anim);
                                    imgvLevelup.startAnimation(shake);
                                    mHandler.postDelayed(new Runnable() {   //edited
                                        @Override
                                        public void run() {
                                            imgvLevelup.setVisibility(View.INVISIBLE);
                                            imgvLevelup.setImageDrawable(null);
                                        }
                                    }, 3000);
                                    frameQuest.setVisibility(View.INVISIBLE);
                                    tvTalk.setVisibility(View.INVISIBLE);
                                    frameTalk.setVisibility(View.VISIBLE);
                                    pbarExp.setMax(200);
                                    pbarExp.setProgress(20);
                                    tvExpText.setText("20/200");
                                    tvLevel.setText("Lv 2");
                                    imgvHeart.setVisibility(View.VISIBLE);
                                    isLevelup = true;
                                }

                            }

                            @Override
                            public void onFail(Res res) {

                            }
                        });
                    }

                    if(readMessage != null && readBuf[1] == BluetoothUtil.Opcode.READ_MONEY_SYNC) {
                        //오랜만에 sync됐을 때 처리들 - UTC랑 여러 번 옴.
                    }

                    break;
                case BTConstants.MESSAGE_DEVICE_NAME:
                    String deviceName = msg.getData().getString(BTConstants.DEVICE_NAME);
                    Toast.makeText(MainActivity.this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BTConstants.MESSAGE_TOAST:
//                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };
}
