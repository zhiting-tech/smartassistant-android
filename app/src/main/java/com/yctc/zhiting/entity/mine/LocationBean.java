package com.yctc.zhiting.entity.mine;

import java.io.Serializable;

public class LocationBean implements Serializable {
    /**
     * id : 1
     * name : 客厅
     * sort : 1
     */

    private int id;
    private String name;
    private int sort;
    private long area_id;
    private String sa_user_token;
    private int locationId;
    private boolean select;//是否选中

    private int user_count; // 人员数量(公司部门才有)
    private boolean is_manger; // (公司部门才有)

    public boolean isCheck() {
        return select;
    }

    public void setCheck(boolean select) {
        this.select = select;
    }

    public LocationBean() {
    }

    public LocationBean(String name) {
        this.name = name;
    }

    public LocationBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public LocationBean(int id, String name, int sort) {
        this.id = id;
        this.name = name;
        this.sort = sort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getArea_id() {
        return area_id;
    }

    public void setArea_id(long area_id) {
        this.area_id = area_id;
    }

    public String getSa_user_token() {
        return sa_user_token;
    }

    public void setSa_user_token(String sa_user_token) {
        this.sa_user_token = sa_user_token;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getUser_count() {
        return user_count;
    }

    public void setUser_count(int user_count) {
        this.user_count = user_count;
    }

    public boolean isIs_manger() {
        return is_manger;
    }

    public void setIs_manger(boolean is_manger) {
        this.is_manger = is_manger;
    }
}
