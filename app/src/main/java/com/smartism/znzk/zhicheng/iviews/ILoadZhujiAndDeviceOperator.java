package com.smartism.znzk.zhicheng.iviews;

import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;

import java.util.List;

//从数据库加载主机与设备
public interface ILoadZhujiAndDeviceOperator {
    List<DeviceInfo> loadDeviceInfosByZhujiId(long zhujiId);

    DeviceInfo loadDeviceInfoByDeviceId(long deviceId);

    ZhujiInfo loadZhujiByZhujiId(long zhujiId);

    List<ZhujiInfo> loadZhujis();

    ZhujiInfo loadZhujiByMasterId(String masterId);

    List<CommandInfo> loadCommandInfo(long did);
}
