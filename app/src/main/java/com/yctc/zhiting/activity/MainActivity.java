
package com.yctc.zhiting.activity;

import static com.app.main.framework.gsonutils.GsonConverter.getGson;
import static com.yctc.zhiting.config.Constant.CurrentHome;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.app.main.framework.baseutil.AndroidUtil;
import com.app.main.framework.baseutil.LibLoader;
import com.app.main.framework.baseutil.LogUtil;
import com.app.main.framework.baseutil.SpConstant;
import com.app.main.framework.baseutil.SpUtil;
import com.app.main.framework.baseutil.UiUtil;
import com.app.main.framework.baseutil.toast.ToastUtil;
import com.app.main.framework.baseview.MVPBaseActivity;
import com.app.main.framework.config.HttpBaseUrl;
import com.app.main.framework.event.AccountCancelledEvent;
import com.app.main.framework.event.FourZeroFourEvent;
import com.app.main.framework.event.LoginInvalidEvent;
import com.app.main.framework.event.PwdModifiedEvent;
import com.app.main.framework.httputil.cookie.PersistentCookieStore;
import com.app.main.framework.widget.CustomFragmentTabHost;
import com.yctc.zhiting.R;
import com.yctc.zhiting.activity.contract.MainContract;
import com.yctc.zhiting.activity.presenter.MainPresenter;
import com.yctc.zhiting.application.Application;
import com.yctc.zhiting.config.Constant;
import com.yctc.zhiting.config.HttpUrlConfig;
import com.yctc.zhiting.db.DBManager;
import com.yctc.zhiting.dialog.CenterAlertDialog;
import com.yctc.zhiting.dialog.RemovedTipsDialog;
import com.yctc.zhiting.entity.GetDeviceInfoBean;
import com.yctc.zhiting.entity.ScanDeviceByUDPBean;
import com.yctc.zhiting.entity.mine.HomeCompanyBean;
import com.yctc.zhiting.event.AfterFindIPEvent;
import com.yctc.zhiting.event.LogoutEvent;
import com.yctc.zhiting.event.MineUserInfoEvent;
import com.yctc.zhiting.event.UpdateProfessionStatusEvent;
import com.yctc.zhiting.fragment.HomeFragment2;
import com.yctc.zhiting.fragment.MinFragment;
import com.yctc.zhiting.fragment.SceneFragment;
import com.yctc.zhiting.listener.IHomeView;
import com.yctc.zhiting.listener.IMinView;
import com.yctc.zhiting.listener.ISceneView;
import com.yctc.zhiting.utils.AESUtil;
import com.yctc.zhiting.utils.ByteConstant;
import com.yctc.zhiting.utils.CollectionUtil;
import com.yctc.zhiting.utils.FastUtil;
import com.yctc.zhiting.utils.GpsUtil;
import com.yctc.zhiting.utils.HomeUtil;
import com.yctc.zhiting.utils.IntentConstant;
import com.yctc.zhiting.utils.Md5Util;
import com.yctc.zhiting.utils.StringUtil;
import com.yctc.zhiting.utils.UserUtils;
import com.yctc.zhiting.utils.udp.ByteUtil;
import com.yctc.zhiting.utils.udp.UDPSocket;
import com.yctc.zhiting.websocket.WSocketManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *App??????dev 1.9.1
 */
public class MainActivity extends MVPBaseActivity<MainContract.View, MainPresenter> implements MainContract.View {

    private CustomFragmentTabHost mTabHost;
    private LayoutInflater layoutInflater;
    private final int DEFAULT_TAB = 0;

    private String[] mTextViewArray = new String[]{UiUtil.getString(R.string.main_home), UiUtil.getString(R.string.main_scene), UiUtil.getString(R.string.main_mine)};
    private Class[] fragmentArray = new Class[]{HomeFragment2.class, SceneFragment.class, MinFragment.class};
    private int[] drawableArray = new int[]{R.drawable.tab_home, R.drawable.tab_scene, R.drawable.tab_mine};
    private View[] indexView = new View[fragmentArray.length];
    private IMinView mIMinView;
    private IHomeView mIHomeView;
    private ISceneView mISceneView;

