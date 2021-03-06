package com.yctc.zhiting.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.main.framework.baseutil.UiUtil;
import com.app.main.framework.dialog.CommonBaseDialog;
import com.yctc.zhiting.R;

import butterknife.OnClick;

/**
 * 被移除家庭提示
 */
public class RemovedTipsDialog extends CommonBaseDialog {

    private TextView tvContent;
    private TextView tvKnow;

    private String mHomeName;
    private String confirmStr;

    public RemovedTipsDialog(String name) {
        this.mHomeName = name;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.dialog_removed_tips;
    }

    @Override
    protected int obtainWidth() {
        return dp2px(300);
    }

    @Override
    protected int obtainHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    @Override
    protected int obtainGravity() {
        return Gravity.CENTER;
    }

    @Override
    protected void initArgs(Bundle arguments) {
        confirmStr = arguments.getString("confirmStr");
    }

    @Override
    protected void initView(View view) {
        setCancelable(false);
        tvContent = view.findViewById(R.id.tvContent);
        tvKnow = view.findViewById(R.id.tvKnow);
        tvContent.setText(mHomeName);
        tvKnow.setText(TextUtils.isEmpty(confirmStr) ? UiUtil.getString(R.string.common_know) : confirmStr);
    }

    @OnClick(R.id.tvKnow)
    void onClickKnow() {
        dismiss();
        if (knowListener!=null){
            knowListener.onKnow();
        }
    }

    private OnKnowListener knowListener;

    public OnKnowListener getKnowListener() {
        return knowListener;
    }

    public void setKnowListener(OnKnowListener knowListener) {
        this.knowListener = knowListener;
    }

    public interface OnKnowListener{
        void onKnow();
    }
}
