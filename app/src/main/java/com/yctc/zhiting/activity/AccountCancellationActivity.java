package com.yctc.zhiting.activity;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.app.main.framework.baseutil.LibLoader;
import com.app.main.framework.baseutil.SpConstant;
import com.app.main.framework.baseutil.SpUtil;
import com.app.main.framework.baseutil.UiUtil;
import com.app.main.framework.baseutil.toast.ToastUtil;
import com.app.main.framework.baseview.MVPBaseActivity;
import com.app.main.framework.httputil.NameValuePair;
import com.app.main.framework.httputil.cookie.PersistentCookieStore;
import com.yctc.zhiting.R;
import com.yctc.zhiting.activity.contract.AccountCancellationContract;
import com.yctc.zhiting.activity.presenter.AccountCancellationPresenter;
import com.yctc.zhiting.adapter.ACHomeAdapter;
import com.yctc.zhiting.adapter.ACProductAdapter;
import com.yctc.zhiting.config.Constant;
import com.yctc.zhiting.db.DBManager;
import com.yctc.zhiting.dialog.AccountCancellationDialog;
import com.yctc.zhiting.dialog.RemovedTipsDialog;
import com.yctc.zhiting.entity.ACProductBean;
import com.yctc.zhiting.entity.UnregisterAreasBean;
import com.yctc.zhiting.entity.mine.CaptchaBean;
import com.yctc.zhiting.entity.mine.HomeCompanyBean;
import com.yctc.zhiting.event.HomeSelectedEvent;
import com.yctc.zhiting.event.LogoutEvent;
import com.yctc.zhiting.event.MineUserInfoEvent;
import com.yctc.zhiting.event.RefreshHome;
import com.yctc.zhiting.event.UpdateProfessionStatusEvent;
import com.yctc.zhiting.request.UnregisterUserRequest;
import com.yctc.zhiting.utils.CollectionUtil;
import com.app.main.framework.NetworkErrorConstant;
import com.yctc.zhiting.utils.UserUtils;
import com.yctc.zhiting.websocket.WSocketManager;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * ????????????
 */
