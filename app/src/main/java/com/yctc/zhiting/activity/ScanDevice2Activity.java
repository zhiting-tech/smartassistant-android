package com.yctc.zhiting.activity;

import static com.yctc.zhiting.config.Constant.CurrentHome;
import static com.yctc.zhiting.config.Constant.wifiInfo;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.main.framework.baseutil.AndroidUtil;
import com.app.main.framework.baseutil.LogUtil;
import com.app.main.framework.baseutil.SpUtil;
import com.app.main.framework.baseutil.UiUtil;
import com.app.main.framework.baseutil.toast.ToastUtil;
import com.app.main.framework.baseview.MVPBaseActivity;
import com.app.main.framework.fileutil.BaseFileUtil;
import com.app.main.framework.gsonutils.GsonConverter;
import com.app.main.framework.httputil.DownloadFailListener;
import com.app.main.framework.httputil.HTTPCaller;
import com.app.main.framework.httputil.Header;
import com.app.main.framework.httputil.NameValuePair;
import com.app.main.framework.httputil.RequestDataCallback;
import com.app.main.framework.httputil.comfig.HttpConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yctc.zhiting.R;
import com.yctc.zhiting.activity.contract.AddDeviceContract;
import com.yctc.zhiting.activity.presenter.AddDevicePresenter;
import com.yctc.zhiting.adapter.AddDeviceAdapter;
import com.yctc.zhiting.adapter.AddDeviceInCategoryAdapter;
import com.yctc.zhiting.adapter.DeviceCategoryAdapter;
import com.yctc.zhiting.adapter.DeviceFirstCategoryAdapter;
import com.yctc.zhiting.adapter.DeviceSecondCategoryAdapter;
import com.yctc.zhiting.config.Constant;
import com.yctc.zhiting.config.HttpUrlConfig;
import com.yctc.zhiting.config.HttpUrlParams;
import com.yctc.zhiting.dialog.CenterAlertDialog;
import com.yctc.zhiting.entity.home.DeviceMultipleBean;
import com.yctc.zhiting.entity.ws_request.WSAuthParamsBean;
import com.yctc.zhiting.entity.ws_response.EventResponseBean;
import com.yctc.zhiting.entity.ws_response.FindDeviceResultBean;
import com.yctc.zhiting.entity.GetDeviceInfoBean;
import com.yctc.zhiting.entity.ScanDeviceByUDPBean;
import com.yctc.zhiting.entity.ws_response.WSBaseResponseBean;
import com.yctc.zhiting.entity.home.DeviceBean;
import com.yctc.zhiting.entity.home.DeviceTypeBean;
import com.yctc.zhiting.entity.home.DeviceTypeDeviceBean;
import com.yctc.zhiting.entity.home.DeviceTypeListBean;
import com.yctc.zhiting.entity.mine.CheckBindSaBean;
import com.yctc.zhiting.entity.mine.PluginsBean;
import com.yctc.zhiting.entity.scene.PluginDetailBean;
import com.yctc.zhiting.entity.ws_request.WSRequest;
import com.yctc.zhiting.entity.ws_request.WSConstant;
import com.yctc.zhiting.utils.AESUtil;
import com.yctc.zhiting.utils.BluetoothUtil;
import com.yctc.zhiting.utils.ByteConstant;
import com.yctc.zhiting.utils.CollectionUtil;
import com.yctc.zhiting.utils.GpsUtil;
import com.yctc.zhiting.utils.HomeUtil;
import com.yctc.zhiting.utils.IntentConstant;
import com.yctc.zhiting.utils.Md5Util;
import com.yctc.zhiting.utils.SpacesItemDecoration;
import com.yctc.zhiting.utils.StringUtil;
import com.yctc.zhiting.utils.UserUtils;
import com.yctc.zhiting.utils.ZipUtils;
import com.yctc.zhiting.utils.udp.ByteUtil;
import com.yctc.zhiting.utils.udp.UDPSocket;
import com.yctc.zhiting.websocket.IWebSocketListener;
import com.yctc.zhiting.websocket.WSocketManager;
import com.yctc.zhiting.widget.RadarScanView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * ????????????
 */
public class ScanDevice2Activity extends MVPBaseActivity<AddDeviceContract.View, AddDevicePresenter> implements AddDeviceContract.View {

