package org.codechimp.apprater;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class RatingDialog extends Dialog {

    private ImageView img_back;
    private RatingBar ratingBar;
    private TextView textView;
    private Button encourage;

    public float stars = 0;

    public interface OnDialogClickListener{
        void onEncourageButtonClickListener();
    }

    private OnDialogClickListener onDialogClickListener;

    public RatingDialog(Context context) {
        super(context, R.style.Dialog);
    }

    public RatingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RatingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_rate);

        initView();

        //设置外部点击Dialog不消失
        setCanceledOnTouchOutside(false);
    }

    public void initView(){
        img_back = (ImageView) findViewById(R.id.close);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        textView = (TextView) findViewById(R.id.textview);
        encourage = (Button) findViewById(R.id.encourage_button);
        //点击右上角关闭Dialog
        img_back = (ImageView)findViewById(R.id.close);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingDialog.this.dismiss();
            }
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //第一个参数，当前评分修改的 ratingBar
                //第二个参数，当前评分分手，范围 0~星星数量
                //第三个参数，如果评分改变是由用户触摸手势或方向键轨迹球移动触发的，则返回true
                textView.setVisibility(View.INVISIBLE);
                encourage.setVisibility(View.VISIBLE);
                stars = rating;
            }
        });
        encourage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDialogClickListener != null) {
                    onDialogClickListener.onEncourageButtonClickListener();
                }
            }
        });
    }

    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener){
        this.onDialogClickListener = onDialogClickListener;
    }

}