public class AccountCancellationActivity extends MVPBaseActivity<AccountCancellationContract.View, AccountCancellationPresenter> implements AccountCancellationContract.View {

    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.rvDel)
    RecyclerView rvDel;
    @BindView(R.id.rvQuit)
    RecyclerView rvQuit;
    @BindView(R.id.rvProduct)
    RecyclerView rvProduct;
    @BindView(R.id.tvDel)
    TextView tvDel;
    @BindView(R.id.tvQuit)
    TextView tvQuit;
    @BindView(R.id.nsv)
    NestedScrollView nsv;
    @BindView(R.id.tvApply)
    TextView tvApply;

    private ACHomeAdapter mDelAdapter;  // ????????????
    private ACHomeAdapter mQuitAdapter; // ????????????
    private ACProductAdapter mACProductAdapter;  // ??????

    private AccountCancellationDialog mAccountCancellationDialog;
    private String captcha_id = ""; // ?????????id

    private DBManager dbManager;
    private WeakReference<Context> mContext;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_cancellation;
    }

    @Override
    protected void initUI() {
        super.initUI();
        setTitleCenter(UiUtil.getString(R.string.mine_account_cancellation));
        mContext = new WeakReference<>(getActivity());
        dbManager = DBManager.getInstance(mContext.get());
        tvAccount.setText(String.format(UiUtil.getString(R.string.mine_account_apply_cancellation), UserUtils.getPhone()));
        initRvDel();
        initRvQuit();
        initRvProduct();
        mPresenter.getAreaList(UserUtils.getCloudUserId());
    }

    /**
     * ????????????????????????
     */
    private void showAccountCancellationDialog() {
        if (mAccountCancellationDialog == null) {
            mAccountCancellationDialog = new AccountCancellationDialog();
            mAccountCancellationDialog.setConfirmListener(new AccountCancellationDialog.OnConfirmListener() {
                /**
                 * ???????????????
                 */
                @Override
                public void getCode() {
                    List<NameValuePair> requestData = new ArrayList<>();
                    requestData.add(new NameValuePair(Constant.TYPE, Constant.UNREGISTER));
                    requestData.add(new NameValuePair(Constant.TARGET, SpUtil.get(SpConstant.PHONE_NUM)));
                    requestData.add(new NameValuePair(Constant.COUNTRY_CODE, SpUtil.get(SpConstant.AREA_CODE)));
                    mPresenter.getCaptcha(requestData);
                }

                /**
                 * ??????
                 *
                 * @param code
                 */
                @Override
                public void onConfirm(String code) {
                    UnregisterUserRequest unregisterUserRequest = new UnregisterUserRequest(code, captcha_id);
                    mPresenter.unregisterUser(UserUtils.getCloudUserId(), unregisterUserRequest);
                    setLoading(true);
                }
            });
        }
        mAccountCancellationDialog.show(this);
    }

    /**
     * ???????????????
     */
    private void initRvDel() {
        mDelAdapter = new ACHomeAdapter();
        rvDel.setLayoutManager(new LinearLayoutManager(this));
        rvDel.setAdapter(mDelAdapter);
    }

    /**
     * ???????????????
     */
    private void initRvQuit() {
        mQuitAdapter = new ACHomeAdapter();
        rvQuit.setLayoutManager(new LinearLayoutManager(this));
        rvQuit.setAdapter(mQuitAdapter);
    }

    /**
     * ????????????
     */
    private void initRvProduct() {
        mACProductAdapter = new ACProductAdapter();
        rvProduct.setLayoutManager(new LinearLayoutManager(this));
        rvProduct.setAdapter(mACProductAdapter);
        mACProductAdapter.setNewData(ACProductBean.getData());
    }

    @OnClick(R.id.tvApply)
    void onClick() {
        showAccountCancellationDialog();
    }

    /**
     * ????????????????????????
     *
     * @param success
     */
    private void showTipDialog(boolean success) {
        // ??????????????? mine_confirm_cancellation_fail
        RemovedTipsDialog removedTipsDialog = new RemovedTipsDialog(UiUtil.getString(success ? R.string.mine_confirm_cancellation_success : R.string.mine_confirm_cancellation_fail));
        removedTipsDialog.setKnowListener(new RemovedTipsDialog.OnKnowListener() {
            @Override
            public void onKnow() {
                if (success) {  // ??????
                    UserUtils.saveUser(null);
                    WSocketManager.getInstance().close();
                    PersistentCookieStore.getInstance().removeAll();
                    SpUtil.put(SpConstant.AREA_CODE, "");
                    SpUtil.put(SpConstant.PHONE_NUM, "");

                    UiUtil.starThread(new Runnable() {
                        @Override
                        public void run() {
                            dbManager.removeVirtualSAFamily();
                            List<HomeCompanyBean> hcList = dbManager.queryHomeCompanyList();
                            if (CollectionUtil.isEmpty(hcList)) {
                                HomeCompanyBean homeCompanyBean = new HomeCompanyBean(1, getResources().getString(R.string.main_my_home));
                                homeCompanyBean.setArea_type(Constant.HOME_MODE);
                                long localId = dbManager.insertHomeCompany(homeCompanyBean, null, false);
                                homeCompanyBean.setLocalId(localId);
                                Constant.CurrentHome = homeCompanyBean;
                            } else {
                                for (HomeCompanyBean homeCompanyBean : hcList) {
                                    dbManager.unbindCloudUser(homeCompanyBean.getLocalId());
                                }
                            }
                            UiUtil.runInMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    Constant.isUnregisterSuccess = true;
                                    EventBus.getDefault().post(new LogoutEvent());
                                    EventBus.getDefault().post(new UpdateProfessionStatusEvent());
                                    //EventBus.getDefault().post(new RefreshHome());
                                    EventBus.getDefault().post(new MineUserInfoEvent(false));
                                    LibLoader.finishAllActivityExcludeCertain(MainActivity.class);
                                }
                            });
                        }
                    });

                } else {  // ??????
                    removedTipsDialog.dismiss();
                }
            }
        });
        removedTipsDialog.show(this);
    }

    /**
     * ?????????/???????????????????????????
     *
     * @param unregisterAreasBean
     */
    @Override
    public void getAreaListSuccess(UnregisterAreasBean unregisterAreasBean) {
        if (unregisterAreasBean != null) {
            List<HomeCompanyBean> areas = unregisterAreasBean.getAreas();
            List<HomeCompanyBean> delAreas = new ArrayList<>();
            List<HomeCompanyBean> quitAreas = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(areas)) {
                for (HomeCompanyBean homeCompanyBean : areas) {
                    if (homeCompanyBean.isIs_owner()) {
                        delAreas.add(homeCompanyBean);
                    } else {
                        quitAreas.add(homeCompanyBean);
                    }
                }
                tvDel.setVisibility(CollectionUtil.isNotEmpty(delAreas) ? View.VISIBLE : View.GONE);
                rvDel.setVisibility(CollectionUtil.isNotEmpty(delAreas) ? View.VISIBLE : View.GONE);
                tvQuit.setVisibility(CollectionUtil.isNotEmpty(quitAreas) ? View.VISIBLE : View.GONE);
                rvQuit.setVisibility(CollectionUtil.isNotEmpty(quitAreas) ? View.VISIBLE : View.GONE);
                mDelAdapter.setNewData(delAreas);
                mQuitAdapter.setNewData(quitAreas);
            }
        }
        nsv.setVisibility(View.VISIBLE);
        tvApply.setVisibility(View.VISIBLE);
    }

    /**
     * ?????????/???????????????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getAreaListFail(int errorCode, String msg) {
        ToastUtil.show(msg);
    }

    /**
     * ???????????????
     *
     * @param captchaBean
     */
    @Override
    public void getCaptchaSuccess(CaptchaBean captchaBean) {
        ToastUtil.show(getResources().getString(R.string.login_sent_successfully));
        if (captchaBean != null) {
            captcha_id = captchaBean.getCaptcha_id();
        }
    }

    /**
     * ???????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getCaptchaFail(int errorCode, String msg) {
        ToastUtil.show(msg);
    }

    /**
     * ????????????
     */
    @Override
    public void unregisterUserSuccess() {
        setLoading(false);
        closeDialog();
        showTipDialog(true);
    }

    /**
     * ????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void unregisterUserFail(int errorCode, String msg) {
        setLoading(false);
        if (errorCode == NetworkErrorConstant.ERROR_VERIFICATION_CODE) {
            ToastUtil.show(msg);
        } else {
            closeDialog();
            showTipDialog(false);
        }
    }

    /**
     * ??????????????????
     * @param isLoading
     */
    private void setLoading(boolean isLoading) {
        if (mAccountCancellationDialog != null) {
            mAccountCancellationDialog.setLoading(isLoading);
        }
    }

    /**
     * ????????????
     */
    private void closeDialog() {
        if (mAccountCancellationDialog != null && !mAccountCancellationDialog.isShowing()) {
            mAccountCancellationDialog.dismiss();
        }
    }
}