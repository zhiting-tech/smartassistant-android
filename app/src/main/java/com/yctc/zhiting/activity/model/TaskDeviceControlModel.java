package com.yctc.zhiting.activity.model;


import com.app.main.framework.httputil.HTTPCaller;
import com.app.main.framework.httputil.RequestDataCallback;
import com.yctc.zhiting.activity.contract.SceneDeviceStatusControlContract;
import com.yctc.zhiting.activity.contract.TaskDeviceControlContract;
import com.yctc.zhiting.bean.DeviceDetailBean;
import com.yctc.zhiting.config.HttpUrlConfig;

/**
 * 任务设备控制
 */
public class TaskDeviceControlModel implements TaskDeviceControlContract.Model {
    @Override
    public void getDeviceDetail(int id, RequestDataCallback<DeviceDetailBean> callback) {
        HTTPCaller.getInstance().get(DeviceDetailBean.class, HttpUrlConfig.getAddDeviceUrl()+"/"+id, callback);
    }
}
