package com.yctc.zhiting.fragment;

import static com.yctc.zhiting.config.Constant.CurrentHome;
import static com.yctc.zhiting.config.Constant.wifiInfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.app.main.framework.NetworkErrorConstant;
import com.app.main.framework.baseutil.AndroidUtil;
import com.app.main.framework.baseutil.LogUtil;
import com.app.main.framework.baseutil.NetworkUtil;
import com.app.main.framework.baseutil.SpConstant;
import com.app.main.framework.baseutil.SpUtil;
import com.app.main.framework.baseutil.UiUtil;
import com.app.main.framework.baseutil.toast.ToastUtil;
import com.app.main.framework.baseview.BaseActivity;
import com.app.main.framework.baseview.BaseFragment;
import com.app.main.framework.baseview.MVPBaseFragment;
import com.app.main.framework.config.HttpBaseUrl;
import com.app.main.framework.event.FourZeroFourEvent;
import com.app.main.framework.gsonutils.GsonConverter;
import com.app.main.framework.httputil.NameValuePair;
import com.app.main.framework.httputil.TempChannelUtil;
import com.app.main.framework.httputil.cookie.PersistentCookieStore;
import com.app.main.framework.imageutil.GlideUtil;
import com.app.main.framework.updateapp.AppUpdateHelper;
import com.app.main.framework.widget.StatusBarView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.king.zxing.util.LogUtils;
import com.yctc.zhiting.R;
import com.yctc.zhiting.activity.CaptureNewActivity;
import com.yctc.zhiting.activity.CommonWebActivity;
import com.yctc.zhiting.activity.DepartmentListActivity;
import com.yctc.zhiting.activity.DevicesSortActivity;
import com.yctc.zhiting.activity.FindSAGuideActivity;
import com.yctc.zhiting.activity.RoomAreaActivity;
import com.yctc.zhiting.activity.ScanDevice2Activity;
import com.yctc.zhiting.adapter.HomeFragmentPagerAdapter;
import com.yctc.zhiting.config.Constant;
import com.yctc.zhiting.config.HttpUrlConfig;
import com.yctc.zhiting.db.DBManager;
import com.yctc.zhiting.dialog.CalendarSelectDialog;
import com.yctc.zhiting.dialog.CenterAlertDialog;
import com.yctc.zhiting.dialog.HomeSelectDialog;
import com.yctc.zhiting.dialog.RemovedTipsDialog;
import com.yctc.zhiting.dialog.UpgradeTipDialog;
import com.yctc.zhiting.entity.FindSATokenBean;
import com.yctc.zhiting.entity.home.ApiVersionBean;
import com.yctc.zhiting.entity.home.DeviceMultipleBean;
import com.yctc.zhiting.entity.home.RoomDeviceListBean;
import com.yctc.zhiting.entity.mine.AndroidAppVersionBean;
import com.yctc.zhiting.entity.mine.CheckBindSaBean;
import com.yctc.zhiting.entity.mine.HomeCompanyBean;
import com.yctc.zhiting.entity.mine.LocationBean;
import com.yctc.zhiting.entity.mine.PermissionBean;
import com.yctc.zhiting.entity.mine.RoomListBean;
import com.yctc.zhiting.entity.mine.UpdateUserPost;
import com.yctc.zhiting.entity.mine.UploadFileBean;
import com.yctc.zhiting.event.AfterFindIPEvent;
import com.yctc.zhiting.event.ChangeLayoutModeEvent;
import com.yctc.zhiting.event.DeviceDataEvent;
import com.yctc.zhiting.event.DevicesVisibleEvent;
import com.yctc.zhiting.event.HomeEvent;
import com.yctc.zhiting.event.HomeSelectedEvent;
import com.yctc.zhiting.event.MineUserInfoEvent;
import com.yctc.zhiting.event.PermissionEvent;
import com.yctc.zhiting.event.RefreshHomeEvent;
import com.yctc.zhiting.event.SocketStatusEvent;
import com.yctc.zhiting.fragment.contract.HomeFragmentContract;
import com.yctc.zhiting.fragment.presenter.HomeFragmentPresenter;
import com.yctc.zhiting.popup_window.HomeSetPopupWindow;
import com.yctc.zhiting.receiver.WifiReceiver;
import com.yctc.zhiting.utils.AllRequestUtil;
import com.yctc.zhiting.utils.AnimationUtil;
import com.yctc.zhiting.utils.ChannelUtil;
import com.yctc.zhiting.utils.CollectionUtil;
import com.yctc.zhiting.utils.FastUtil;
import com.yctc.zhiting.utils.FileUtils;
import com.yctc.zhiting.utils.GpsUtil;
import com.yctc.zhiting.utils.HomeUtil;
import com.yctc.zhiting.utils.IntentConstant;
import com.yctc.zhiting.utils.StringUtil;
import com.yctc.zhiting.utils.UserUtils;
import com.yctc.zhiting.websocket.IWebSocketListener;
import com.yctc.zhiting.websocket.WSocketManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;
import okhttp3.WebSocket;

