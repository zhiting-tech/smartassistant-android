package com.yctc.zhiting.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.main.framework.baseutil.UiUtil;
import com.app.main.framework.baseutil.toast.ToastUtil;
import com.app.main.framework.dialog.CommonBaseDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yctc.zhiting.R;
import com.yctc.zhiting.adapter.HomeSelectAdapter;
import com.yctc.zhiting.entity.mine.HomeCompanyBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeSelectDialog extends CommonBaseDialog {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvTip)
    TextView tvTip;
    @BindView(R.id.tvTodo)
    TextView tvTodo;
    @BindView(R.id.rvData)
    RecyclerView rvData;

    private HomeSelectAdapter homeSelectAdapter;

    private List<HomeCompanyBean> data;

    private boolean needCheckBind;
    private boolean canCancel;

    public HomeSelectDialog() {
    }

    public HomeSelectDialog(List<HomeCompanyBean> data) {
        this.data = data;
    }

    public HomeSelectDialog(List<HomeCompanyBean> data, boolean needCheckBind) {
        this.data = data;
        this.needCheckBind = needCheckBind;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.dialog_list_bottom;
    }

    @Override
    protected int obtainWidth() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected int obtainHeight() {
        return UiUtil.getScreenHeight()*2/3;
    }

    @Override
    protected int obtainGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    protected void initArgs(Bundle arguments) {
        canCancel = arguments.getBoolean("canCancel");
    }

    @Override
    protected void initView(View view) {
        setCancelable(canCancel);
        tvTitle.setText(getContext().getResources().getString(R.string.home_switch));
        tvTip.setVisibility(View.GONE);
        tvTodo.setVisibility(View.GONE);

        homeSelectAdapter = new HomeSelectAdapter();
        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(homeSelectAdapter);
        homeSelectAdapter.setNewData(data);

        homeSelectAdapter.setOnItemClickListener((adapter, view1, position) -> {
            HomeCompanyBean homeCompanyBean = homeSelectAdapter.getItem(position);
            if (needCheckBind){
                if (!homeCompanyBean.isIs_bind_sa()){
                    ToastUtil.show(UiUtil.getString(R.string.family_without_intelligent_center));
                    return;
                }
            }
            if (clickItemListener!=null){
                clickItemListener.onItem(homeSelectAdapter.getItem(position));
                for (int i=0; i<homeSelectAdapter.getData().size(); i++){
                    homeSelectAdapter.getData().get(i).setSelected(false);
                }
                homeSelectAdapter.getItem(position).setSelected(true);
                homeSelectAdapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.ivClose)
    void onClickClose(){
        dismiss();
    }

    private OnClickItemListener clickItemListener;

    public OnClickItemListener getClickItemListener() {
        return clickItemListener;
    }

    public void setClickItemListener(OnClickItemListener clickItemListener) {
        this.clickItemListener = clickItemListener;
    }

    public interface OnClickItemListener{
        void onItem(HomeCompanyBean homeCompanyBean);
    }
}
