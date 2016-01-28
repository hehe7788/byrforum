package byr.byrforum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import byr.byrforum.adapter.TopicAdapter;
import byr.byrforum.javabean.OneFloor;
import byr.byrforum.javabean.OneFloorPic;
import byr.byrforum.javabean.OneTopic;

public class TopicActivity extends AppCompatActivity {
    final String TAG = "TopicActicity";
    final String JSON_DATA = "jsondata";
    TextView oneTitle;
    TextView oneReply;
    TextView oneBoard;
    Button prePage;
    Button nextPage;
    int currentPage = 1;
    int oneReplyNum = 0;
    private boolean mContentLoaded;
    private View mContentView;
    private View mLoadingView;
    private View mRefreshView;
    private int mShortAnimationDuration;
    Handler handler;
    String topicBoard;
    String topicId;

    ArrayList<OneFloorPic> floorPics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);

        mContentView = findViewById(R.id.content);
        mLoadingView = findViewById(R.id.loading_spinner);
        mRefreshView = findViewById(R.id.loading_refresh);

        // Initially hide the content view.
        mContentView.setVisibility(View.GONE);
        mRefreshView.setVisibility(View.GONE);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        //获取板块和帖子id
        Intent intent = getIntent();
        String linkStr = intent.getStringExtra("linkStr");
        Log.e(TAG, linkStr);
        String[] words = linkStr.split("/");
//        for(String i : words) {
//            Log.e(TAG, i);
//        }
        topicBoard = words[4];
        topicId = words[5];

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 404) {
                    Log.e(TAG, "error");
                    Toast.makeText(TopicActivity.this,"请检查网络是否连接",Toast.LENGTH_SHORT).show();
                    mLoadingView.setVisibility(View.GONE);
                    mRefreshView.setVisibility(View.VISIBLE);
                    mRefreshView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mLoadingView.setVisibility(View.VISIBLE);
                            mRefreshView.setVisibility(View.GONE);
                            NetThread thread = new NetThread();
                            thread.start();
                        }
                    });
                } else if (msg.what == 124) {
                    mRefreshView.setVisibility(View.GONE);
                    mContentLoaded = true;
                    showContentOrLoadingIndicator(mContentLoaded);
                    String text = msg.getData().getString(JSON_DATA);

                    Gson gson = new Gson();
                    OneTopic topic = gson.fromJson(text, OneTopic.class);
                    //获取本主题的标题，总回复数，所属板块
                    String title = topic.getTitle();
                    String totalReply = topic.getTotalReply();
                    String boardName = topic.getBoard();

                    oneTitle = (TextView) findViewById(R.id.one_title);
                    oneBoard = (TextView) findViewById(R.id.one_board);
                    oneReply = (TextView) findViewById(R.id.one_reply);

                    oneReplyNum = Integer.parseInt(totalReply);

                    oneTitle.setText(title);
                    oneBoard.setText(totalReply);
                    oneReply.setText(boardName);

                    //处理items 每一层楼
                    ArrayList<OneFloor> floors = topic.getItems();
                    new GetPicThread(floors).start();


                } else if(msg.what == 123) {

                    ListView list = (ListView) findViewById(R.id.one_list);
                    TopicAdapter topicAdapter = new TopicAdapter(TopicActivity.this, floorPics);
                    list.setAdapter(topicAdapter);
                }
            }
        };






        //先显示第一页
        viewOnePage();

        prePage = (Button) findViewById(R.id.one_pre_page);
        nextPage = (Button) findViewById(R.id.one_next_page);

        prePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 1) {
                    Toast.makeText(TopicActivity.this, "已是第一页", Toast.LENGTH_SHORT).show();
                } else {
                    currentPage--;
                    viewOnePage();
                }

            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == (oneReplyNum%10 == 0 ? oneReplyNum/10 : oneReplyNum/10 +1)) {
                    Toast.makeText(TopicActivity.this, "已是最后一页", Toast.LENGTH_SHORT).show();
                } else {
                    currentPage++;
                    viewOnePage();
                }

            }
        });
    }




    public void viewOnePage() {
        NetThread netThread = new NetThread();
        netThread.start();
    }

    private void showContentOrLoadingIndicator(boolean contentLoaded) {
        // Decide which view to hide and which to show.
        final View showView = contentLoaded ? mContentView : mLoadingView;
        final View hideView = contentLoaded ? mLoadingView : mContentView;

        // Set the "show" view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);

        // Animate the "show" view to 100% opacity, and clear any animation listener set on
        // the view. Remember that listeners are not limited to the specific animation
        // describes in the chained method calls. Listeners are set on the
        // ViewPropertyAnimator object for the view, which persists across several
        // animations.
        showView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the "hide" view to 0% opacity. After the animation ends, set its visibility
        // to GONE as an optimization step (it won't participate in layout passes, etc.)
        hideView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
    }

    //将图片的url转为bitmap
    class GetPicThread extends Thread {
        private ArrayList<OneFloor> floors;

        public GetPicThread(ArrayList<OneFloor> floors) {
            this.floors = floors;
        }
        @Override
        public void run() {
            floorPics = new ArrayList<>();
            for(int i=0; i<floors.size(); i++) {
                OneFloorPic floorPic = new OneFloorPic();
                floorPic.setAuthor(floors.get(i).getAuthor());
                floorPic.setFloor(floors.get(i).getFloor());
                floorPic.setContent(floors.get(i).getContent());
                ArrayList<String> urls = floors.get(i).getPics();
                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                for(int j=0; j<urls.size();j++) {
                    try {
                        URL url = new URL(urls.get(j));
                        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
                        uc.setConnectTimeout(3500);
                        uc.setReadTimeout(3500);
                        uc.setDoInput(true);
                        uc.connect();
                        InputStream is = uc.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                floorPic.setPics(bitmaps);
                floorPics.add(floorPic);
            }

            Message msg = new Message();
            msg.what = 123;
            handler.sendMessage(msg);
            super.run();
        }
    }

    //定义一个线程类用来联网
    class NetThread extends Thread {
        @Override
        public void run() {
            //必须写prepare
            Looper.prepare();
            String inputLine;
            String data = "";
            Log.e(TAG, "new Thread");

            Properties p = new Properties();
            InputStream input = getResources().openRawResource(R.raw.properties);

            try {
                p.load(input);
                String serverUrl = p.getProperty("url");
                input.close();
                URL url = new URL(serverUrl + "/topic?board=" + topicBoard + "&id=" + topicId +"&p=" + currentPage);
                HttpURLConnection uc = (HttpURLConnection)url.openConnection();
                uc.setConnectTimeout(3500);
                uc.setReadTimeout(3500);
                uc.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                while ((inputLine = reader.readLine()) != null) {
                    Log.e(TAG,inputLine);

                    data += inputLine;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException e" + e.toString());
                Message msg = new Message();
                msg.what = 404;
                handler.sendMessage(msg);
            }
            if(!data.equals("")) {
                Message msg = new Message();
                msg.what = 124;
                Bundle bundle = new Bundle();
                bundle.putString(JSON_DATA, data);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
            Looper.loop();
        }

    }

}