    @BindView(R.id.radarScanView)
    RadarScanView radarScanView;
    @BindView(R.id.ivReport)
    ImageView ivReport;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvTips)
    TextView tvTips;
    @BindView(R.id.tvAgain)
    TextView tvAgain;
    @BindView(R.id.rvDevice)
    RecyclerView rvDevice;
    @BindView(R.id.rvDeviceCategory)
    RecyclerView rvDeviceCategory;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.rvDeviceInCategory)
    RecyclerView rvDeviceInCategory;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.radarScanViewSmall)
    RadarScanView radarScanViewSmall;
    @BindView(R.id.line1)
    View viewLine1;
    @BindView(R.id.line2)
    View viewLine2;
    @BindView(R.id.rvDeviceSecondCategory)
    RecyclerView rvDeviceSecondCategory;

    private static final int REQUEST_PERMISSION = 0x01;
    private boolean again;
    private AddDeviceAdapter addDeviceAdapter;
    private DeviceCategoryAdapter mDeviceCategoryAdapter;
    private AddDeviceInCategoryAdapter mAddDeviceInCategoryAdapter;
    private List<DeviceBean> mScanLists = new ArrayList<>();
    private CountDownTimer mCountDownTimer;

    private long homeId;
    private IWebSocketListener mIWebSocketListener;

    private BluetoothScanCallback mBluetoothScanCallback;  // ??????????????????
    private boolean needLoadBluetooth = false; // ????????????????????????
    private Set<String> blueDeviceAdded; // ??????????????????????????????
    private Set<String> updAddressSet; // ???????????????upd??????
    private ConcurrentHashMap<String, ScanDeviceByUDPBean> scanMap = new ConcurrentHashMap<>();  // ??????udp?????????????????????
    private UDPSocket udpSocket; // ??????sa udpsocket
    private DeviceTypeDeviceBean mDeviceTypeDeviceBean;  // ?????????????????????
    private final String BLUETOOTH_PLUGIN_ID = "zhiting";  // ?????????????????????pluginId

    private CenterAlertDialog alertDialog;

    private String mType; // ????????????
    private long sendId = 0;

    private DeviceFirstCategoryAdapter mDeviceFirstCategoryAdapter;  // ????????????
    private DeviceSecondCategoryAdapter mDeviceSecondCategoryAdapter; // ????????????

    private ConcurrentHashMap<String, WSRequest> requestHashMap = new ConcurrentHashMap<>();

    private CenterAlertDialog gpsTipDialog; // ?????????????????????
    private final int GPS_REQUEST_CODE = 1001;
    private DeviceBean mDeviceBean;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan_device2;
    }

    @Override
    protected void initUI() {
        super.initUI();
        SpUtil.init(this);
        initDeviceRv();
        initRvDeviceCategory();
        initRvDeviceInCategory();
        initRvDeviceSecondCategory();
        initRadarScanView();
        initCountDownTimer();
        initAlertDialog();
        blueDeviceAdded = new HashSet<>();
        updAddressSet = new HashSet<>();
        mBluetoothScanCallback = new BluetoothScanCallback();
        initWebSocket();
        if (UserUtils.isLogin() || HomeUtil.isSAEnvironment()) {
            mPresenter.getDeviceFirstType();
        }
        if (!CurrentHome.isIs_bind_sa()) {
            initUDPScan();
        }
//        initBlueToothScan();
    }

    /**
     * ???????????????????????????
     */
    private void initAlertDialog() {
        alertDialog = CenterAlertDialog.newInstance(UiUtil.getString(R.string.please_add_intelligent_center_or_login_before_adding_device), getResources().getString(R.string.home_cancel), getResources().getString(R.string.home_go_to_login), false);
        alertDialog.setConfirmListener(del -> {
            alertDialog.dismiss();
            switchToActivity(LoginActivity.class);
            finish();
        });
    }

    /**
     * upd????????????
     */
    private void initUDPScan() {
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
        udpSocket.startUDPSocket();
        udpSocket.sendMessage(ByteConstant.SEND_HELLO_DATA, Constant.FIND_DEVICE_URL);
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
                    LogUtil.e("?????????????????????" + Arrays.toString(dealData));
                    LogUtil.e("=========?????????????????????" + dealData.length + "      " + length);
                    String infoJson = AESUtil.decryptAES(dealData, decryptDeviceData, ivEncryptedDeviceData, AESUtil.PKCS7, false);
                    LogUtil.e("???????????????" + infoJson);
                    GetDeviceInfoBean getDeviceInfoBean = new Gson().fromJson(infoJson, GetDeviceInfoBean.class);

                    if (deviceInfoBean == null && getDeviceInfoBean != null && sdb.getId() == getDeviceInfoBean.getId()) {
                        GetDeviceInfoBean.ResultBean gdifb = getDeviceInfoBean.getResult();
                        sdb.setDeviceInfoBean(getDeviceInfoBean);
                        DeviceBean deviceBean = new DeviceBean();
                        deviceBean.setAddress(sdb.getHost());
                        if (gdifb != null) {
                            deviceBean.setPort(gdifb.getPort());
                            String model = gdifb.getModel();
                            if (!TextUtils.isEmpty(model) && model.startsWith(Constant.MH_SA)) { // ?????????sa??????
                                deviceBean.setType(Constant.DeviceType.TYPE_SA);
                                deviceBean.setModel(gdifb.getModel());
                                deviceBean.setName(gdifb.getSa_id());
                                deviceBean.setSa_id(gdifb.getSa_id());
                                deviceBean.setSwVersion(gdifb.getSw_ver());
                                checkIsBind(deviceBean);
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
     * ????????????udp?????????sa??????????????????
     */
    private void checkIsBind(DeviceBean deviceBean) {
        HttpConfig.clearHear(HttpConfig.AREA_ID);
        String url = Constant.HTTP_HEAD + deviceBean.getAddress() + ":" + deviceBean.getPort() + HttpUrlConfig.API + HttpUrlParams.checkBindSa;
        HTTPCaller.getInstance().get(CheckBindSaBean.class, url,
                new RequestDataCallback<CheckBindSaBean>() {
                    @Override
                    public void onSuccess(CheckBindSaBean obj) {
                        super.onSuccess(obj);
                        if (obj != null) {
                            deviceBean.setBind(obj.isIs_bind());
                            mScanLists.add(deviceBean);
                            addDeviceAdapter.notifyDataSetChanged();
                            setHasDeviceStatus();
                        }
                    }

                    @Override
                    public void onFailed(int errorCode, String errorMessage) {
                        super.onFailed(errorCode, errorMessage);
                        LogUtil.e(errorMessage);
                    }
                });
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

    /**
     * ????????????
     */
    private void initBlueToothScan() {
        if (BluetoothUtil.hasBlueTooth()) {
            if (BluetoothUtil.isEnabled()) {
                checkLocationPermission();
            } else {
                String title = getResources().getString(R.string.home_blue_tooth_disabled);
                String tip = getResources().getString(R.string.home_guide_user_to_open_bluetooth);
                CenterAlertDialog alertDialog = CenterAlertDialog.newInstance(
                        title, tip, UiUtil.getString(R.string.home_cancel),
                        UiUtil.getString(R.string.home_setting), false);

                alertDialog.setConfirmListener(del -> {
                    Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    needLoadBluetooth = true;
                    startActivity(intent);
                    alertDialog.dismiss();
                });
                alertDialog.show(this);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int size = permissions.length;
        for (int i = 0; i < size; ++i) {
            String permission = permissions[i];
            int grant = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    scanBluetoothDevice();
                }
            }
        }
    }

    /**
     * ????????????
     */
    private void checkLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
    }

    /**
     * ??????
     */
    private void initCountDownTimer() {
        mCountDownTimer = new CountDownTimer(30_000, 800) {
            @Override
            public void onTick(long millisUntilFinished) {
                LogUtil.e("initCountDownTimer=" + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                LogUtil.e("initCountDownTimer=onFinish=");
                if (mScanLists.size() == 0) {
                    tvAgain.setVisibility(View.VISIBLE);
                    tvStatus.setText(UiUtil.getString(R.string.mine_home_not_find_device));
                    ivReport.setVisibility(View.VISIBLE);
                    radarScanView.stop();
                    radarScanViewSmall.stop();
                    tvAgain.setVisibility(View.VISIBLE);
                    rvDevice.setVisibility(View.GONE);
                }
                BluetoothUtil.stopScanBluetooth(mBluetoothScanCallback);
                if (udpSocket != null) {
                    udpSocket.stopUDPSocket();
                }
            }
        }.start();
    }

    /**
     * WebSocket????????????????????????
     */
    private void initWebSocket() {
        if (Constant.CurrentHome != null && Constant.CurrentHome.isIs_bind_sa()) {//?????????SA
            mIWebSocketListener = new IWebSocketListener() {
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    if (!TextUtils.isEmpty(text)) {
                        LogUtil.e("?????????????????????"+text);
                        WSBaseResponseBean<FindDeviceResultBean> responseBean = GsonConverter.getGson().fromJson(text, new TypeToken<WSBaseResponseBean<FindDeviceResultBean>>() {
                        }.getType());
                        long responseId = responseBean.getId();
                        if (responseBean != null && requestHashMap.containsKey(String.valueOf(responseId)) && responseBean.isSuccess()) {
                            FindDeviceResultBean resultBean = responseBean.getData();
                            if (resultBean != null) {
                                DeviceBean deviceBean = resultBean.getDevice();
                                if (deviceBean != null) {
                                    mScanLists.add(deviceBean);
                                    addDeviceAdapter.notifyDataSetChanged();
                                    setHasDeviceStatus();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                }
            };
            WSocketManager.getInstance().addWebSocketListener(mIWebSocketListener);
            startScanDevice();
        }
    }

    /**
     * ??????????????????
     */
    private void setHasDeviceStatus() {
        if (radarScanViewSmall.getVisibility() == View.GONE) {
            radarScanView.stop();
            radarScanViewSmall.setVisibility(View.VISIBLE);
            radarScanViewSmall.start();
            nestedScrollView.setVisibility(View.GONE);
            tvAgain.setTextColor(UiUtil.getColor(R.color.color_94A5BE));
            tvAgain.setText(getResources().getString(R.string.mine_home_scanning));
            tvAgain.setVisibility(View.VISIBLE);
            rvDevice.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ??????????????????
     */
    private void startScanDevice() {
        Constant.mSendId=Constant.mSendId+1;
        tvStatus.setText(UiUtil.getString(R.string.mine_home_scanning));
        WSRequest wsRequest = new WSRequest();
        wsRequest.setId(Constant.mSendId);
        wsRequest.setService(WSConstant.DISCOVER);
        String findDeviceJson = GsonConverter.getGson().toJson(wsRequest);
        LogUtil.e("???????????????????????????" + findDeviceJson);
        requestHashMap.put(String.valueOf(Constant.mSendId), wsRequest);
        UiUtil.postDelayed(() -> WSocketManager.getInstance().sendMessage(findDeviceJson), 1000);
    }

    /**
     * ???????????????????????????
     */
    private void initRadarScanView() {
        radarScanView.setScanListener(new RadarScanView.OnScanListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        homeId = getIntent().getLongExtra(IntentConstant.ID, -1);
        LogUtil.e("homeId1=" + homeId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GpsUtil.isEnabled(this) && requestCode == GPS_REQUEST_CODE) {
            addHome();
        }
    }

    /**
     * ????????????
     */
    private void initDeviceRv() {
        addDeviceAdapter = new AddDeviceAdapter(mScanLists);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        rvDevice.setAdapter(addDeviceAdapter);
        addDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            DeviceBean bean = mScanLists.get(position);
            if (bean.getType() != null && bean.getType().equalsIgnoreCase(Constant.DeviceType.TYPE_SA)) {
                mDeviceBean = bean;
                if (AndroidUtil.isGE9() && !GpsUtil.isEnabled(this) && wifiInfo != null) {
                    showGpsTipDialog();
                } else {
                    bean.setArea_type(Constant.CurrentHome.getArea_type());
                    addHome();
                }

            } else {//????????????(????????????)
                if (HomeUtil.isSAEnvironment() || UserUtils.isLogin()) {
                    if (bean.getBluetoothDevice() == null) { // ???????????????
                        addDevice(bean);
                    } else {  // ????????????
                        String name = bean.getName();
                        String provisioning = "html/index.html#/h5?mode=bluetooth_softap&bluetooth_name=" + name + "&hotspot_name=" + name;
                        mDeviceTypeDeviceBean = new DeviceTypeDeviceBean(name, name, "", "", provisioning, BLUETOOTH_PLUGIN_ID);
                        mPresenter.getPluginDetail(BLUETOOTH_PLUGIN_ID);
                    }
                } else {
                    if (alertDialog != null && !alertDialog.isShowing()) {
                        alertDialog.show(this);
                    }
                }
            }
        });
    }

    /**
     * ??????????????????/??????SA
     */
    private void addHome() {
        if (mDeviceBean != null) {
            if (mDeviceBean.isBind()) {//???????????????????????????????????????(SA)
                switchToActivity(CaptureNewActivity.class);
            } else {//??????SA
                mDeviceBean.setArea_type(Constant.CurrentHome.getArea_type());
                addDevice(mDeviceBean);
            }
        }
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

    /**
     * ????????????
     */
    private void initRvDeviceCategory() {
        mDeviceCategoryAdapter = new DeviceCategoryAdapter();
        mDeviceFirstCategoryAdapter = new DeviceFirstCategoryAdapter();
        rvDeviceCategory.setLayoutManager(new LinearLayoutManager(this));
        rvDeviceCategory.setAdapter(mDeviceFirstCategoryAdapter);

        mDeviceCategoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            DeviceTypeBean deviceTypeBean = mDeviceCategoryAdapter.getItem(position);
            for (int i = 0; i < mDeviceCategoryAdapter.getData().size(); i++) {
                mDeviceCategoryAdapter.getData().get(i).setSelected(false);
            }
            deviceTypeBean.setSelected(true);
            setSelData(deviceTypeBean);
            mType = deviceTypeBean.getType();
            mDeviceCategoryAdapter.notifyDataSetChanged();
        });
        mDeviceFirstCategoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            for (DeviceTypeBean deviceTypeBean : mDeviceFirstCategoryAdapter.getData()) {
                deviceTypeBean.setSelected(false);
            }
            DeviceTypeBean deviceTypeBean = mDeviceFirstCategoryAdapter.getItem(position);
            deviceTypeBean.setSelected(true);
            mDeviceFirstCategoryAdapter.notifyDataSetChanged();
            getSecondType(deviceTypeBean.getType());
        });
    }

    /**
     * ????????????
     *
     * @param type
     */
    private void getSecondType(String type) {
        NameValuePair nameValuePair = new NameValuePair("type", type);
        List<NameValuePair> typePairs = new ArrayList<>();
        typePairs.add(nameValuePair);
        mPresenter.getDeviceSecondType(typePairs);
    }

    /**
     * ??????????????????
     */
    private void initRvDeviceInCategory() {
        mAddDeviceInCategoryAdapter = new AddDeviceInCategoryAdapter();
        rvDeviceInCategory.setLayoutManager(new GridLayoutManager(this, 3));
        HashMap<String, Integer> spaceValue = new HashMap<>();
        spaceValue.put(SpacesItemDecoration.LEFT_SPACE, 5);
        spaceValue.put(SpacesItemDecoration.TOP_SPACE, 5);
        spaceValue.put(SpacesItemDecoration.RIGHT_SPACE, 5);
        spaceValue.put(SpacesItemDecoration.BOTTOM_SPACE, 5);
        SpacesItemDecoration spacesItemDecoration = new SpacesItemDecoration(spaceValue);
        rvDeviceInCategory.addItemDecoration(spacesItemDecoration);
        rvDeviceInCategory.setAdapter(mAddDeviceInCategoryAdapter);
        mAddDeviceInCategoryAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (HomeUtil.isSAEnvironment() || UserUtils.isLogin()) {
                DeviceTypeDeviceBean deviceTypeDeviceBean = mAddDeviceInCategoryAdapter.getItem(position);
                mDeviceTypeDeviceBean = deviceTypeDeviceBean;
                writeStorageTask();
//                if (!TextUtils.isEmpty(deviceTypeDeviceBean.getPlugin_id())) {
//                    showLoadingDialogInAct();
//                    mPresenter.getPluginDetail(deviceTypeDeviceBean.getPlugin_id());
//                }
            } else {
                if (alertDialog != null && !alertDialog.isShowing()) {
                    alertDialog.show(ScanDevice2Activity.this);
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void initRvDeviceSecondCategory() {
        mDeviceSecondCategoryAdapter = new DeviceSecondCategoryAdapter();
        rvDeviceSecondCategory.setLayoutManager(new LinearLayoutManager(this));
        rvDeviceSecondCategory.setAdapter(mDeviceSecondCategoryAdapter);
        mDeviceSecondCategoryAdapter.setClickDeviceListener((type, item) -> {
            mType = type;
            if (HomeUtil.isSAEnvironment() || UserUtils.isLogin()) {
                mDeviceTypeDeviceBean = item;
                if (!TextUtils.isEmpty(mDeviceTypeDeviceBean.getPlugin_id())) {
                    showLoadingDialogInAct();
                    mPresenter.getPluginDetail(mDeviceTypeDeviceBean.getPlugin_id());
                }
            } else {
                if (alertDialog != null && !alertDialog.isShowing()) {
                    alertDialog.show(ScanDevice2Activity.this);
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void addDevice(DeviceBean bean) {
        Bundle bundle = new Bundle();
        bundle.putLong(IntentConstant.ID, homeId);
        bundle.putSerializable(IntentConstant.BEAN, bean);
        List<WSAuthParamsBean> wsAuthParams = bean.getAuth_params();
        boolean isAuth_required = bean.isAuth_required();
        boolean hasHomekit = false;
        if (isAuth_required && CollectionUtil.isNotEmpty(wsAuthParams)) {
            for (WSAuthParamsBean wsAuthParamsBean : wsAuthParams) {
                String type = wsAuthParamsBean.getType();
                if (!TextUtils.isEmpty(type) && type.equals(Constant.HOMEKIT)) {
                    hasHomekit = true;
                    break;
                }
            }
        }
        // type???homekit???????????????homekit??????
        switchToActivity(hasHomekit ? SetHomeKitActivity.class : DeviceConnectActivity.class, bundle);
    }

    /**
     * ????????????
     */
    @OnClick(R.id.tvAgain)
    void onClickAgain() {
        again = true;
        tvAgain.setVisibility(View.GONE);
        ivReport.setVisibility(View.GONE);
        mCountDownTimer.start();
        radarScanView.start();
        startScanDevice();
        scanBluetoothDevice();
    }

    @OnClick(R.id.ivBack)
    void onClickBack() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (radarScanView != null && radarScanView.getVisibility() == View.VISIBLE)
            radarScanView.start();
        if (radarScanViewSmall != null && radarScanViewSmall.getVisibility() == View.VISIBLE)
            radarScanViewSmall.start();
        if (needLoadBluetooth) {
            checkLocationPermission();
            needLoadBluetooth = false;
        }
    }

    /**
     * ??????????????????
     */
    private void scanBluetoothDevice() {
        BluetoothUtil.startScanBluetooth(mBluetoothScanCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        radarScanView.stop();
        radarScanViewSmall.stop();
        if (!needLoadBluetooth) {
            if (mCountDownTimer != null) {
                mCountDownTimer.onFinish();
                mCountDownTimer.cancel();
            }
            if (udpSocket != null) {
                udpSocket.stopUDPSocket();
            }
            BluetoothUtil.stopScanBluetooth(mBluetoothScanCallback);
        }
        requestHashMap.clear();
        WSocketManager.getInstance().removeWebSocketListener(mIWebSocketListener);
        LogUtil.e("mCountDownTimer=onPause=");
    }

    @Override
    public void checkBindSaSuccess(CheckBindSaBean data) {
        LogUtil.e("checkBindSaSuccess==" + data.isIs_bind());
        initDefaultSA(data.isIs_bind());
    }

    @Override
    public void checkBindSaFail(int errorCode, String msg) {
        LogUtil.e("checkBindSaFail==errorCode=" + errorCode + ",msg=" + msg);
    }

    /**
     * ????????????????????????
     *
     * @param deviceTypeListBean
     */
    @Override
    public void getDeviceTypeSuccess(DeviceTypeListBean deviceTypeListBean) {
        if (deviceTypeListBean != null) {
            List<DeviceTypeBean> types = deviceTypeListBean.getTypes();
            if (CollectionUtil.isNotEmpty(types)) {
                mDeviceCategoryAdapter.setNewData(types);
                DeviceTypeBean deviceTypeBean = types.get(0);
                deviceTypeBean.setSelected(true);
                mType = deviceTypeBean.getType();
                setSelData(deviceTypeBean);
                viewLine1.setVisibility(View.VISIBLE);
                viewLine2.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param deviceTypeBean
     */
    private void setSelData(DeviceTypeBean deviceTypeBean) {
        List<DeviceTypeDeviceBean> devices = deviceTypeBean.getDevices();
        tvName.setText(deviceTypeBean.getName());
        mAddDeviceInCategoryAdapter.setNewData(devices);
    }

    /**
     * ????????????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getDeviceTypeFail(int errorCode, String msg) {
        ToastUtil.show(msg);
    }

    /**
     * ??????????????????
     *
     * @param pluginDetailBean
     */
    @Override
    public void getPluginDetailSuccess(PluginDetailBean pluginDetailBean) {
        if (pluginDetailBean != null) {
            PluginsBean pluginsBean = pluginDetailBean.getPlugin();
            String pluginId = pluginsBean.getId();
            String  pluginFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/plugins/" + pluginId;
            if (pluginsBean != null) {
                String downloadUrl = pluginsBean.getDownload_url();
                String cachePluginJson = SpUtil.get(pluginId);
                PluginsBean cachePlugin = GsonConverter.getGson().fromJson(cachePluginJson, PluginsBean.class);
                String cacheVersion = "";
                if (cachePlugin != null) {
                    cacheVersion = cachePlugin.getVersion();
                }
                String version = pluginsBean.getVersion();
                if (mDeviceTypeDeviceBean != null && BaseFileUtil.isFileExist(pluginFilePath) &&
                        !TextUtils.isEmpty(cacheVersion) && !TextUtils.isEmpty(version) && cacheVersion.equals(version)) {  // ?????????????????????????????????
                    String urlPath = "file://" + pluginFilePath + "/" + mDeviceTypeDeviceBean.getProvisioning();
                    toConfigNetwork(urlPath);
                } else {
                    if (!TextUtils.isEmpty(downloadUrl)) {
                        String suffix = downloadUrl.substring(downloadUrl.lastIndexOf(".") + 1);
                        BaseFileUtil.deleteFolderFile(pluginFilePath, true);
                        String fileZipPath = pluginFilePath + "." + suffix;
                        File file = new File(fileZipPath);
                        BaseFileUtil.deleteFile(file);
                        List<Header> headers = new ArrayList<>();
                        headers.add(new Header("Accept-Encoding", "identity"));
                        headers.add(new Header(HttpConfig.TOKEN_KEY, HomeUtil.getSaToken()));
                        String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/plugins/";
                        String finalPath = path;
                        String finalPluginFilePath = pluginFilePath;
                        HTTPCaller.getInstance().downloadFile(downloadUrl, path, pluginId, headers.toArray(new Header[headers.size()]), UiUtil.getString(R.string.home_download_plugin_package_fail),
                                new ProgressUIListener() {
                                    @Override
                                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                        LogUtil.e("?????????" + percent);
                                        if (percent == 1) {
                                            LogUtil.e("????????????");
                                            try {
                                                ZipUtils.decompressFile(new File(fileZipPath), finalPath, true);
                                                String pluginsBeanToJson = GsonConverter.getGson().toJson(pluginsBean);
                                                SpUtil.put(pluginId, pluginsBeanToJson);
                                                String urlPath = "file://" + finalPluginFilePath + "/" + mDeviceTypeDeviceBean.getProvisioning();
                                                toConfigNetwork(urlPath);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, new DownloadFailListener() {
                                    @Override
                                    public void downloadFailed() {
                                        dismissLoadingDialogInAct();
                                    }
                                });
                    }
                }
            }
        }
    }

    /**
     * ???????????????
     *
     * @param urlPath
     */
    private void toConfigNetwork(String urlPath) {
        dismissLoadingDialogInAct();
        Bundle bundle = new Bundle();
        bundle.putString(IntentConstant.PLUGIN_PATH, urlPath);
        bundle.putString(IntentConstant.TYPE, mType);
        bundle.putSerializable(IntentConstant.BEAN, mDeviceTypeDeviceBean);
        switchToActivity(ConfigNetworkWebActivity.class, bundle);
    }

    /**
     * ??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getPluginDetailFail(int errorCode, String msg) {
        dismissLoadingDialogInAct();
        ToastUtil.show(msg);
    }

    /**
     * ??????????????????
     *
     * @param deviceTypeListBean
     */
    @Override
    public void getDeviceFirstTypeSuccess(DeviceTypeListBean deviceTypeListBean) {
        if (deviceTypeListBean != null) {
            List<DeviceTypeBean> firstCategoryData = deviceTypeListBean.getTypes();
            if (CollectionUtil.isNotEmpty(firstCategoryData)) {
                DeviceTypeBean deviceTypeBean = firstCategoryData.get(0);
                deviceTypeBean.setSelected(true);
                mType = deviceTypeBean.getType();
                getSecondType(mType);
            }
            mDeviceFirstCategoryAdapter.setNewData(firstCategoryData);
        }
    }

    /**
     * ??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getDeviceFirstTypFail(int errorCode, String msg) {
        ToastUtil.show(msg);
    }

    /**
     * ??????????????????
     *
     * @param deviceTypeListBean
     */
    @Override
    public void getDeviceSecondTypeSuccess(DeviceTypeListBean deviceTypeListBean) {
        if (deviceTypeListBean != null) {
            List<DeviceTypeBean> secondCategoryData = deviceTypeListBean.getTypes();
            mDeviceSecondCategoryAdapter.setNewData(secondCategoryData);
        }
    }

    /**
     * ??????????????????
     *
     * @param errorCode
     * @param msg
     */
    @Override
    public void getDeviceSecondTypeFail(int errorCode, String msg) {
        ToastUtil.show(msg);
    }

    private void initDefaultSA(boolean isBind) {
        String json1 = "{\"address\":\"192.168.0.188\", \"port\":\"8088\", \"identity\":\"0x00000000157b4d9c\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"WeiJie'SA\",\"plugin_id\":\"light_001\",\"sw_version\":\"17\",\"type\":\"sa\"}";
        String json2 = "{\"address\":\"192.168.0.112\", \"port\":\"8088\",\"identity\":\"0x00000000157b4d9d\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"MaJian'SA\",\"plugin_id\":\"light_002\",\"sw_version\":\"18\",\"type\":\"sa\"}";
        String json3 = "{\"address\":\"192.168.0.165\", \"port\":\"8089\",\"identity\":\"0x00000000157b4d9e\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"LiHong'SA\",\"plugin_id\":\"light_003\",\"sw_version\":\"19\",\"type\":\"sa\"}";
        String json4 = "{\"address\":\"192.168.22.123\", \"port\":\"9020\",\"identity\":\"0x00000000157b4d9f\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"CeShi'SA\",\"plugin_id\":\"light_004\",\"sw_version\":\"120\",\"type\":\"sa\"}";
        String json5 = "{\"address\":\"192.168.0.84\", \"port\":\"8088\",\"identity\":\"0x00000000157b4d9f\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"CeShi1'SA\",\"plugin_id\":\"light_004\",\"sw_version\":\"120\",\"type\":\"sa\"}";
        String json6 = "{\"address\":\"192.168.0.82\", \"port\":\"8088\",\"identity\":\"0x00000000157b4d9f\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"CeShi2'SA\",\"plugin_id\":\"light_004\",\"sw_version\":\"120\",\"type\":\"sa\"}";
        String json7 = "{\"address\":\"192.168.0.182\", \"port\":\"8088\",\"identity\":\"0x00000000157b4d9e\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"LiHong'SA\",\"plugin_id\":\"light_003\",\"sw_version\":\"19\",\"type\":\"sa\"}";
        String json8 = "{\"address\":\"192.168.22.76\", \"port\":\"8088\",\"identity\":\"0x00000000157b4d9e\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"LiHong'SA\",\"plugin_id\":\"light_003\",\"sw_version\":\"19\",\"type\":\"sa\"}";
        String json9 = "{\"address\":\"192.168.22.86\", \"port\":\"37965\",\"identity\":\"0x00000000157b4d9e\",\"manufacturer\":\"yeelight\",\"model\":\"smart_assistant\",\"name\":\"LiHong'SA\",\"plugin_id\":\"light_003\",\"sw_version\":\"19\",\"type\":\"sa\"}";
        DeviceBean bean1 = GsonConverter.getGson().fromJson(json1, DeviceBean.class);
        DeviceBean bean2 = GsonConverter.getGson().fromJson(json2, DeviceBean.class);
        DeviceBean bean3 = GsonConverter.getGson().fromJson(json3, DeviceBean.class);
        DeviceBean bean4 = GsonConverter.getGson().fromJson(json4, DeviceBean.class);
        DeviceBean bean5 = GsonConverter.getGson().fromJson(json5, DeviceBean.class);
        DeviceBean bean6 = GsonConverter.getGson().fromJson(json6, DeviceBean.class);
        DeviceBean bean7 = GsonConverter.getGson().fromJson(json7, DeviceBean.class);
        DeviceBean bean8 = GsonConverter.getGson().fromJson(json8, DeviceBean.class);
        DeviceBean bean9 = GsonConverter.getGson().fromJson(json9, DeviceBean.class);

        if (HttpUrlConfig.apiSAUrl.contains(bean1.getAddress())) {
            mScanLists.add(bean1);
            bean1.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean2.getAddress())) {
            mScanLists.add(bean2);
            bean2.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean3.getAddress())) {
            mScanLists.add(bean3);
            bean3.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean4.getAddress())) {
            mScanLists.add(bean4);
            bean4.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean5.getAddress())) {
            mScanLists.add(bean5);
            bean5.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean6.getAddress())) {
            mScanLists.add(bean6);
            bean6.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean7.getAddress())) {
            mScanLists.add(bean7);
            bean7.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean8.getAddress())) {
            mScanLists.add(bean8);
            bean8.setBind(isBind);
        } else if (HttpUrlConfig.apiSAUrl.contains(bean9.getAddress())) {
            mScanLists.add(bean9);
            bean9.setBind(isBind);
        }
        addDeviceAdapter.notifyDataSetChanged();
        setHasDeviceStatus();
    }

    @Override
    protected boolean isLoadTitleBar() {
        return false;
    }

    private class BluetoothScanCallback extends ScanCallback {
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onLeScan(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeScan(result);
        }

        private void onLeScan(ScanResult scanResult) {
            if (scanResult != null) {
                BluetoothDevice bluetoothDevice = scanResult.getDevice();
                if (bluetoothDevice != null) {
                    String name = bluetoothDevice.getName();
                    if (TextUtils.isEmpty(name)) {
                        return;
                    }
                    if (!TextUtils.isEmpty(BluetoothUtil.BLUFI_PREFIX)) {
                        if (!name.startsWith(BluetoothUtil.BLUFI_PREFIX)) {
                            return;
                        }
                    }
                    if (blueDeviceAdded.contains(name)) {
                        return;
                    }
                    blueDeviceAdded.add(bluetoothDevice.getName());
                    DeviceBean deviceBean = new DeviceBean();
                    deviceBean.setName(scanResult.getDevice().getName());
                    deviceBean.setBluetoothDevice(bluetoothDevice);
                    mScanLists.add(deviceBean);
                    addDeviceAdapter.notifyDataSetChanged();
                    setHasDeviceStatus();
                }
            }
        }
    }
}