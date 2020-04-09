package com.smartism.znzk.zhicheng.tasks;

import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.zhicheng.iviews.ILoadZhujiAndDeviceOperator;

import java.util.List;

public class GetZhujiAndDeviceOperator implements ILoadZhujiAndDeviceOperator {
    @Override
    public List<DeviceInfo> loadDeviceInfosByZhujiId(long zhujiId) {
        return DatabaseOperator.getInstance().queryAllDeviceInfos(zhujiId);
    }

    @Override
    public DeviceInfo loadDeviceInfoByDeviceId(long deviceId) {
        return DatabaseOperator.getInstance().queryDeviceInfo(deviceId);
    }

    @Override
    public ZhujiInfo loadZhujiByZhujiId(long zhujiId) {
        return DatabaseOperator.getInstance().queryDeviceZhuJiInfo(zhujiId);
    }

    @Override
    public List<ZhujiInfo> loadZhujis() {
        return DatabaseOperator.getInstance().queryAllZhuJiInfos();
    }

    @Override
    public ZhujiInfo loadZhujiByMasterId(String masterId) {
        return DatabaseOperator.getInstance().queryDeviceZhuJiInfo(masterId);
    }

    @Override
    public List<CommandInfo> loadCommandInfo(long did) {
        return DatabaseOperator.getInstance().queryAllCommands(did);
    }
}
