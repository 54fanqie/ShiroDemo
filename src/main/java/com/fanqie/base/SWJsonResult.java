package com.fanqie.base;

import com.alibaba.fastjson.annotation.JSONField;
import com.stamp.platform.exception.BaseException;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有接口返回的json实例
 *
 * @author mxy
 * @date 2021年10月27日15:15:32
 */
public class SWJsonResult<T> {

    private static final long serialVersionUID = -7532190660864165247L;
    @JSONField
    private boolean success = true;
    @JSONField
    private String code = "0";
    @JSONField
    private String msg = null;
    @JSONField
    private Integer total = Integer.valueOf(0);
    @JSONField
    private List<T> data = new ArrayList();

    public SWJsonResult() {
    }

    public SWJsonResult(BaseException exception) {
        if (exception != null) {
            this.success = false;
            this.code = exception.getErrorCode();
            this.msg = exception.getErrorMsg();
        }

    }

    public SWJsonResult(List<T> data) {
        if (data != null && data.size() > 0) {
            this.data = data;
        }

    }

    public SWJsonResult(T data) {
        if (data != null) {
            this.data.add(data);
        }

    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<T> getData() {
        return this.data;
    }

    public Integer getTotal() {
        return this.total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }


    @Override
    public String toString() {
        return "SWJsonResult{" +
                "success=" + success +
                ", code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", total=" + total +
                ", data=" + data.size() +
                '}';
    }
}