public class HomeFragment2 extends MVPBaseFragment<HomeFragmentContract.View, HomeFragmentPresenter> implements
        HomeFragmentContract.View {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.vpContent)
    ViewPager vpContent;
    @BindView(R.id.sbView)
    StatusBarView sbView;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.tvTips)
    TextView tvTips;
    @BindView(R.id.tvMyHome)
    TextView tvMyHome;
    @BindView(R.id.tvRefresh)
    TextView tvRefresh;
    @BindView(R.id.ivAddDevice)
    ImageView ivAddDevice;
    @BindView(R.id.llTips)
    LinearLayout llTips;
    @BindView(R.id.ivRefresh)
    ImageView ivRefresh;
    @BindView(R.id.ivGo)
    ImageView ivGo;
    @BindView(R.id.ivLayoutMode)
    ImageView ivLayoutMode;
    @BindView(R.id.ivSetUp)
    ImageView ivSetUp;

    private int currentItem;
    public static long homeLocalId;//??????????????????id???
    public static boolean addDeviceP = false;//??????????????????
    private boolean showRemoveDialog = true;

    private DBManager dbManager;
    private WeakReference<Context> mContext;
    private IWebSocketListener mWebSocketListener;
    private List<HomeItemFragment2> fragments = new ArrayList<>();
    public static List<HomeCompanyBean> mHomeList = new ArrayList<>();

    private UpgradeTipDialog upgradeTipDialog;//App????????????
    private UpgradeTipDialog saTipDialog;//SA API????????????

    private String saVersion;
    private String mApkPath;
    private HomeCompanyBean mTempHome;
    private AppUpdateHelper mHelper;//??????apk
    private boolean isHidden;
    private boolean isSetTempHome;//??????????????????????????????
    private boolean isRegisterWifiReceiver = false;//??????????????????wifi?????????
    private boolean isRefreshHome = false;//wifi???????????????????????????????????????

    private CenterAlertDialog gpsTipDialog; // ?????????????????????
    private final int GPS_REQUEST_CODE = 1001;
    private boolean isVisibleOffLineDevices = true;
    private List<DeviceMultipleBean> devicesData = new ArrayList<>();
    private HomeSetPopupWindow homeSetPopupWindow;

    @Override
    protected int getLayoutId() {
        return R.layout.fragmemt_home;
    }

    @Override
    public boolean bindEventBus() {
        return true;
    }

    @Override
    protected void initUI() {
        SpUtil.init(getContext());
        initWebSocket();
        if (hasAccessFineLocationPermission()) {
            registerWifiReceiver(false);
        }
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int colorId = UiUtil.getColor(R.color.white);
            float fraction = Math.abs(verticalOffset * 1.0f) / appBarLayout.getTotalScrollRange();
            int alpha = changeAlpha(colorId, fraction);

            sbView.setBackgroundColor(alpha);
            appBarLayout.setBackgroundColor(alpha);
        });
        ivLayoutMode.setSelected(SpUtil.getBoolean(SpConstant.KEY_HOME_RV_MODE));
    }

    @Override
    protected void initData() {
        super.initData();
        mContext = new WeakReference<>(getActivity());
        dbManager = DBManager.getInstance(mContext.get());
    }

    private void initWebSocket() {
        mWebSocketListener = new IWebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                handleSaConnectStatus();
            }
        };
        WSocketManager.getInstance().addWebSocketListener(mWebSocketListener);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isHidden = hidden;
        if (!hidden) {
            loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden) {
            loadData();
        }
    }

    /**
     * ??????????????????
     */
    private void loadData() {
        if (UserUtils.isLogin()) {
            //????????????>1?????????????????????????????????????????????
            mPresenter.getHomeList();
        } else {
            queryLocalHomeList();
        }
    }

    /**
     * ??????SA&&??????SA??????&&???????????? ??????
     */
    private void handleSaConnectStatus() {
        setTipsRefreshVisible(true);
        if (UserUtils.isLogin()) {
            llTips.setVisibility(View.GONE);
            EventBus.getDefault().post(new SocketStatusEvent(false));
        } else {
            LogUtil.e("HomeUtil.isHomeIdThanZero========" + HomeUtil.isHomeIdThanZero());
            LogUtil.e("HomeUtil.isSAEnvironment========" + HomeUtil.isSAEnvironment());
            boolean isShowStatus = (HomeUtil.isHomeIdThanZero() && !HomeUtil.isSAEnvironment());
            LogUtil.e("HomeUtil.isShowStatus========" + HomeUtil.isSAEnvironment());
            llTips.setVisibility(isShowStatus ? View.VISIBLE : View.GONE);
            EventBus.getDefault().post(new SocketStatusEvent(isShowStatus));
        }
    }

    /**
     * ??????????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RefreshHomeEvent event) {
        loadData();
    }

    /**
     * ????????????????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HomeSelectedEvent event) {
        tvMyHome.setText(CurrentHome.getName());
        LogUtil.e("??????????????????1=" + CurrentHome.getName());
    }

    /**
     * ?????????????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HomeEvent event) {
        mHomeList = dbManager.queryHomeCompanyList();
        HomeCompanyBean tempHome = event.getHomeCompanyBean();
        LogUtil.e(TAG + "onMessageEvent123=" + GsonConverter.getGson().toJson(tempHome));
        if (CollectionUtil.isNotEmpty(mHomeList)) {
            for (HomeCompanyBean home : mHomeList) {
                if (home.getArea_id() == tempHome.getArea_id()) {
                    LogUtil.e(TAG + "setCurrentHome1=" + tempHome.getName());
                    setCurrentHome(tempHome);
                    break;
                }
            }
        }
    }

    /**
     * ????????????????????????
     */
    private void setTipsRefreshVisible(boolean showRefresh) {
        tvTips.setText(showRefresh ? UiUtil.getString(R.string.home_connect_fail) : UiUtil.getString(R.string.home_invalid_token));
        ivRefresh.setVisibility(showRefresh ? View.VISIBLE : View.GONE);
        tvRefresh.setVisibility(showRefresh ? View.VISIBLE : View.GONE);
        ivGo.setVisibility(showRefresh ? View.GONE : View.VISIBLE);
    }

    /**
     * Wifi ??????????????????
     */
    private void registerWifiReceiver(boolean isRefreshData) {
        if (!isRegisterWifiReceiver && mWifiReceiver != null) {
            isRegisterWifiReceiver = true;
            isRefreshHome = isRefreshData;

            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            getActivity().registerReceiver(mWifiReceiver, filter);
        }
    }

    /**
     * Wifi ???????????????
     */
    private final WifiReceiver mWifiReceiver = new WifiReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    LogUtil.e(TAG + "??????????????????");
                    wifiInfo = null;
                    HomeUtil.isInLAN = false;
                    wifiRefreshHome();
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        wifiInfo = wifiManager.getConnectionInfo();
                        wifiRefreshHome();

                        LogUtil.e(TAG + "??????????????????");
                        LogUtil.e(TAG, "??????=???????????????11 " + wifiInfo.getSSID());
                        LogUtil.e(TAG, "??????=???????????????22 " + GsonConverter.getGson().toJson(wifiInfo));
                        LogUtil.e(TAG, "??????=???????????????33 " + wifiInfo.getBSSID());
                        LogUtil.e(TAG, "??????=???????????????44 " + wifiInfo.getMacAddress());
                    }
                }
            }
        }
    };

    /**
     * ??????????????????????????????
     */
    private void wifiRefreshHome() {
        if (!isRefreshHome) {
            isRefreshHome = true;
            return;
        }
        if (FastUtil.isWifiConnectedOverOne()) {
            UiUtil.postDelayed(() -> {
                if (isSetTempHome) {
                    isSetTempHome = false;
                    setCurrentHome(mTempHome);
                } else {
                    loadData();
                }
            }, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GpsUtil.isEnabled(getContext()) && requestCode == GPS_REQUEST_CODE) {
            switchToActivity(CaptureNewActivity.class);
        }
    }

    @OnClick({R.id.ivScan, R.id.ivAddDevice, R.id.tvMyHome, R.id.ivRefresh, R.id.tvRefresh, R.id.llTips, R.id.ivLayoutMode, R.id.ivSetUp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivScan://??????
                checkFineLocationTask(() -> {
                    registerWifiReceiver(false);
                    if (AndroidUtil.isGE9() && !GpsUtil.isEnabled(getContext()) && wifiInfo != null) {
                        showGpsTipDialog();
                    } else {
                        switchToActivity(CaptureNewActivity.class);
                    }
                });
                break;
            case R.id.ivAddDevice://????????????
                checkFineLocationTask(() -> {
                    registerWifiReceiver(false);
                    bundle.putLong(IntentConstant.ID, CurrentHome.getLocalId());
                    HomeFragment2.this.switchToActivity(ScanDevice2Activity.class, bundle);
                });
                break;
            case R.id.tvMyHome://?????????
                showSelectHomeDialog(true);
                break;
            case R.id.ivRefresh:
            case R.id.tvRefresh://??????
                refreshSocketConnect();
                break;
            case R.id.llTips:
                if (HomeUtil.tokenIsInvalid) {
                    switchToActivity(FindSAGuideActivity.class);
                }
                break;
            case R.id.ivLayoutMode:
                ivLayoutMode.setSelected(!ivLayoutMode.isSelected());
                EventBus.getDefault().post(new ChangeLayoutModeEvent(ivLayoutMode.isSelected()));
                SpUtil.put(SpConstant.KEY_HOME_RV_MODE, ivLayoutMode.isSelected());
                break;
            case R.id.ivSetUp:
                showHomeSetPopupWindow();
                break;
        }
    }

    private void showCalendarSelectDialog(){
        CalendarSelectDialog dialog = new CalendarSelectDialog();
        dialog.show(this);
    }


    private void showHomeSetPopupWindow(){
        if (homeSetPopupWindow != null && homeSetPopupWindow.isShowing()) {
            homeSetPopupWindow.dismiss();
        }
        homeSetPopupWindow = new HomeSetPopupWindow((BaseActivity) getContext(),isVisibleOffLineDevices);
        homeSetPopupWindow.setOnItemClickListener(view -> {
            switch (view.getId()){
                case R.id.tvHomeVisible:
                    isVisibleOffLineDevices = view.isSelected();
                    EventBus.getDefault().post(new DeviceDataEvent(filterDevices(devicesData,false)));
                    break;
                case R.id.tvHomeSort:
                    Intent intent1 = new Intent(getContext(), DevicesSortActivity.class);
                    startActivity(intent1);
                    EventBus.getDefault().postSticky(new DeviceDataEvent(devicesData));
                    break;
                case R.id.tvHomeManage:
                    Intent intent2 = new Intent(getContext(), CurrentHome.getArea_type() == 2 ? DepartmentListActivity.class : RoomAreaActivity.class);
                    intent2.putExtra(IntentConstant.IS_BIND_SA, CurrentHome.isIs_bind_sa());
                    intent2.putExtra(IntentConstant.ID, CurrentHome.getLocalId());
                    intent2.putExtra(IntentConstant.CLOUD_ID, CurrentHome.getId());
                    intent2.putExtra(IntentConstant.USER_ID, CurrentHome.getUser_id());
                    intent2.putExtra(IntentConstant.NAME, CurrentHome.getName());
                    intent2.putExtra(IntentConstant.BEAN, CurrentHome);
                    startActivity(intent2);
                    break;
            }
        });
        homeSetPopupWindow.showAsDropDown(ivSetUp, -15, 0);
    }

    /**
     * ???????????????????????????
     */
    private void showSelectHomeDialog(boolean canCancel) {
        if (CollectionUtil.isNotEmpty(mHomeList)) {
            for (HomeCompanyBean home : mHomeList) {
                home.setSelected(home.getLocalId() == CurrentHome.getLocalId() || (home.getArea_id() > 0 && home.getArea_id() == CurrentHome.getArea_id()));
            }
        }
        HomeSelectDialog homeSelectDialog = new HomeSelectDialog(mHomeList);
        Bundle bundle = new Bundle();
        bundle.putBoolean("canCancel", canCancel);
        homeSelectDialog.setArguments(bundle);
        homeSelectDialog.setClickItemListener(homeCompanyBean -> {
            LogUtil.e(TAG + "setCurrentHome3=" + homeCompanyBean.getName());
            HomeUtil.isInLAN = false;
            setCurrentHome(homeCompanyBean);
            homeSelectDialog.dismiss();
        });
        homeSelectDialog.show(this);
    }

    /**
     * ??????socket????????????
     */
    private void refreshSocketConnect() {
        AnimationUtil.rotationAnim(ivRefresh, 500, R.drawable.icon_scene_refreshing, R.drawable.icon_scene_refresh);
        if (HomeUtil.isBindSA()) WSocketManager.getInstance().start();
    }

    private void initTabLayout(List<LocationBean> titles) {
        LogUtil.e("HomeFragment2 --- initTabLayout");
        /*fragments.clear();
        for (int i = 0; i < titles.size(); i++) {
            fragments.add(HomeItemFragment2.getInstance(titles.get(i), i));
        }*/
        initFragment(titles);
        HomeFragmentPagerAdapter pagerAdapter = new HomeFragmentPagerAdapter(getChildFragmentManager(), fragments, titles);
        vpContent.setOffscreenPageLimit(titles.size());
        vpContent.setAdapter(pagerAdapter);

        tabLayout.setupWithViewPager(vpContent, false);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setSelectTab(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setSelectTab(tab.getPosition(), false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setCustomTabIcons(titles);
        currentItem = currentItem<titles.size() ? currentItem : 0;
        setSelectTab(currentItem, true);

        queryLocalDevices();
        //????????????>5 ???????????????????????????
        if (HomeUtil.isSAEnvironment() || UserUtils.isLogin() && !TextUtils.isEmpty(CurrentHome.getSa_id())) {
            mPresenter.getDeviceList(false);
        }
    }

    private void initFragment(List<LocationBean> titles) {
        if(titles.size() > fragments.size()){
            for (int i = fragments.size(); i < titles.size(); i++) {
                fragments.add(HomeItemFragment2.getInstance(titles.get(i), i));
            }
        }else if(titles.size() < fragments.size()){
            List<HomeItemFragment2> fs = new ArrayList<>();
            for (int i = titles.size(); i < fragments.size(); i++) {
                fs.add(fragments.get(i));
            }
            fragments.removeAll(fs);
            fs.clear();
            for (int i = 0; i < titles.size(); i++) {
                fragments.get(i).setLocationBean(titles.get(i));
                fragments.get(i).setmPosition(i);
            }
        }else {
            for (int i = 0; i < titles.size(); i++) {
                fragments.get(i).setLocationBean(titles.get(i));
                fragments.get(i).setmPosition(i);
            }
        }

        LogUtils.d(TAG + " ------ fragments : " + fragments.size());
        LogUtils.d(TAG + " ------ titles : " + titles.size());
    }

    /**
     * @param position
     * @param select
     */
    private void setSelectTab(int position, boolean select) {
        if (tabLayout != null && tabLayout.getTabCount() > 0) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                RelativeLayout view = (RelativeLayout) tab.getCustomView();
                if (view != null) {
                    TextView tvText = view.findViewById(R.id.tvText);
                    View indicator = view.findViewById(R.id.indicator);
                    if (select) {
                        tabLayout.getTabAt(position).select();
                        currentItem = position;
                        indicator.setVisibility(View.VISIBLE);
                        tvText.setTextColor(UiUtil.getColor(R.color.appPurple));
                    } else {
                        indicator.setVisibility(View.INVISIBLE);
                        tvText.setTextColor(UiUtil.getColor(R.color.color_94a5be));
                    }
                }
            }
        }
    }

    /**
     * ?????????TabLayout ????????????
     *
     * @param data
     */
    private void setCustomTabIcons(List<LocationBean> data) {
        for (int i = 0; i < data.size(); i++) {
            RelativeLayout view = (RelativeLayout) UiUtil.inflate(R.layout.item_tablayout);
            TextView tvText = view.findViewById(R.id.tvText);
            tvText.setText(data.get(i).getName());
            tabLayout.getTabAt(i).setCustomView(view);
        }
    }



    /**
     * ????????????????????????
     */
    private void queryLocalHomeList() {
        UiUtil.starThread(() -> {
            List<HomeCompanyBean> homeList = dbManager.queryHomeCompanyList();
            if (CollectionUtil.isEmpty(homeList)) {// ????????????????????????????????????
                HomeCompanyBean homeCompanyBean = new HomeCompanyBean(getResources().getString(R.string.main_my_home));
                homeCompanyBean.setSelected(true);
                homeCompanyBean.setArea_type(Constant.HOME_MODE);
                dbManager.insertHomeCompany(homeCompanyBean, null, false);
                homeList.add(homeCompanyBean);
            }
            handleHomeList(homeList, false);
        });
    }

    /**
     * ????????????????????????
     */
    private void queryLocalRoomList() {
        if (dbManager.isOpen()) {
            UiUtil.starThread(() -> {
                List<LocationBean> roomList = dbManager.queryLocationList(CurrentHome.getLocalId());
                LocationBean defaultRoom = new LocationBean(UiUtil.getString(R.string.home_all));
                roomList.add(0, defaultRoom);
                UiUtil.runInMainThread(() -> {
                    initTabLayout(roomList);
                });
            });
        }
    }

    /**
     * ??????????????????
     *
     * @param homeList      ????????????
     * @param isRefreshData ??????????????????
     */
    private synchronized void handleHomeList(List<HomeCompanyBean> homeList, boolean isRefreshData) {
        if (CollectionUtil.isEmpty(homeList)) return;
        mHomeList.clear();
        mHomeList.addAll(homeList);

        //??????????????????????????????
        if (UserUtils.isLogin() && isRefreshData) {
            //???????????????????????????????????????
            int cloudUserId = UserUtils.getCloudUserId();
            dbManager.removeFamilyNotPresentUserFamily(cloudUserId);

            //??????????????????
            List<HomeCompanyBean> userHomeCompanyList = dbManager.queryHomeCompanyList();
            //????????????????????????????????????
            List<Long> cloudIdList = new ArrayList<>();
            //????????????SA,???????????????????????????
            List<Long> areaIdList = new ArrayList<>();
            for (HomeCompanyBean home : userHomeCompanyList) {
                cloudIdList.add(home.getId());
                if (home.getId() == 0 && home.isIs_bind_sa()) {
                    areaIdList.add(home.getArea_id());
                }
            }

            //?????????????????????????????????ids
            List<Long> serverIdList = new ArrayList<>();
            //???????????????????????????????????????????????????
            for (HomeCompanyBean home : homeList) {
                //?????????????????????id
                home.setCloud_user_id(cloudUserId);
                long homeId = home.getId();
                serverIdList.add(homeId);
                if (areaIdList.contains(homeId)) {
                    dbManager.removeFamilyByAreaId(homeId);
                }
                //??????????????????
                if (cloudIdList.contains(homeId)) {
                    dbManager.updateHomeCompanyByCloudId(home);
                } else {//??????????????????
                    if (home.isIs_bind_sa()) {
                        home.setArea_id(home.getId());
                    }
                    dbManager.insertCloudHomeCompany(home);
                }
            }

            //??????sc??????????????????,????????????????????????????????????????????????????????????????????????????????????
            for (Long id : cloudIdList) {
                if (id > 0 && !serverIdList.contains(id)) {
                    dbManager.removeFamilyByCloudId(id);
                }
            }
            mHomeList = dbManager.queryHomeCompanyList();
        }

        HomeCompanyBean tempHome = mHomeList.get(0);
        if (homeLocalId > 0) {//?????????????????????????????????
            for (HomeCompanyBean homeCompanyBean : mHomeList) {
                if (homeCompanyBean.getLocalId() == homeLocalId) {
                    tempHome = homeCompanyBean;
                    break;
                }
            }
        } else if (wifiInfo != null) {
            for (HomeCompanyBean home : mHomeList) {
                if (home.getBSSID() != null && home.getBSSID().
                        equalsIgnoreCase(wifiInfo.getBSSID()) && home.isIs_bind_sa()) {//??????sa??????
                    tempHome = home;
                    break;
                }
            }
        }
        setCurrentHome(tempHome);
    }

    /**
     * ???????????????????????????
     *
     * @param home
     */
    public void setCurrentHome(HomeCompanyBean home) {
        if (home == null) return;
        if (home.isIs_bind_sa()) {
            if (!hasAccessFineLocationPermission()) {
                mTempHome = home;
                isSetTempHome = true;
                checkFineLocationTask(() -> registerWifiReceiver(true));
                return;
            }
        }

        HomeUtil.tokenIsInvalid = false;

        UiUtil.runInMainThread(() -> {
            CurrentHome = home;
            homeLocalId = home.getLocalId();
            Constant.AREA_TYPE = home.getArea_type();
            tvMyHome.setText(home.getName());

            String saLanAddress = home.getSa_lan_address();
            HttpUrlConfig.baseSAUrl = TextUtils.isEmpty(saLanAddress) ? "" : saLanAddress;
            HttpUrlConfig.apiSAUrl = HttpUrlConfig.baseSAUrl + HttpUrlConfig.API;
            if (!TextUtils.isEmpty(saLanAddress)) {
                TempChannelUtil.baseSAUrl = saLanAddress + HttpUrlConfig.API;
            }

            SpUtil.put(SpConstant.SA_TOKEN, home.getSa_user_token());
            SpUtil.put(SpConstant.AREA_ID, String.valueOf(home.getId()));
            SpUtil.put(SpConstant.IS_BIND_SA, home.isIs_bind_sa());
            SpUtil.put(SpConstant.SA_ID, home.getSa_id());
            EventBus.getDefault().postSticky(new HomeSelectedEvent());

            //??????????????????????????????????????????
            if (!UserUtils.isLogin() && wifiInfo == null) {
                queryLocalRoomList();
            }

            if (TextUtils.isEmpty(home.getSa_id())) { // ?????????????????????
                onCheckSaAddressFailed();
            } else { // ??????????????????SA?????????
                dealData();
            }
        });
    }

    /**
     * ????????????
     */
    private void dealData() {
        //????????????>2??????SA??????????????????,ping sa??????
        //if (CurrentHome.isIs_bind_sa() && TextUtils.isEmpty(CurrentHome.getBSSID()) && !TextUtils.isEmpty(CurrentHome.getSa_lan_address())) {
        if (CurrentHome.isIs_bind_sa() && !TextUtils.isEmpty(CurrentHome.getSa_lan_address()) && NetworkUtil.isWifi()) {
            mPresenter.checkSaAddress();
        } else {
            onCheckSaAddressFailed();
        }
    }

    @Override
    public void onCheckSaAddressSuccess() {
        setMacAddress();
        HomeUtil.isInLAN = true;
        //????????????>3??????????????????
        if (!isDialogShowing()) { // ????????????app???sa??????????????????
            mPresenter.getSACheck(); // ??????SA????????????
        }
    }

    @Override
    public void onCheckSaAddressFailed() {
        //UDP??????SA
        if (CurrentHome.isIs_bind_sa() && TextUtils.isEmpty(CurrentHome.getSa_lan_address())) {
            LogUtil.e(TAG + "FourZeroFourEvent1===");
            EventBus.getDefault().post(new FourZeroFourEvent());
        }

        //????????????>3??????????????????
        getHomeDetail();
    }

    /**
     * ??????????????????
     *
     * @param uploadFileBean
     */
    @Override
    public void uploadAvatarSuccess(UploadFileBean uploadFileBean) {
        LogUtil.e("uploadAvatarSuccess");
        if (uploadFileBean != null) {
            UpdateUserPost updateUserPost = new UpdateUserPost();
            updateUserPost.setAvatar_id(uploadFileBean.getFile_id());
            updateUserPost.setAvatar_url(uploadFileBean.getFile_url());
            String body = new Gson().toJson(updateUserPost);
            mPresenter.updateMember(HomeUtil.getUserId(), body);
        }
    }

    /**
     * ??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void uploadAvatarFail(int errorCode, String msg) {
        LogUtil.e("uploadAvatarFail");
    }

    @Override
    public void updateMemberSuccess() {
        LogUtil.e("updateMemberSuccess");
    }

    @Override
    public void updateMemberFail(int errorCode, String msg) {
        LogUtil.e("updateMemberFail");
    }

    /**
     * ??????SA??????????????????
     *
     * @param checkBindSaBean
     */
    @Override
    public void getSACheckSuccess(CheckBindSaBean checkBindSaBean) {
        if (checkBindSaBean != null) {
            saVersion = checkBindSaBean.getMin_version();
            List<NameValuePair> requestData = new ArrayList<>();
            requestData.add(new NameValuePair(Constant.VERSION, saVersion));
            mPresenter.getSupportApi(requestData);
        }
    }

    /**
     * ??????SA??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getSACheckFail(int errorCode, String msg) {
        getHomeDetail();
    }

    /**
     * ??????SA???????????????Api??????
     *
     * @param apiVersionBean
     */
    @Override
    public void getSupportApiSuccess(ApiVersionBean apiVersionBean) {
        if (apiVersionBean != null) {
            String minApiVersion = apiVersionBean.getMin_api_version();
            if (AndroidUtil.compareVersion(HttpBaseUrl.VERSION_NUM, minApiVersion) < 0) {  // ??????sa??????????????????api??????
                showUpgradeAppDialog();
            } else {
                String presentVersion = AndroidUtil.getAppVersion();
                List<NameValuePair> requestData = new ArrayList<>();
                requestData.add(new NameValuePair(Constant.VERSION, presentVersion));
                requestData.add(new NameValuePair(Constant.CLIENT, Constant.ANDROID));
                mPresenter.getAppSupportApi(requestData);
            }
        } else {
            getHomeDetail();
        }
    }

    /**
     * ??????SA???????????????Api????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getSupportApiFail(int errorCode, String msg) {
        getHomeDetail();
    }

    /**
     * ??????App???????????????Api????????????
     *
     * @param apiVersionBean
     */
    @Override
    public void getAppSupportApiSuccess(ApiVersionBean apiVersionBean) {
        if (apiVersionBean != null) {
            String presentVersion = AndroidUtil.getAppVersion();
            String minVersion = apiVersionBean.getMin_api_version();
            LogUtil.e("presentVersion========" + presentVersion);
            LogUtil.e("minVersion========" + minVersion);
            LogUtil.e("AndroidUtil.compareVersion(presentVersion, minVersion)========" + AndroidUtil.compareVersion(presentVersion, minVersion));
            boolean isUpgrade = AndroidUtil.compareVersion(presentVersion, minVersion) < 0;
            if (isUpgrade) {  // ??????APP???????????????????????????
                showGoProfessionDialog();
            } else {
                getHomeDetail();
            }
        } else {
            getHomeDetail();
        }
    }

    /**
     * ??????App???????????????Api????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getAppSupportApiFail(int errorCode, String msg) {
        getHomeDetail();
    }

    /**
     * app??????????????????
     *
     * @param androidBean
     */
    @Override
    public void getAppVersionInfoSuccess(AndroidAppVersionBean androidBean) {
        if (androidBean != null) {
            String apkUrl = androidBean.getLink();
            if (!TextUtils.isEmpty(apkUrl)) {
                mHelper = new AppUpdateHelper(getContext(), apkUrl);
                mHelper.setonDownLoadListener(new AppUpdateHelper.onDownLoadListener() {
                    @Override
                    public void pending() {
                        upgradeTipDialog.startDownloading();
                    }

                    @Override
                    public void progress(int currentBytes, int totalBytes) {
                        int progress = (int) ((currentBytes / (totalBytes * 1.0)) * 100);
                        upgradeTipDialog.updateProgress(progress);
                    }

                    @Override
                    public void completed(String apkPath) {
                        mApkPath = apkPath;
                        upgradeTipDialog.downloadCompleted();
                    }

                    @Override
                    public void error() {
                        upgradeTipDialog.setError();
                    }
                });
                mHelper.start();
            }
        }
    }

    /**
     * app??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getAppVersionInfoFailed(int errorCode, String msg) {
        ToastUtil.show(msg);
    }

    /**
     * ?????????????????????App
     */
    private void showUpgradeAppDialog() {
        if (upgradeTipDialog == null) {
            String tipTxt = UiUtil.getString(R.string.home_ask_upgrade_app);
            String leftTxt = UiUtil.getString(R.string.home_change_home);
            String rightTxt = UiUtil.getString(R.string.home_upgrade_now);

            upgradeTipDialog = UpgradeTipDialog.getInstance(tipTxt, leftTxt, rightTxt);
            upgradeTipDialog.setClickCallback(new UpgradeTipDialog.OnClickCallback() {
                @Override
                public void onClickLeft() {
                    super.onClickLeft();
                    showSelectHomeDialog(false);
                    upgradeTipDialog.dismiss();
                }

                @Override
                public void onClickRight() {
                    super.onClickRight();
                    List<NameValuePair> requestData = new ArrayList<>();
                    requestData.add(new NameValuePair(Constant.CLIENT, Constant.ANDROID));
                    requestData.add(new NameValuePair(Constant.APP_TYPE, Constant.ZHI_TING));
                    mPresenter.checkAppVersionInfo(requestData);
                }

                @Override
                public void onClickCenter(UpgradeTipDialog.DownloadStatus downloadStatus) {
                    super.onClickCenter(downloadStatus);
                    switch (downloadStatus) {
                        case DOWNLOAD_FINISH:
                            AndroidUtil.installApk(getActivity(), mApkPath);
                            break;

                        case DOWNLOAD_ERROR:
                            if (!NetworkUtil.isNetworkAvailable()) {
                                ToastUtil.show(UiUtil.getString(R.string.home_check_network));
                                return;
                            }
                            if (mHelper != null) {
                                mHelper.start();
                            }
                            break;
                    }
                }

                @Override
                public void onClickCancel() {
                    super.onClickCancel();
                    getActivity().finish();
                }
            });
        }
        if (upgradeTipDialog != null && !upgradeTipDialog.isShowing()) {
            upgradeTipDialog.show(this);
        }
    }

    /**
     * ??????????????????????????????
     */
    private void showGoProfessionDialog() {
        if (saTipDialog == null) {
            String tipTxt = UiUtil.getString(R.string.home_ask_upgrade_sa);
            String leftTxt = UiUtil.getString(R.string.home_change_home);
            String rightTxt = UiUtil.getString(R.string.home_go_to_profession);

            saTipDialog = UpgradeTipDialog.getInstance(tipTxt, leftTxt, rightTxt);
            saTipDialog.setClickCallback(new UpgradeTipDialog.OnClickCallback() {
                @Override
                public void onClickLeft() {
                    showSelectHomeDialog(false);
                    saTipDialog.dismiss();
                }

                @Override
                public void onClickRight() {
                    Bundle proBundle = new Bundle();
                    proBundle.putInt(IntentConstant.WEB_URL_TYPE, 0);
                    switchToActivity(CommonWebActivity.class, proBundle);
                    saTipDialog.dismiss();
                }
            });
        }
        if (saTipDialog != null && !saTipDialog.isShowing()) {
            saTipDialog.show(this);
        }
    }

    /**
     * ???????????????App????????????????????????
     *
     * @return
     */
    private boolean isDialogShowing() {
        if ((upgradeTipDialog != null && upgradeTipDialog.isShowing())
                || (saTipDialog != null && saTipDialog.isShowing())) {
            return true;
        }
        return false;
    }

    /**
     * ??????????????????
     */
    private void getHomeDetail() {
        //????????????>4 ??????????????????????????? ??????&&SA
        if (HomeUtil.isSAEnvironment() || UserUtils.isLogin()) {
            mPresenter.getPermissions(CurrentHome.getUser_id());
        } else if (CurrentHome.getArea_id() == 0) {
            addDeviceP = true;
        } else {
            addDeviceP = false;
        }

        long homeId = CurrentHome.getId() == 0 ? CurrentHome.getArea_id() : CurrentHome.getId();
        if (HomeUtil.isSAEnvironment() || UserUtils.isLogin() && homeId > 0) {
            LogUtil.e("getDetail1");
            mPresenter.getDetail(homeId, false);
        } else {//????????????
            queryLocalRoomList();
        }
    }

    /**
     * ?????????????????????mac_address
     */
    private void setMacAddress() {
        if (CurrentHome != null && wifiInfo != null && hasAccessFineLocationPermission()) {
            String address = StringUtil.getBssid();
            String macAddress = CurrentHome.getBSSID();
            LogUtil.e("macAddress???=============" + macAddress);
            if (TextUtils.isEmpty(macAddress) && !TextUtils.isEmpty(address)) {
                CurrentHome.setBSSID(address);
                dbManager.updateHomeMacAddress(CurrentHome.getLocalId(), address);
            } else if (!macAddress.equals(address)) {
                CurrentHome.setBSSID("");
            }
        }
    }

    /**
     * ??????ip?????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AfterFindIPEvent event) {
        for (HomeCompanyBean homeCompanyBean : mHomeList) {
            boolean isEqualLocalId = (homeCompanyBean.getLocalId() == CurrentHome.getLocalId());
            if (isEqualLocalId) {
                homeCompanyBean.setSa_lan_address(CurrentHome.getSa_lan_address());
                break;
            }
        }
        LogUtil.e(TAG + "setCurrentHome5=" + CurrentHome.getName());
        setCurrentHome(CurrentHome);
    }

    /**
     * ??????????????????
     *
     * @param roomListBean
     */
    @Override
    public void getRoomListSuccess(RoomListBean roomListBean) {
        if (roomListBean != null) {
            //????????????????????????
            List<LocationBean> roomList = new ArrayList<>();
            LocationBean location = new LocationBean(UiUtil.getString(R.string.home_all));
            roomList.add(location);

            List<LocationBean> serverRoomList = null;
            if (CurrentHome.getArea_type() == 1 && CollectionUtil.isNotEmpty(roomListBean.getLocations())) {//??????
                serverRoomList = roomListBean.getLocations();
                roomList.addAll(roomListBean.getLocations());
            } else if (CurrentHome.getArea_type() == 2 && CollectionUtil.isNotEmpty(roomListBean.getDepartments())) {//??????
                serverRoomList = roomListBean.getDepartments();
                roomList.addAll(roomListBean.getDepartments());
            }

            initTabLayout(roomList);
            saveRooms(serverRoomList);
        }
    }

    /**
     * ????????????
     *
     * @param roomListBean
     */
    @Override
    public void getDeviceListSuccess(RoomDeviceListBean roomListBean) {
        LogUtil.e(TAG + "getDeviceListSuccess=0");
        if (roomListBean != null) {
            List<DeviceMultipleBean> deviceList = roomListBean.getDevices();
            LogUtil.e(TAG + "getDeviceListSuccess=1=" + GsonConverter.getGson().toJson(deviceList));
            if (CollectionUtil.isNotEmpty(deviceList)) {
                for (DeviceMultipleBean deviceMultipleBean : deviceList) {
                    deviceMultipleBean.setItemType(DeviceMultipleBean.DEVICE);
                }
            }
            EventBus.getDefault().post(new DeviceDataEvent(filterDevices(deviceList,true)));
            saveDevices(deviceList);
            handleSaConnectStatus();
        }
    }

    //????????????
    private List<DeviceMultipleBean> filterDevices(List<DeviceMultipleBean> deviceList, boolean isResetData) {
        if(isResetData){
            devicesData.clear();
        }
        if(CollectionUtil.isNotEmpty(deviceList)){
            if(isResetData){
                devicesData.addAll(deviceList);
            }
            if(!isVisibleOffLineDevices){
                List<DeviceMultipleBean> devices =  new ArrayList<>();
                for (DeviceMultipleBean multipleBean : deviceList){
                    if(multipleBean.isOnline()){
                        devices.add(multipleBean);
                    }
                }
                return devices;
            }else {
                return deviceList;
            }
        }else {
            return deviceList;
        }
    }

    /**
     * ????????????????????????
     */
    private void queryLocalDevices() {
        if (!TextUtils.isEmpty(CurrentHome.getSa_id())) {
            UiUtil.starThread(() -> {
                List<DeviceMultipleBean> deviceList = dbManager.queryDeviceListByAreaId(CurrentHome.getLocalId());
                UiUtil.runInMainThread(() -> {
                    if (CollectionUtil.isNotEmpty(deviceList)) {
                        for (DeviceMultipleBean deviceMultipleBean : deviceList) {
                            deviceMultipleBean.setItemType(DeviceMultipleBean.DEVICE);
                        }
                    }
                    LogUtil.e("eventbus???????????????"+ 0);
                    EventBus.getDefault().post(new DeviceDataEvent(filterDevices(deviceList,true)));
                    handleSaConnectStatus();
                });
            });
        } else {
            UiUtil.postDelayed(() -> {
                LogUtil.e("eventbus???????????????"+ 1);
                EventBus.getDefault().post(new DeviceDataEvent(filterDevices(null,true)));
                handleSaConnectStatus();
            }, 100);
        }
    }

    /**
     * ????????????
     *
     * @param permissionBean
     */
    @Override
    public void getPermissionsSuccess(PermissionBean permissionBean) {
        if (permissionBean != null) {
            addDeviceP = permissionBean.getPermissions().isAdd_device();
            ivAddDevice.setVisibility(addDeviceP ? View.VISIBLE : View.GONE);
            EventBus.getDefault().post(new PermissionEvent(permissionBean.getPermissions()));
        }
        LogUtil.e(TAG + "getPermissionsSuccess=" + addDeviceP);
    }

    /**
     * ????????????
     */
    @Override
    public void requestFail(int errorCode, String msg) {
    }

    @Override
    public void getRoomListFailed(int errorCode, String msg) {
        queryLocalRoomList();
    }

    /**
     * ????????????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getDeviceFail(int errorCode, String msg) {
        if (errorCode != 5012 && errorCode != 5027) {
            ToastUtil.show(msg);
        } else if (errorCode == 5012 || errorCode == 5027) {
            LogUtil.e("eventbus???????????????"+ 2);
            EventBus.getDefault().post(new DeviceDataEvent(filterDevices(null,true)));
        } else {
            queryLocalDevices();
        }
    }

    /**
     * ????????????
     */
    private void modifyAvatar() {
        UiUtil.starThread(() -> {
            String avatar = SpUtil.get(SpConstant.AVATAR);
            LogUtil.e("???????????????" + avatar);
            if (!TextUtils.isEmpty(avatar)) {

                File file = null;
                if (avatar.startsWith(Constant.HTTP)) {
//                    file = DiskLruCacheWrapper.create(Glide.getPhotoCacheDir(getActivity()), 250 * 1024 * 1024).get(new GlideUrl(avatar));
                    String cachePath = GlideUtil.getImagePathFromCache(avatar);
                    if (cachePath!=null)
                    file = new File(cachePath);
                } else {
                    file = new File(avatar);
                }
                if (file != null && file.isFile()) {
                    LogUtil.e("?????????????????????" + file.getAbsolutePath());
                    String path = file.getAbsolutePath();
                    String hash = "";
                    byte[] fileData = FileUtils.hashV2(path);
                    if (fileData != null) {
                        hash = FileUtils.toHex(fileData);
                        if (!TextUtils.isEmpty(hash)) {
                            List<NameValuePair> uploadSAData = new ArrayList<>();
                            uploadSAData.add(new NameValuePair(Constant.FILE_UPLOAD, path, true));
                            uploadSAData.add(new NameValuePair(Constant.FILE_HASH, hash));
                            uploadSAData.add(new NameValuePair(Constant.FILE_TYPE, Constant.IMG));
                            mPresenter.uploadAvatar(uploadSAData);
                        }
                    }
                }
            }
        });
    }

    /**
     * ????????????
     *
     * @param home
     */
    @Override
    public void getDetailSuccess(HomeCompanyBean home) {
        if (home == null) return;
        modifyAvatar();

        LogUtil.e("??????????????????3=" + CurrentHome.getName());
        //?????????SA???????????????area_id????????????????????????id
        for (HomeCompanyBean hc : mHomeList) {
            boolean isTheSameHome = (home.getId() > 0 && (hc.getId() == home.getId())) || (home.getArea_id() > 0 && (hc.getArea_id() == home.getArea_id()));
            if (isTheSameHome) {
                //????????????????????????????????????
                tvMyHome.setText(home.getName());
                CurrentHome.setName(home.getName());
                hc.setName(home.getName());
                //????????????????????????????????????,?????????????????????
                UiUtil.starThread(() -> dbManager.updateHCNameByToken(CurrentHome.getSa_user_token(), home.getName()));
                break;
            }
        }

        //????????????????????????
        ChannelUtil.refreshHomeTempeChannel();

        //?????????SA && ?????????????????????
        if (UserUtils.isLogin() && CurrentHome.getArea_id() > 0 && CurrentHome.getId() == 0) {
            AllRequestUtil.bindCloudWithoutCreateHome(CurrentHome);
        }

        //????????????>4 ???????????????????????????
        //mPresenter.getPermissions(CurrentHome.getUser_id());
        if (HomeUtil.isSAEnvironment() || UserUtils.isLogin()) {
            mPresenter.getRoomList(CurrentHome.getArea_type(), false);
        } else {//??????????????????
            queryLocalRoomList();
        }
    }

    /**
     * ??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getDetailFail(int errorCode, String msg) {
        if (errorCode == 5012 || errorCode == 5027) {
            if (UserUtils.isLogin()) {//????????????SC??????
                mPresenter.getSAToken(CurrentHome.getCloud_user_id(), requestData);//sc?????????id, sc????????????id
            } else {
                removeLocalFamily();
            }
        } else if (errorCode == 5003) {//???????????????
            removeLocalFamily();
        } else if (errorCode == 100001) {//?????????????????????
            //1.???????????????????????? 2.????????????
            String tokenKey = SpUtil.get(SpConstant.AREA_ID);
            SpUtil.put(tokenKey, "");
            queryLocalRoomList();
        } else {
            queryLocalRoomList();
        }
    }

    /**
     * ??????????????????
     */
    private void removeLocalFamily() {
        HomeUtil.isInLAN = false;
        if (showRemoveDialog) {
            RemovedTipsDialog removedTipsDialog = new RemovedTipsDialog(String.format(UiUtil.getString(R.string.common_remove_home), CurrentHome.getName()));
            removedTipsDialog.setKnowListener(() -> showRemoveDialog = true);
            removedTipsDialog.show(this);
        }
        showRemoveDialog = false;
        dbManager.removeFamily(CurrentHome.getLocalId());
        queryLocalHomeList();
    }

    /**
     * ????????????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getHomeListFail(int errorCode, String msg) {
        if (errorCode == NetworkErrorConstant.LOGIN_INVALID_1 || errorCode == NetworkErrorConstant.LOGIN_INVALID_2) {//???????????????/????????????
            UserUtils.saveUser(null);
            PersistentCookieStore.getInstance().removeAll();
            EventBus.getDefault().post(new MineUserInfoEvent(false));
        } else if (errorCode != NetworkErrorConstant.PWD_MODIFIED) {
            ToastUtil.show(msg);
        }
        queryLocalHomeList();
    }

    /**
     * ????????????????????????
     *
     * @param areas
     */
    @Override
    public void getHomeListSuccess(List<HomeCompanyBean> areas) {
        if (CollectionUtil.isNotEmpty(areas)) {
            UiUtil.starThread(() -> handleHomeList(areas, true));
        } else {
            queryLocalHomeList();
        }
    }

    /**
     * ??????sc??????sa?????????????????????
     *
     * @param findSATokenBean
     */
    @Override
    public void getSATokenSuccess(FindSATokenBean findSATokenBean) {
        if (findSATokenBean != null) {
            String saToken = findSATokenBean.getSa_token();
            if (!TextUtils.isEmpty(saToken)) {
                LogUtil.e("getDetail2");
                HomeUtil.tokenIsInvalid = false;
                CurrentHome.setSa_user_token(saToken);
                mPresenter.getDetail(CurrentHome.getId(), false);
                UiUtil.starThread(() -> dbManager.updateSATokenByLocalId(CurrentHome.getLocalId(), saToken));
            }
        }
    }

    /**
     * ??????sc??????sa????????????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getSATokenFail(int errorCode, String msg) {
        if (errorCode == 2011 || errorCode == 2010) {//??????????????????????????????2011????????????
            HomeUtil.tokenIsInvalid = true;
            setTipsRefreshVisible(false);
            llTips.setVisibility(View.VISIBLE);
            tvTips.setText(getResources().getString(R.string.home_invalid_token));
            LogUtil.e("eventbus???????????????"+ 3);
            EventBus.getDefault().post(new DeviceDataEvent(filterDevices(null,true)));
        } else if (errorCode == 3002) {//?????????3002?????????????????????????????????
            removeLocalFamily();
        } else {
            ToastUtil.show(msg);
        }
    }

    /**
     * ??????????????????
     */
    public void saveRooms(List<LocationBean> roomList) {
        if (CollectionUtil.isEmpty(roomList)) return;

        for (LocationBean locationBean : roomList) {
            locationBean.setLocationId(locationBean.getId());
            locationBean.setSa_user_token(CurrentHome.getSa_user_token());
            locationBean.setArea_id(CurrentHome.getLocalId());
        }
        UiUtil.starThread(() -> {
            dbManager.removeLocationBySaToken(CurrentHome.getSa_user_token());
            dbManager.insertLocationList(CurrentHome.getLocalId(), roomList);
        });
    }

    /**
     * ??????????????????
     */
    public void saveDevices(List<DeviceMultipleBean> deviceList) {
        if (dbManager.isOpen()) {
            UiUtil.starThread(() -> {
                dbManager.removeDeviceByAreaId(CurrentHome.getLocalId());
                dbManager.insertDeviceList(deviceList, CurrentHome.getSa_user_token(), CurrentHome.getLocalId());
            });
        }
    }

    @Override
    public void checkTokenFail(int errorCode, String msg, String homeName) {
        LogUtil.e("checkTokenFail==" + errorCode + ",msg=" + msg);
        if (errorCode == 5003 || errorCode == 3002) {
            RemovedTipsDialog removedTipsDialog = new RemovedTipsDialog(String.format(UiUtil.getString(R.string.common_remove_home), homeName));
            removedTipsDialog.setKnowListener(() -> showRemoveDialog = true);
            removedTipsDialog.show(this);
            UiUtil.starThread(() -> dbManager.removeFamily(CurrentHome.getLocalId()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeLocalId = 0;
        CurrentHome = null;
        HomeUtil.tokenIsInvalid = false;
        unRegisterWifiReceiver();
        if (mWebSocketListener != null) {
            WSocketManager.getInstance().removeWebSocketListener(mWebSocketListener);
        }
    }

    /**
     * Wifi ??????????????????
     */
    public void unRegisterWifiReceiver() {
        if (mWifiReceiver == null || getActivity().isDestroyed() || getActivity().isFinishing())
            return;
        getActivity().unregisterReceiver(mWifiReceiver);
    }

    public void showGpsTipDialog() {
        if (gpsTipDialog == null) {
            String title = UiUtil.getString(R.string.common_tips);
            String tip = UiUtil.getString(R.string.main_need_open_location);
            String cancelStr = UiUtil.getString(R.string.cancel);
            String confirmStr = UiUtil.getString(R.string.confirm);
            gpsTipDialog = CenterAlertDialog.newInstance(title, tip, cancelStr, confirmStr);
            gpsTipDialog.setConfirmListener(del -> {
                Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(gpsIntent, GPS_REQUEST_CODE);
                gpsTipDialog.dismiss();
            });

            gpsTipDialog.setCancelListener(new CenterAlertDialog.OnCancelListener() {
                @Override
                public void onCancel() {
                    switchToActivity(CaptureNewActivity.class);
                }
            });
        }
        if (!gpsTipDialog.isShowing()) {
            gpsTipDialog.show(this);
        }
    }
}
