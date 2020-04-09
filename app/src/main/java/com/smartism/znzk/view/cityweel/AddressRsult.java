package com.smartism.znzk.view.cityweel;

import com.smartism.znzk.domain.AreaInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/2/22.
 */

interface AddressResult{
    /**
     * 洲、国、省、市、区
     * @param areaInfoList
     * @param id
     */
    public void result(List<AreaInfo> areaInfoList, int id);
}