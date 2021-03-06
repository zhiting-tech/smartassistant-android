package com.yctc.zhiting.activity.model;


import com.app.main.framework.httputil.HTTPCaller;
import com.app.main.framework.httputil.NameValuePair;
import com.app.main.framework.httputil.RequestDataCallback;
import com.app.main.framework.httputil.request.Request;
import com.yctc.zhiting.activity.contract.HCDetailContract;
import com.yctc.zhiting.activity.contract.HomeCompanyContract;
import com.yctc.zhiting.config.Constant;
import com.yctc.zhiting.config.HttpUrlConfig;
import com.yctc.zhiting.entity.DepartmentListBean;
import com.yctc.zhiting.entity.ExtensionBean;
import com.yctc.zhiting.entity.FindSATokenBean;
import com.yctc.zhiting.entity.mine.HomeCompanyBean;
import com.yctc.zhiting.entity.mine.HomeCompanyListBean;
import com.yctc.zhiting.entity.mine.IdBean;
import com.yctc.zhiting.entity.mine.InvitationCodeBean;
import com.yctc.zhiting.entity.mine.MemberDetailBean;
import com.yctc.zhiting.entity.mine.MembersBean;
import com.yctc.zhiting.entity.mine.PermissionBean;
import com.yctc.zhiting.entity.mine.RemoveHCBean;
import com.yctc.zhiting.entity.mine.RolesBean;
import com.yctc.zhiting.entity.mine.VerificationCodeBean;
import com.yctc.zhiting.request.AddHCRequest;

import java.util.List;

/**
 * 家庭/公司详情
 */
public class HCDetailModel implements HCDetailContract.Model {
    /**
     * 家庭详情
     *
     * @param id
     * @param callback
     */
    @Override
    public void getDetail(long id, RequestDataCallback<HomeCompanyBean> callback) {
        HTTPCaller.getInstance().get(HomeCompanyBean.class, HttpUrlConfig.getAreaDetailUrl() + "/" + id, callback);
    }

    /**
     * 成员列表
     *
     * @param callback
     */
    @Override
    public void getMembers(RequestDataCallback<MembersBean> callback) {
        HTTPCaller.getInstance().get(MembersBean.class, HttpUrlConfig.getUsers(), callback);
    }

    /**
     * 修改家庭名称
     *
     * @param id
     * @param callback
     */
    @Override
    public void updateName(long id, String name, RequestDataCallback<Object> callback) {
        HTTPCaller.getInstance().put(Object.class, HttpUrlConfig.getAreaDetailUrl() + "/" + id, name, callback);
    }

    /**
     * 获取用户权限
     *
     * @param id
     * @param callback
     */
    @Override
    public void getPermissions(int id, RequestDataCallback<PermissionBean> callback) {
        HTTPCaller.getInstance().get(PermissionBean.class, HttpUrlConfig.getPermissions1(id), callback);
    }

    /**
     * 退出家庭
     *
     * @param id       家庭id
     * @param user_id  用户id
     * @param callback
     */
    @Override
    public void exitHomeCompany(long id, int user_id, RequestDataCallback<Object> callback) {
        HTTPCaller.getInstance().delete(Object.class, HttpUrlConfig.getExitHomeCompany(id, user_id), "", callback);
    }

    /**
     * @param id       家庭id
     * @param callback
     */
    @Override
    public void delHomeCompany(long id, String body, RequestDataCallback<RemoveHCBean> callback) {
        HTTPCaller.getInstance().delete(RemoveHCBean.class, HttpUrlConfig.getDelHomeCompany(id), body, callback);
    }

    /**
     * 角色列表
     *
     * @param callback
     */
    @Override
    public void getRoleList(RequestDataCallback<RolesBean> callback) {
        HTTPCaller.getInstance().get(RolesBean.class, HttpUrlConfig.getRoles(), callback);
    }

    /**
     * 生成邀请码
     *
     * @param id
     * @param body
     * @param callback
     */
    @Override
    public void generateCode(int id, String body, RequestDataCallback<InvitationCodeBean> callback) {
        HTTPCaller.getInstance().post(InvitationCodeBean.class, HttpUrlConfig.getInvitationCode(id), body, callback);
    }

    /**
     * 用户详情
     *
     * @param id
     * @param callback
     */
    @Override
    public void getMemberDetail(int id, RequestDataCallback<MemberDetailBean> callback) {
        HTTPCaller.getInstance().get(MemberDetailBean.class, HttpUrlConfig.getUsers() + "/" + id, callback);
    }

    /**
     * 通过sc找回sa的用户凭证
     *
     * @param userId
     * @param requestData
     * @param callback
     */
    @Override
    public void getSATokenBySC(int userId, List<NameValuePair> requestData, RequestDataCallback<FindSATokenBean> callback) {
        HTTPCaller.getInstance().get(FindSATokenBean.class, HttpUrlConfig.getSAToken(userId) + Constant.ONLY_SC, requestData, callback);
    }

    @Override
    public void addScHome(AddHCRequest addHCRequest, RequestDataCallback<IdBean> callback) {
        HTTPCaller.getInstance().post(IdBean.class, HttpUrlConfig.getScAreasNoHeader() + Constant.ONLY_SC, addHCRequest, callback);
    }

    @Override
    public void getVerificationCode(RequestDataCallback<VerificationCodeBean> callback) {
        HTTPCaller.getInstance().post(VerificationCodeBean.class, HttpUrlConfig.getVerificationCode(), "", callback);
    }

    /**
     * 部门列表
     * @param callback
     */
    @Override
    public void getDepartmentList(RequestDataCallback<DepartmentListBean> callback) {
        HTTPCaller.getInstance().get(DepartmentListBean.class, HttpUrlConfig.getDepartments(),callback);
    }

    /**
     * 扩展列表
     * @param callback
     */
    @Override
    public void getExtensions(RequestDataCallback<ExtensionBean> callback) {
        HTTPCaller.getInstance().get(ExtensionBean.class, HttpUrlConfig.getExtensions(), callback);
    }
}
