package nju.androidchat.client.hw1;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.java.Log;
import nju.androidchat.client.ClientMessage;
import nju.androidchat.client.R;
import nju.androidchat.client.Utils;
import nju.androidchat.client.component.ItemTextReceive;
import nju.androidchat.client.component.ItemTextSend;
import nju.androidchat.client.component.OnRecallMessageRequested;
import nju.androidchat.client.hw1.Hw1Contract;
import nju.androidchat.client.hw1.Hw1TalkModel;
import nju.androidchat.client.hw1.Hw1TalkPresenter;

@Log
public class Hw1TalkActivity extends AppCompatActivity implements nju.androidchat.client.hw1.Hw1Contract.View, TextView.OnEditorActionListener, OnRecallMessageRequested {
    private nju.androidchat.client.hw1.Hw1Contract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nju.androidchat.client.hw1.Hw1TalkModel hw1TalkModel = new Hw1TalkModel();

        // Create the presenter
        this.presenter = new Hw1TalkPresenter(hw1TalkModel, this, new ArrayList<>());
        hw1TalkModel.setIHw1TalkPresenter(this.presenter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showMessageList(List<ClientMessage> messages) {
        runOnUiThread(() -> {
            LinearLayout content = findViewById(R.id.chat_content);

            // 删除所有已有的ItemText
            content.removeAllViews();

            // 增加ItemText
            new Thread(() -> {
                for (ClientMessage message : messages) {
                    String text = String.format("%s", message.getMessage());
                    if (isImage(text)) {
                        String[] str = text.split("!\\[.*\\]");
                        String url = str[str.length - 1];
                        url = text.substring(2, url.length() - 2);
                        String img_html = "<img src ='" + url + "'>";
                        CharSequence CharS;
                        try {
                            CharS = Html.fromHtml(img_html, new Html.ImageGetter() {
                                @Override
                                public Drawable getDrawable(String source) {
                                    Drawable drawable = getOnlineImg(source);
                                    //对图片进行压缩（此处我采用原图）
                                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                                    return drawable;
                                }
                            }, null);
                        } catch (Exception e) {
                            CharS = "图片加载失败，请检查网络连接及url后重试";
                        }
                        CharSequence result = CharS;
                        runOnUiThread(() -> {
                            if (message.getSenderUsername().equals(this.presenter.getUsername())) {
                                content.addView(new ItemTextSend(this, result, message.getMessageId(), this));
                            } else {
                                content.addView(new ItemTextReceive(this, result, message.getMessageId()));
                            }
                        });
                    } else
                    // 如果是自己发的，增加ItemTextSend
                    runOnUiThread(() -> {
                        if (message.getSenderUsername().equals(this.presenter.getUsername())) {
                            content.addView(new ItemTextSend(this, text, message.getMessageId(), this));
                        } else {
                            content.addView(new ItemTextReceive(this, text, message.getMessageId()));
                        }
                    });
                }
            }).start();

            Utils.scrollListToBottom(this);
        });
    }

    @Override
    public void setPresenter(Hw1Contract.Presenter presenter) {
        this.presenter=presenter;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            return hideKeyboard();
        }
        return super.onTouchEvent(event);
    }

    private boolean hideKeyboard() {
        return Utils.hideKeyboard(this);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (Utils.send(actionId, event)) {
            hideKeyboard();
            // 异步地让Controller处理事件
            sendText();
        }
        return false;
    }

    private void sendText() {
        EditText text = findViewById(R.id.et_content);
        AsyncTask.execute(() -> {
            this.presenter.sendMessage(text.getText().toString());
        });
    }

    public void onBtnSendClicked(View v) {
        hideKeyboard();
        sendText();
    }

    private boolean isImage (String text){
        if(text.matches("!\\[.*\\]\\(.*\\)")){
            return true;
        }
        else{
            return false;
        }
    }

    private Drawable getOnlineImg(String url){
        Drawable drawPic = null;
        try {
            URL Myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) Myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();

            InputStream InputS = conn.getInputStream();//获得图片的数据流
            drawPic = Drawable.createFromStream(InputS, "");
            InputS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return text;
        return drawPic;
    }

    // 当用户长按消息，并选择撤回消息时做什么，MVP-0不实现
    @Override
    public void onRecallMessageRequested(UUID messageId) {

    }
}