    /**
     * 1 ????????????
     */
    private String type;
    /**
     * ??????????????????????????????
     */
    private String needPermissions;
    /**
     * ?????????app?????????
     */
    private String appName;

    private UDPSocket udpSocket; // ??????sa udpsocket
    private Set<String> updAddressSet; // ???????????????upd??????
    private ConcurrentHashMap<String, ScanDeviceByUDPBean> scanMap = new ConcurrentHashMap<>();  // ??????udp?????????????????????

    private CountDownTimer mCountDownTimer;  // ??????sa?????????????????????

    private long sendId = 0; // ??????upd id

    private CenterAlertDialog gpsTipDialog;
    private final int GPS_REQUEST_CODE = 1001;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        type = intent.getStringExtra(IntentConstant.TYPE);
        updAddressSet = new HashSet<>();
        needPermissions = intent.getStringExtra(IntentConstant.NEED_PERMISSION);
        appName = intent.getStringExtra(IntentConstant.APP_NAME);
        initCountDownTimer();
        initTab(this);
    }

    @Override
    protected boolean isSetStateBar() {
        return false;
    }

    @Override
    protected boolean isLoadTitleBar() {
        return false;
    }

    @Override
    public boolean bindEventBus() {
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTabHost.setCurrentTab(0);
    }

    /**
     * ???????????????????????????
     *
     * @param activity
     */
    private void initTab(MainActivity activity) {
        mTabHost = findViewById(R.id.custom_tabhost);
        layoutInflater = LayoutInflater.from(activity);
        mTabHost.setup(activity, getSupportFragmentManager(), R.id.contentPanel);
        int fragmentCount = fragmentArray.length;
        for (int i = 0; i < fragmentCount; ++i) {
            //????????????Tab????????????????????????????????????
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextViewArray[i]).setIndicator(getTabItemView(i));
            //???Tab???????????????Tab????????????
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            mTabHost.getTabWidget().setDividerDrawable(null);
        }

        mTabHost.setOnTabChangedListener(tabId -> {
            if (mTabHost.getCurrentTab() == 1) {
                if (mISceneView == null) mISceneView = (ISceneView) mTabHost.getCurrentFragment();
                mISceneView.selectTab();
            } else if (mTabHost.getCurrentTab() == 2) {//??????
                if (mIMinView == null) mIMinView = (IMinView) mTabHost.getCurrentFragment();
                mIMinView.selectTab();
            }
            LogUtil.e("mTabHost==" + mTabHost.getCurrentTab());
        });
    }

    /**
     * ??????????????????view
     *
     * @param index
     * @return
     */
    private View getTabItemView(int index) {
        indexView[index] = layoutInflater.inflate(R.layout.home_tab, null);
        TextView title = indexView[index].findViewById(R.id.title);
        ImageView imageView = indexView[index].findViewById(R.id.tab_image);
        title.setText(mTextViewArray[index]);
        imageView.setImageResource(drawableArray[index]);
        return indexView[index];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Application.releaseResource();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        if (udpSocket != null) {
            udpSocket.stopUDPSocket();
        }
    }

    /**
     * ?????????????????????
     */
    public void toAuth() {
        if (!TextUtils.isEmpty(type) && type.equals("1")) {
            Bundle bundle = new Bundle();
            bundle.putString(IntentConstant.NEED_PERMISSION, needPermissions);
            bundle.putString(IntentConstant.APP_NAME, appName);
            switchToActivity(AuthorizeActivity.class, bundle);
        }
    }

    /**
     * ???ip??????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FourZeroFourEvent event) {
        String responseMessage = event.getResponseMessage();
        String url = event.getUrl();
        // ?????????SA?????????responseMessage????????????????????????SA????????????????????????????????????SA????????????
        boolean findBack = TextUtils.isEmpty(responseMessage) || (!TextUtils.isEmpty(url) && url.equals(HttpUrlConfig.baseSAUrl) && responseMessage.equals(Constant.TIMEOUT));
        if (findBack && AndroidUtil.getNetworkType().equals(AndroidUtil.NET_WIFI) && Constant.CurrentHome.isIs_bind_sa()) {  // ?????????SA??????
            startUDPScan();
            mCountDownTimer.start();
        }
    }

    /**
     * ????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginInvalidEvent event) {
        UserUtils.saveUser(null);
        SpUtil.put(SpConstant.PHONE_NUM, "");
        SpUtil.put(SpConstant.AREA_CODE, "");
        PersistentCookieStore.getInstance().removeAll();
        EventBus.getDefault().post(new MineUserInfoEvent(false));
    }

    /**
     * ??????????????????
     *
     * @param event
     */
    private boolean hasPwdModified = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PwdModifiedEvent event) {
        if (!hasPwdModified) {
            UserUtils.saveUser(null);
            PersistentCookieStore.getInstance().removeAll();
            EventBus.getDefault().post(new MineUserInfoEvent(false));
            FragmentActivity activity = (FragmentActivity) LibLoader.getCurrentActivity();
            if (activity != null) {
                RemovedTipsDialog removedTipsDialog = new RemovedTipsDialog(UiUtil.getString(R.string.mine_pwd_modified));
                Bundle bundle = new Bundle();
                bundle.putString("confirmStr", UiUtil.getString(R.string.confirm));
                removedTipsDialog.setArguments(bundle);
                removedTipsDialog.setKnowListener(() -> {
                            hasPwdModified = false;
                            removedTipsDialog.dismiss();
                        }
                );
                removedTipsDialog.show(activity);
                hasPwdModified = true;
            }
        }
    }

    /**
     * upd????????????
     */
    private void startUDPScan() {
        if (udpSocket == null) {
            udpSocket = new UDPSocket(Constant.FIND_DEVICE_URL, Constant.FIND_DEVICE_PORT,
                    new UDPSocket.OnReceiveCallback() {
                        @Override
                        public void onReceiveByteData(String address, int port, byte[] data, int length) {
                            scanDeviceSuccessByUDP(address, port, data, length);
                        }

                        @Override
                        public void onReceive(String msg) {

                        }
                    });
        }
        if (udpSocket != null && !udpSocket.isRunning()) {
            udpSocket.startUDPSocket();
            udpSocket.sendMessage(ByteConstant.SEND_HELLO_DATA, Constant.FIND_DEVICE_URL);
        }
    }

    /**
     * ??????
     */
    private void initCountDownTimer() {
        mCountDownTimer = new CountDownTimer(20_000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                LogUtil.e("initCountDownTimer=" + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                LogUtil.e("initCountDownTimer=onFinish=");
                clearCollection();
            }
        };
    }

    /**
     * ??????udp???????????????
     *
     * @param address
     * @param port
     * @param data
     * @param length
     */
    private void scanDeviceSuccessByUDP(String address, int port, byte[] data, int length) {
        byte[] deviceIdData = Arrays.copyOfRange(data, 6, 12);  // ??????id
        try {
            String deviceId = ByteUtil.bytesToHex(deviceIdData);
            if (scanMap.containsKey(deviceId)) {  // ???????????????hello???????????????
                ScanDeviceByUDPBean sdb = scanMap.get(deviceId);
                String token = sdb.getToken();
                byte[] dealData = Arrays.copyOfRange(data, 32, length);
                if (TextUtils.isEmpty(token)) { // ???????????????token??????
                    String key = sdb.getPassword();
                    String decryptKeyMD5 = Md5Util.getMD5(key);
                    byte[] decryptKeyDta = ByteUtil.md5Str2Byte(decryptKeyMD5);
                    byte[] ivData = ByteUtil.byteMergerAll(decryptKeyDta, key.getBytes());
                    byte[] ivEncryptedData = Md5Util.getMD5(ivData);
                    String tokenFromServer = AESUtil.decryptAES(dealData, decryptKeyDta, ivEncryptedData, AESUtil.PKCS7, true);
                    if (!TextUtils.isEmpty(tokenFromServer)) {
                        sdb.setToken(tokenFromServer);
                        sdb.setId(sendId);
                        String deviceStr = "{\"method\":\"get_prop.info\",\"params\":[],\"id\":" + sendId + "}";  // ?????????????????????
                        sendId++;
                        byte[] bodyData = AESUtil.encryptAES(deviceStr.getBytes(), tokenFromServer, AESUtil.PKCS7); // ????????????????????????????????????
                        int len = bodyData.length + 32;  // ??????
                        byte[] lenData = ByteUtil.intToByte2(len);  // ????????????????????????
                        byte[] headData = ByteConstant.GET_DEV_INFO_HEAD_Data; // ????????????
                        byte[] preData = ByteConstant.GET_DEV_INFO_PRE_Data; // ????????????
                        byte[] serData = ByteConstant.SER_DATA; // ???????????????
                        byte[] tokenData = sdb.getPassword().getBytes();  // ????????????????????????????????????16???????????????
                        byte[] getDeviceInfoData = ByteUtil.byteMergerAll(headData, lenData, preData, deviceIdData, serData, tokenData, bodyData); //  ???????????????????????????
                        LogUtil.e(address + "?????????????????????????????????" + Arrays.toString(getDeviceInfoData));
                        udpSocket.sendMessage(getDeviceInfoData, address);
                    }
                } else { // ?????????token??????
                    GetDeviceInfoBean deviceInfoBean = sdb.getDeviceInfoBean();
                    byte[] decryptDeviceData = Md5Util.getMD5(ByteUtil.md5Str2Byte(token));
                    byte[] ivDeviceData = ByteUtil.byteMergerAll(decryptDeviceData, ByteUtil.md5Str2Byte(token));
                    byte[] ivEncryptedDeviceData = Md5Util.getMD5(ivDeviceData);
                    String infoJson = AESUtil.decryptAES(dealData, decryptDeviceData, ivEncryptedDeviceData, AESUtil.PKCS7, false);
                    LogUtil.e("???????????????" + infoJson);
                    GetDeviceInfoBean getDeviceInfoBean = getGson().fromJson(infoJson, GetDeviceInfoBean.class);

                    if (deviceInfoBean == null && getDeviceInfoBean != null && sdb.getId() == getDeviceInfoBean.getId()) {
                        GetDeviceInfoBean.ResultBean gdifb = getDeviceInfoBean.getResult();
                        sdb.setDeviceInfoBean(getDeviceInfoBean);
                        if (gdifb != null) {
                            String model = gdifb.getModel();
                            if (!TextUtils.isEmpty(model) && model.startsWith(Constant.MH_SA)) { // ?????????sa??????
                                String saId = gdifb.getSa_id();
                                //if (!TextUtils.isEmpty(CurrentHome.getBSSID()) && !TextUtils.isEmpty(CurrentHome.getSa_lan_address())) return;
                                if (!TextUtils.isEmpty(saId) && saId.equals(CurrentHome.getSa_id())) {
                                    String url = Constant.HTTP_HEAD + sdb.getHost() + ":" + gdifb.getPort();
                                    LogUtil.e("MainActivity==udp=" + url);
                                    CurrentHome.setSa_lan_address(url);
                                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    String macAddress = CurrentHome.getBSSID();
                                    if (wifiManager != null) {
                                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                        if (wifiInfo != null && TextUtils.isEmpty(macAddress)) {
                                            macAddress = StringUtil.getBssid();
                                            CurrentHome.setBSSID(macAddress);
                                        }
                                    }
                                    DBManager.getInstance(getApplicationContext()).updateSALanAddressBySAId(saId, url, macAddress);
                                    if (mCountDownTimer != null) {
                                        mCountDownTimer.cancel();
                                    }
                                    clearCollection();
                                    EventBus.getDefault().post(new AfterFindIPEvent());
                                }
                            }
                        }
                    }
                }
            } else {  // ?????????hello???????????????
                ScanDeviceByUDPBean scanDeviceByUDPBean = new ScanDeviceByUDPBean(address, port, deviceId);
                String password = StringUtil.getUUid().substring(0, 16);
                scanDeviceByUDPBean.setPassword(password);
                scanMap.put(deviceId, scanDeviceByUDPBean);
                getDeviceToken(address, data, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????????????????
     */
    private void clearCollection() {
        if (udpSocket != null && udpSocket.isRunning()) {
            udpSocket.stopUDPSocket();
        }
        scanMap.clear();
        updAddressSet.clear();
    }

    /**
     * ????????????token
     *
     * @param address
     * @param data
     */
    private void getDeviceToken(String address, byte[] data, String password) {
        if (!updAddressSet.contains(address)) {
            updAddressSet.add(address);
            byte[] tokenHeadData = ByteConstant.TOKEN_HEAD_DATA; // ????????????????????????????????????
            byte[] deviceIdData = Arrays.copyOfRange(data, 6, 12);  // ??????id
            byte[] passwordData = password.getBytes();  // ??????
            byte[] serData = ByteConstant.SER_DATA; // ????????? ??????
            byte[] tokenData = ByteUtil.byteMergerAll(tokenHeadData, deviceIdData, serData, passwordData);  // ????????????token?????????
            udpSocket.sendMessage(tokenData, address);
        }
    }

    @Override
    public void onBackPressed() {
        if (mTabHost.getCurrentTab() == DEFAULT_TAB) {
            if (FastUtil.isDoubleClick()) {
                super.onBackPressed();
            } else {
                ToastUtil.show(UiUtil.getString(R.string.main_exit_tip));
            }
        } else {
            mTabHost.setCurrentTab(DEFAULT_TAB);
        }
    }

    /**
     * ?????????????????????
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AccountCancelledEvent event) {
        accountCancelled();
        ToastUtil.show(UiUtil.getString(R.string.main_account_cancelled));
    }

    /**
     *  ???????????????
     */
    private void accountCancelled() {
        UserUtils.saveUser(null);
        WSocketManager.getInstance().close();
        PersistentCookieStore.getInstance().removeAll();
        SpUtil.put(SpConstant.AREA_CODE, "");
        SpUtil.put(SpConstant.PHONE_NUM, "");

        UiUtil.starThread(new Runnable() {
            @Override
            public void run() {
                DBManager.getInstance(getApplicationContext()).removeVirtualSAFamily();
                List<HomeCompanyBean> hcList =  DBManager.getInstance(getApplicationContext()).queryHomeCompanyList();
                if (CollectionUtil.isEmpty(hcList)) {
                    HomeCompanyBean homeCompanyBean = new HomeCompanyBean(1, getResources().getString(R.string.main_my_home));
                    homeCompanyBean.setArea_type(Constant.HOME_MODE);
                    long localId =  DBManager.getInstance(getApplicationContext()).insertHomeCompany(homeCompanyBean, null, false);
                    homeCompanyBean.setLocalId(localId);
                    Constant.CurrentHome = homeCompanyBean;
                } else {
                    for (HomeCompanyBean homeCompanyBean : hcList) {
                        DBManager.getInstance(getApplicationContext()).unbindCloudUser(homeCompanyBean.getLocalId());
                    }
                }
                UiUtil.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Constant.isUnregisterSuccess = true;
                        EventBus.getDefault().post(new LogoutEvent());
                        EventBus.getDefault().post(new UpdateProfessionStatusEvent());
                        EventBus.getDefault().post(new MineUserInfoEvent(false));
                        LibLoader.finishAllActivityExcludeCertain(MainActivity.class);
                        ToastUtil.show(UiUtil.getString(R.string.main_account_cancelled));
                    }
                });
            }
        });
    }
}
