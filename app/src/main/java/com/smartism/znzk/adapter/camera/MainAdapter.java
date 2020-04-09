package com.smartism.znzk.adapter.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.macrovideo.sdk.defines.ResultCode;
import com.macrovideo.sdk.setting.AlarmAndPromptInfo;
import com.macrovideo.sdk.setting.DeviceAlarmAndPromptSetting;
import com.macrovideo.sdk.tools.DeviceScanner;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.AddContactNextActivity;
import com.smartism.znzk.activity.camera.ModifyContactActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.db.camera.SharedPreferencesManager;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.LocalDevice;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.NormalDialog;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 摄像头列表 adapter适配器
 *
 * @author update by 王建 2016年08月19日
 */
public class MainAdapter extends BaseAdapter {
    Context context;
    NormalDialog dialogProgress;
    private String result;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (dialogProgress != null) {
                        dialogProgress.dismiss();
                        dialogProgress = null;
                    }
                    Contact contact = (Contact) msg.obj;
                    if (FList.getInstance() == null || FList.getInstance().size() <= 0) {
                        Intent createPwdSuccess = new Intent();
                        createPwdSuccess.setAction(Constants.Action.ACTIVITY_FINISH); //结束掉mainActivity
                        context.sendBroadcast(createPwdSuccess);
                    }
                    break;
                case 0:
                    com.macrovideo.sdk.custom.DeviceInfo info = (com.macrovideo.sdk.custom.DeviceInfo) msg.obj;
                    addDevice(info);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);
    private ZhujiInfo zhuji;
    private DeviceInfo device;

    public MainAdapter(Context context) {
        this.context = context;
//        zhuji = DatabaseOperator.getInstance(context.getApplicationContext()).queryDeviceZhuJiInfo(
//                DataCenterSharedPreferences.getInstance(context, DataCenterSharedPreferences.Constant.CONFIG)
//                        .getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(context.getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());

        if (zhuji == null){
            ToastUtil.shortMessage(context.getString(R.string.net_error_noinit));
            ((Activity)context).finish();
        }
        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(context).queryAllDeviceInfos(zhuji.getId());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo deviceInfo : dInfos) {
                if (deviceInfo.getCak().equals("surveillance")) {
                    device = deviceInfo;
                    break;
                }
            }
        }
    }

    @Override
    public int getCount() {
        return FList.getInstance().size();
    }

    @Override
    public Contact getItem(int position) {
        return FList.getInstance().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int size = FList.getInstance().size();
        final ViewHolder holder;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_device_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Contact contact = FList.getInstance().get(position);

        if (contact == null) {
            return convertView;
        }
        if (holder.getName() != null) {
            holder.getName().setText(contact.contactName);
        }
        int deviceType = contact.contactType;
        holder.getL_ap().setVisibility(View.GONE);
        if (contact.onLineState == Constants.DeviceState.ONLINE) {
            holder.getHead().updateImage(contact.contactId, false);
//			holder.getOnline_state().setImageResource(R.drawable.device_online);
            if (contact.contactType == P2PValue.DeviceType.UNKNOWN
                    || contact.contactType == P2PValue.DeviceType.PHONE) {
                holder.getIv_defence_state().setVisibility(RelativeLayout.INVISIBLE);
            } else {
                holder.getIv_defence_state().setVisibility(RelativeLayout.VISIBLE);
                if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_LOADING) {
                    holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.INVISIBLE);
//                    holder.getIv_defence_state().setImageResource(R.drawable.weak_password);
                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                    holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setImageResource(R.drawable.defence_on);
                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
                    holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setImageResource(R.drawable.defence_off);
                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_NET) {
                    holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setImageResource(R.drawable.ic_defence_warning);
                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                    holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setImageResource(R.drawable.weak_password);
                } else if (contact.defenceState == Constants.DefenceState.DEFENCE_NO_PERMISSION) {
                    holder.getProgress_defence().setVisibility(RelativeLayout.GONE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setImageResource(R.drawable.weak_password);
                }
            }

            // 如果是门铃且不是访客密码则获取报警推送账号并判断自己在不在其中，如不在则添加(只执行一次)
            if (deviceType == P2PValue.DeviceType.DOORBELL
                    && contact.defenceState != Constants.DefenceState.DEFENCE_NO_PERMISSION) {
                if (!getIsDoorBellBind(contact.contactId)) {
                    getBindAlarmId(contact.contactId, contact.contactPassword);
                } else {

                }
            }

        } else {
//            holder.getHead().updateImage(contact.contactId, true);
//			holder.getOnline_state().setImageResource(R.drawable.device_online);
            holder.getIv_defence_state().setVisibility(RelativeLayout.INVISIBLE);
            holder.getIv_defence_state().setImageResource(R.drawable.ic_defence_warning);
            holder.getProgress_defence().setVisibility(View.GONE);
        }

//        switch (deviceType) {
//            case P2PValue.DeviceType.IPC:
//                Log.e("Camera_log", "IPC");
//                holder.getIv_set().setVisibility(View.VISIBLE);
//                holder.getIv_editor().setVisibility(View.VISIBLE);
//                holder.getIv_call().setVisibility(View.GONE);
//                break;
//            case P2PValue.DeviceType.NPC:
//                Log.e("Camera_log", "NPC");
//                holder.getIv_playback().setVisibility(View.VISIBLE);
//                holder.getIv_set().setVisibility(View.VISIBLE);
//                holder.getIv_editor().setVisibility(View.VISIBLE);
//                holder.getIv_call().setVisibility(View.VISIBLE);
//                break;
//            case P2PValue.DeviceType.NVR:
//                Log.e("Camera_log", "NVR");
//                holder.getIv_defence_state().setVisibility(View.INVISIBLE);
//                holder.getProgress_defence().setVisibility(View.GONE);
//                holder.getIv_playback().setVisibility(View.GONE);
//                holder.getIv_set().setVisibility(View.VISIBLE);
//                holder.getIv_editor().setVisibility(View.VISIBLE);
//                holder.getIv_call().setVisibility(View.GONE);
//                break;
//            default:
//                Log.e("Camera_log", "其他");
//                holder.getIv_call().setVisibility(View.GONE);
//                break;
//        }

        // 获得布防状态之后判断弱密码
        if (contact.onLineState == Constants.DeviceState.ONLINE
                && (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON
                || contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF)) {
            if (Utils.isWeakPassword(contact.userPassword)) {
                holder.getIv_weakpassword().setVisibility(View.GONE);
            } else {
                holder.getIv_weakpassword().setVisibility(View.GONE);
            }

            holder.getIv_update().setVisibility(ImageView.GONE);
        } else {
            holder.getIv_weakpassword().setVisibility(View.GONE);
            holder.getIv_update().setVisibility(ImageView.GONE);
        }
        holder.getHead().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (contact.isConnectApWifi == true) {
                    T.showShort(context, R.string.change_phone_net);
                    return;
                }
                LocalDevice localDevice = FList.getInstance().isContactUnSetPassword(contact.contactId);
                if (null != localDevice) {
                    Contact saveContact = new Contact();
                    saveContact.contactId = localDevice.contactId;
                    saveContact.contactType = localDevice.type;
                    saveContact.messageCount = 0;
                    saveContact.activeUser = NpcCommon.mThreeNum;

                    Intent modify = new Intent();
                    modify.setClass(context, AddContactNextActivity.class);
                    modify.putExtra("isCreatePassword", true);
                    modify.putExtra("contact", saveContact);
                    String mark = localDevice.address.getHostAddress();
                    modify.putExtra("ipFlag", mark.substring(mark.lastIndexOf(".") + 1, mark.length()));
                    context.startActivity(modify);
                    return;
                }
                toMonitor(contact);
            }

        });
//		if (deviceType == P2PValue.DeviceType.NPC || deviceType == P2PValue.DeviceType.IPC
//				|| deviceType == P2PValue.DeviceType.DOORBELL) {
//			holder.getHead().setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					// TODO Auto-generated method stub
//					Log.e("Camera_log", "trueNPC");
//					if (contact.isConnectApWifi == true) {
//						T.showShort(context, R.string.change_phone_net);
//						return;
//					}
//					LocalDevice localDevice = FList.getInstance().isContactUnSetPassword(contact.contactId);
//					if (null != localDevice) {
//						Contact saveContact = new Contact();
//						saveContact.contactId = localDevice.contactId;
//						saveContact.contactType = localDevice.type;
//						saveContact.messageCount = 0;
//						saveContact.activeUser = NpcCommon.mThreeNum;
//
//						Intent modify = new Intent();
//						modify.setClass(context, AddContactNextActivity.class);
//						modify.putExtra("isCreatePassword", true);
//						modify.putExtra("contact", saveContact);
//						String mark = localDevice.address.getHostAddress();
//						modify.putExtra("ipFlag", mark.substring(mark.lastIndexOf(".") + 1, mark.length()));
//						context.startActivity(modify);
//						return;
//					}
//					toMonitor(contact);
//				}
//
//			});
//		} else if (deviceType == P2PValue.DeviceType.NVR) {
//			holder.getHead().setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//				}
//			});
//
//		} else if (deviceType == P2PValue.DeviceType.PHONE) {
//			holder.getHead().setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					if (contact.contactId == null || contact.contactId.equals("")) {
//						T.showShort(context, R.string.username_error);
//						return;
//					}
//
//						/*
//                         * Intent call = new Intent(); call.setClass(context,
//						 * CallActivity.class); call.putExtra("callId",
//						 * contact.contactId); call.putExtra("contact",contact);
//						 * call.putExtra("isOutCall", true);
//						 * call.putExtra("type",
//						 * Constants.P2P_TYPE.P2P_TYPE_CALL);
//						 * context.startActivity(call);
//						 */
//				}
//
//			});
//		} else {
//			holder.getHead().setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					// TODO Auto-generated method stub
//					Log.e("jiankong", "trueOTHER");
//					if (contact.isConnectApWifi == true) {
//						T.showShort(context, R.string.change_phone_net);
//					}
//					if (Integer.parseInt(contact.contactId) < 256) {
//						toMonitor(contact);
//					} else {
//						holder.getHead().setOnClickListener(null);
//					}
//
//				}
//			});
//
//		}

        holder.getIv_defence_state().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_NET
                        || contact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                    Toast.makeText(context, "密码错误或者网络异常", Toast.LENGTH_SHORT).show();
                    holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                    P2PHandler.getInstance().getDefenceStates(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                    FList.getInstance().setIsClickGetDefenceState(contact.contactId, true);
                } else {
                    if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                        holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                        holder.getIv_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                        P2PHandler.getInstance().setRemoteDefence(contact.contactId, contact.contactPassword,
                                Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF, MainApplication.GWELL_LOCALAREAIP);
                        FList.getInstance().setIsClickGetDefenceState(contact.contactId, true);
                    } else if (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
                        holder.getProgress_defence().setVisibility(RelativeLayout.VISIBLE);
                        holder.getIv_defence_state().setVisibility(RelativeLayout.INVISIBLE);
                        P2PHandler.getInstance().setRemoteDefence(contact.contactId, contact.contactPassword,
                                Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON, MainApplication.GWELL_LOCALAREAIP);
                        FList.getInstance().setIsClickGetDefenceState(contact.contactId, true);
                        AlertDialog myAlertDialog = new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.activity_weight_notice))
                                .setMessage(context.getString(R.string.camera_dafen_msg))
                                .setPositiveButton(context.getString(R.string.sure),
                                        null).show();
                    }
                }
            }
        });
        holder.getHead().setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                Intent it = new Intent();
                it.setAction(Constants.Action.DIAPPEAR_ADD);
                context.sendBroadcast(it);
                return false;
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                LocalDevice localDevice = FList.getInstance().isContactUnSetPassword(contact.contactId);
                if (null != localDevice) {
                    Contact saveContact = new Contact();
                    saveContact.contactId = localDevice.contactId;
                    saveContact.contactType = localDevice.type;
                    saveContact.messageCount = 0;
                    saveContact.activeUser = NpcCommon.mThreeNum;

                    Intent modify = new Intent();
                    modify.setClass(context, AddContactNextActivity.class);
                    modify.putExtra("isCreatePassword", true);
                    modify.putExtra("contact", saveContact);
                    String mark = localDevice.address.getHostAddress();
                    modify.putExtra("ipFlag", mark.substring(mark.lastIndexOf(".") + 1, mark.length()));
                    context.startActivity(modify);
                    return;
                }
            }

        });

        holder.getHead().setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                NormalDialog dialog = new NormalDialog(context,
                        context.getResources().getString(R.string.delete_contact),
                        context.getResources().getString(R.string.are_you_sure_delete) + " " + contact.contactId
                                + "?",
                        context.getResources().getString(R.string.delete),
                        context.getResources().getString(R.string.cancel));
                dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

                    @Override
                    public void onClick() {
                        if (getCount() != 0) {
                            if (null == dialogProgress) {
                                dialogProgress = new NormalDialog(context, context.getResources().getString(R.string.operationing), "", "", "");
                                dialogProgress.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
                                dialogProgress.showDialog();
                            }
                            deleteDevice(contact, position);
                        }

                    }

                    private void deleteDevice(final Contact contact, final int position) {
                        //清空推送用户
                        P2PHandler.getInstance().setBindAlarmId(contact.contactId,contact.getContactPassword(),0,new String[]{}, MainApplication.GWELL_LOCALAREAIP);

                        final long did = device.getId();
                        final String id = contact.contactId;
                        JavaThreadPool.getInstance().excute(new Runnable() {

                            @Override
                            public void run() {
                                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(context,
                                        DataCenterSharedPreferences.Constant.CONFIG);
                                String server = dcsp
                                        .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                com.alibaba.fastjson.JSONObject pJsonObject = new com.alibaba.fastjson.JSONObject();
                                pJsonObject.put("did", did);
                                pJsonObject.put("id", id);

                                String http = server + "/jdm/s3/ipcs/del";
                                String result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject, dcsp);

                                // -1参数为空 -2校验失败 -100服务器错误
                                if ("-1".equals(result)) {
                                    defaultHandler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            Toast.makeText(context, context.getString(R.string.register_tip_empty), Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    });
                                } else if ("-2".equals(result)) {
                                    defaultHandler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            Toast.makeText(context, context.getString(R.string.device_check_failure),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else if ("-100".equals(result)) {
                                    defaultHandler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            Toast.makeText(context, context.getString(R.string.activity_server_not_exit),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else if (result != null && !"".equals(result) && "0".equals(result)) {
                                    //删除本地数据库
                                    FList.getInstance().delete(contact.contactId);
                                    //删除本地截图
                                    File file = new File(Constants.Image.USER_HEADER_PATH + NpcCommon.mThreeNum + "/"
                                            + contact.contactId);
                                    Utils.deleteFile(file);
                                    Message message = defaultHandler.obtainMessage(1);
                                    message.obj = contact;
                                    defaultHandler.sendMessage(message);
                                }
                            }
                        });
                    }

                });
                dialog.showDialog();
                return true;
            }

        });

        holder.getIv_weakpassword().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                    /*
                     * // TODO Auto-generated method stub Intent modify_pwd=new
					 * Intent(context,ModifyNpcPasswordActivity.class);
					 * modify_pwd.putExtra("contact",contact);
					 * modify_pwd.putExtra("isWeakPwd",true);
					 * context.startActivity(modify_pwd);
					 */
            }
        });
        holder.getIv_playback().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (contact.contactType == P2PValue.DeviceType.UNKNOWN) {
                    return;
                }
                if (contact.isConnectApWifi == true) {
                    T.showShort(context, R.string.change_phone_net);
                    return;
                }
                if (contact.onLineState == Constants.DeviceState.ONLINE) {
                        /*
                         * Intent playback = new Intent();
						 * playback.setClass(context,
						 * PlayBackListActivity.class);
						 * playback.putExtra("contact", contact);
						 * context.startActivity(playback);
						 */
                }
            }
        });
        holder.getIv_editor().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DeviceEditor(contact);
            }
        });
        holder.getIv_set().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (contact.onLineState == Constants.DeviceState.OFFLINE) return;
                if (contact.isConnectApWifi == true) {
                    T.showShort(context, R.string.change_phone_net);
                    return;
                }
                if (contact.contactType == P2PValue.DeviceType.NVR) {
                } else {
                    DeviceSettingClick(contact);
                }
            }
        });
        holder.getIv_update().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (contact.contactType == P2PValue.DeviceType.UNKNOWN
                        && Integer.valueOf(contact.contactId) > 256) {
                    return;
                }
            }
        });
        holder.getIv_call().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stu
                if (contact.isConnectApWifi == true) {
                    T.showShort(context, R.string.change_phone_net);
                    return;
                }
                if (contact.contactType == P2PValue.DeviceType.UNKNOWN
                        && Integer.valueOf(contact.contactId) > 256) {
                    return;
                }
                if (contact.contactId == null || contact.contactId.equals("")) {
                    T.showShort(context, R.string.username_error);
                    return;
                }
                if (contact.contactPassword == null || contact.contactPassword.equals("")) {
                    T.showShort(context, R.string.password_error);
                    return;
                }
                Intent i = new Intent();
                i.putExtra("contact", contact);
                i.setAction(Constants.Action.CALL_DEVICE);
                MainApplication.app.sendBroadcast(i);
            }
        });
        return convertView;
    }


    /**
     * 设置按钮单击跳转流程
     *
     * @param contact
     */
    private void DeviceSettingClick(Contact contact) {
        Log.e("set", "holder");
        if (contact.contactType == P2PValue.DeviceType.UNKNOWN && Integer.valueOf(contact.contactId) > 256) {
            return;
        }
        if (contact.contactId == null || contact.contactId.equals("")) {
            T.showShort(context, R.string.username_error);
            return;
        }
        if (contact.contactPassword == null || contact.contactPassword.equals("")) {
            T.showShort(context, R.string.password_error);
            return;
        }
        Intent i = new Intent();
        i.putExtra("contact", contact);
        i.setAction(Constants.Action.ENTER_DEVICE_SETTING);
        context.sendBroadcast(i);
    }

    /**
     * 普通模式下的编辑
     *
     * @param contact
     */
    private void DeviceEditor(Contact contact) {
        LocalDevice localDevice = FList.getInstance().isContactUnSetPassword(contact.contactId);
        // 判断局域网密码
        if (null != localDevice) {
            Contact saveContact = new Contact();
            saveContact.contactId = localDevice.contactId;
            saveContact.contactType = localDevice.type;
            saveContact.messageCount = 0;
            saveContact.activeUser = NpcCommon.mThreeNum;

            Intent modify = new Intent();
            modify.setClass(context, AddContactNextActivity.class);
            modify.putExtra("isCreatePassword", true);
            modify.putExtra("contact", saveContact);
            String mark = localDevice.address.getHostAddress();
            modify.putExtra("ipFlag", mark.substring(mark.lastIndexOf(".") + 1, mark.length()));
            context.startActivity(modify);
            return;
        } else {
            Intent modify = new Intent();
            modify.setClass(context, ModifyContactActivity.class);
            modify.putExtra("contact", contact);
            modify.putExtra("isEditorWifiPwd", false);
            context.startActivity(modify);
        }
    }

    /**
     * 普通设备去监控
     *
     * @param contact
     */
    private void toMonitor(Contact contact) {
        Log.e("toMonitor", "toMonitor");
        if (null != FList.getInstance().isContactUnSetPassword(contact.contactId)) {
            return;
        }
        if (contact.contactId == null || contact.contactId.equals("")) {
            T.showShort(context, R.string.username_error);
            return;
        }
        if (contact.contactPassword == null || contact.contactPassword.equals("")) {
            T.showShort(context, R.string.password_error);
            return;
        }
        Intent monitor = new Intent();
        monitor.setClass(context, ApMonitorActivity.class);
        monitor.putExtra("contact", contact);
        monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
        context.startActivity(monitor);
    }

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            notifyDataSetChanged();
            return true;
        }
    });

    List<String> doorbells = new ArrayList<String>();
    Map<String, String[]> idMaps = new HashMap<String, String[]>();

    private void getBindAlarmId(String id, String password) {
        if (!doorbells.contains(id)) {
            doorbells.add(id);
        }
        P2PHandler.getInstance().getBindAlarmId(id, password, MainApplication.GWELL_LOCALAREAIP);
    }

    public boolean isNeedModifyPwd(Contact contact) {

        if (contact.onLineState == Constants.DeviceState.ONLINE
                && (contact.defenceState == Constants.DefenceState.DEFENCE_STATE_ON
                || contact.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF)) {
            if (Utils.isWeakPassword(contact.userPassword)) {
                return true;
            }
        }
        return false;
    }

    public void getAllBindAlarmId() {
        for (String ss : doorbells) {
            getBindAlarmId(ss);
        }
    }

    private int count = 0;// 总请求数计数器
    private int SumCount = 20;// 总请求次数上限

    public void getBindAlarmId(String id) {

        Contact contact = DataManager.findContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, id);
        if (contact != null && count <= SumCount) {
            // 获取绑定id列表
            P2PHandler.getInstance().getBindAlarmId(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
            count++;
        }
    }

    public void setBindAlarmId(String id, String[] ids) {
        int ss = 0;
        String[] new_data;
        for (int i = 0; i < ids.length; i++) {
            if (!NpcCommon.mThreeNum.equals(ids[i])) {
                ss++;
            }
        }
        if (ss == ids.length) {
            // 不包含则设置
            new_data = new String[ids.length + 1];
            for (int i = 0; i < ids.length; i++) {
                new_data[i] = ids[i];
            }
            new_data[new_data.length - 1] = NpcCommon.mThreeNum;
            Contact contact = DataManager.findContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, id);
            P2PHandler.getInstance().setBindAlarmId(contact.contactId, contact.contactPassword, new_data.length,
                    new_data, MainApplication.GWELL_LOCALAREAIP);
        } else {
            new_data = ids;
        }
        idMaps.put(id, new_data);

    }

    public void setBindAlarmId(String Id) {
        Contact contact = DataManager.findContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, Id);
        if (contact != null && (!idMaps.isEmpty())) {
            String[] new_data = idMaps.get(Id);
            P2PHandler.getInstance().setBindAlarmId(contact.contactId, contact.contactPassword, new_data.length,
                    new_data, MainApplication.GWELL_LOCALAREAIP);
        }

    }

    public void setBindAlarmIdSuccess(String doorbellid) {
        SharedPreferencesManager.getInstance().putIsDoorbellBind(doorbellid, true, context);
    }

    private boolean getIsDoorBellBind(String doorbellid) {
        return SharedPreferencesManager.getInstance().getIsDoorbellBind(context, doorbellid);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public onConnectListner listner;

    public interface onConnectListner {
        void onNvrClick(Contact contact);
    }

    public void setOnSrttingListner(onConnectListner listner) {
        this.listner = listner;
    }

    public class setAlarmAndPropmtThread extends Thread {
        com.macrovideo.sdk.custom.DeviceInfo deviceInfo;
        boolean ishasAlarmConfig;
        boolean isAlarmSwitch;
        boolean ishasVoiceSwitch;
        boolean isVoicePromptsMainSwitch;
        int nLanguage;

        public setAlarmAndPropmtThread(com.macrovideo.sdk.custom.DeviceInfo deviceInfo, boolean ishasAlarmConfig,
                                       boolean isAlarmSwitch, boolean ishasVoiceSwitch, boolean isVoicePromptsMainSwitch, int nLanguage) {
            this.deviceInfo = deviceInfo;
            this.ishasAlarmConfig = ishasAlarmConfig;
            this.isAlarmSwitch = isAlarmSwitch;
            this.ishasVoiceSwitch = ishasVoiceSwitch;
            this.isVoicePromptsMainSwitch = isVoicePromptsMainSwitch;
            this.nLanguage = nLanguage;
        }

        @Override
        public void run() {
            AlarmAndPromptInfo info = DeviceAlarmAndPromptSetting.setAlarmAndPropmt(deviceInfo, ishasAlarmConfig,
                    isAlarmSwitch, true, ishasVoiceSwitch, ishasVoiceSwitch, nLanguage, true, 1,
                    isVoicePromptsMainSwitch);
            if (info.getnResult() == ResultCode.RESULT_CODE_SUCCESS) {
                defaultHandler.sendEmptyMessage(1);
            }
        }
    }

    public class getAlarmAndPropmtThread extends Thread {
        com.macrovideo.sdk.custom.DeviceInfo info;

        public getAlarmAndPropmtThread(com.macrovideo.sdk.custom.DeviceInfo info) {
            this.info = info;
        }

        @Override
        public void run() {
            AlarmAndPromptInfo alarm = DeviceAlarmAndPromptSetting.getAlarmAndPropmt(info);
            if (alarm.getnResult() == ResultCode.RESULT_CODE_SUCCESS) {
                defaultHandler.sendEmptyMessage(1);
            }

        }
    }

    private void addDevice(com.macrovideo.sdk.custom.DeviceInfo device) {
        String c1 = null, id1 = null, n1 = null, p1 = null;
        final long did = zhuji.getId();
        c1 = "v380";
        id1 = device.getnDevID() + "";
        n1 = device.getStrName();
        p1 = device.getStrPassword();
        final String c = c1;
        final String id = id1;
        final String n = n1;
        final String p = p1;
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(context,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("c", c);
                pJsonObject.put("id", id);
                pJsonObject.put("n", n);
                pJsonObject.put("p", p);

                String http =  server + "/jdm/s3/ipcs/add";
                String result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject, dcsp);

                // -1参数为空 -2校验失败 -10服务器不存在
                if ("-1".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(context, context.getString(R.string.register_tip_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-2".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(context, context.getString(R.string.device_check_failure),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-100".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(context, context.getString(R.string.net_error_requestfailed),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("0".equals(result)) {
                    defaultHandler.sendEmptyMessage(1);
                } else {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(context, context.getString(R.string.net_error_weizhi),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    public boolean StartSearchDevice() {
        Log.e("摄像头", "准备连接1");
        System.out.println("StartSearchDevice");// add for test
        closeMulticast();
        openMulticast();
        new DeviceSearchThread(1).start();
        return true;

    }

    public void closeMulticast() {
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }

    }

    public void openMulticast() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifiManager.createMulticastLock("multicast");
            multicastLock.acquire();
        } catch (Exception e) {
            // @@System.out.println("openMulticast error");//add for test
            multicastLock = null;
        }
    }

    WifiManager.MulticastLock multicastLock = null;
    com.macrovideo.sdk.custom.DeviceInfo deviceInfo = null;

    // 设备搜索线程
    public class DeviceSearchThread extends Thread {

        static final int MAX_DATA_PACKET_LENGTH = 128;

        private byte buffer[] = new byte[MAX_DATA_PACKET_LENGTH];

        private int nTreadSearchID = 0;

        public DeviceSearchThread(int nSearchID) {
            nTreadSearchID = nSearchID;

        }

        public void run() {
            Log.e("摄像头", "准备连接2");
            System.out.println("DeviceSearchThread: run ");// add for test
            com.macrovideo.sdk.custom.DeviceInfo info = null;
            ArrayList<com.macrovideo.sdk.custom.DeviceInfo> resultList = DeviceScanner.getDeviceListFromLan();
            if (resultList != null && resultList.size() > 0) {
                for (int i = 0; i < resultList.size(); i++) {
                    info = resultList.get(i);
                    VList.getInstance().insert(info);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = info;
                    defaultHandler.sendMessage(msg);
                }

            }

        }

    }

    /**
     * 控件管理
     */
    class ViewHolder {
        private HeaderView head;
        private TextView name;
        private ImageView online_state;
        private ImageView iv_defence_state;
        private ProgressBar progress_defence;
        private ImageView iv_weakpassword;
        private ImageView iv_update;
        private ImageView iv_editor;
        private ImageView iv_playback;
        private ImageView iv_set;
        private RelativeLayout r_online_state;
        private LinearLayout l_ap;
        private ImageView iv_ap_state;
        private ImageView iv_call;

        public ViewHolder(View convertView) {
            HeaderView head = (HeaderView) convertView.findViewById(R.id.user_icon);
            this.setHead(head);
            TextView name = (TextView) convertView.findViewById(R.id.tv_name_holder);
            this.setName(name);
            ImageView onlineState = (ImageView) convertView.findViewById(R.id.tv_online_state);
            this.setOnline_state(onlineState);
            ImageView iv_defence_state = (ImageView) convertView.findViewById(R.id.iv_defence_state);
            this.setIv_defence_state(iv_defence_state);
            ProgressBar progress_defence = (ProgressBar) convertView.findViewById(R.id.progress_defence);
            this.setProgress_defence(progress_defence);
            ImageView iv_weakpassword = (ImageView) convertView.findViewById(R.id.iv_weakpassword);
            this.setIv_weakpassword(iv_weakpassword);
            ImageView iv_update = (ImageView) convertView.findViewById(R.id.iv_update);
            this.setIv_update(iv_update);
            ImageView iv_editor = (ImageView) convertView.findViewById(R.id.iv_editor);
            this.setIv_editor(iv_editor);
            ImageView iv_playback = (ImageView) convertView.findViewById(R.id.iv_playback);
            this.setIv_playback(iv_playback);
            ImageView iv_set = (ImageView) convertView.findViewById(R.id.iv_set);
            this.setIv_set(iv_set);
            LinearLayout l_ap = (LinearLayout) convertView.findViewById(R.id.l_ap);
            this.setL_ap(l_ap);
            RelativeLayout r_online_state = (RelativeLayout) convertView.findViewById(R.id.r_online_state);
            this.setR_online_state(r_online_state);
            ImageView iv_ap_state = (ImageView) convertView.findViewById(R.id.iv_ap_state);
            this.setIv_ap_state(iv_ap_state);
            ImageView iv_call = (ImageView) convertView.findViewById(R.id.iv_call);
            this.setIv_call(iv_call);
        }

        public HeaderView getHead() {
            return head;
        }

        public void setHead(HeaderView head) {
            this.head = head;
        }

        public TextView getName() {
            return name;
        }

        public void setName(TextView name) {
            this.name = name;
        }

        public ImageView getOnline_state() {
            return online_state;
        }

        public void setOnline_state(ImageView online_state) {
            this.online_state = online_state;
        }

        public ImageView getIv_defence_state() {
            return iv_defence_state;
        }

        public void setIv_defence_state(ImageView iv_defence_state) {
            this.iv_defence_state = iv_defence_state;
        }

        public ProgressBar getProgress_defence() {
            return progress_defence;
        }

        public void setProgress_defence(ProgressBar progress_defence) {
            this.progress_defence = progress_defence;
        }

        public ImageView getIv_weakpassword() {
            return iv_weakpassword;
        }

        public void setIv_weakpassword(ImageView iv_weakpassword) {
            this.iv_weakpassword = iv_weakpassword;
        }

        public ImageView getIv_update() {
            return iv_update;
        }

        public void setIv_update(ImageView iv_update) {
            this.iv_update = iv_update;
        }

        public ImageView getIv_editor() {
            return iv_editor;
        }

        public void setIv_editor(ImageView iv_editor) {
            this.iv_editor = iv_editor;
        }

        public ImageView getIv_playback() {
            return iv_playback;
        }

        public void setIv_playback(ImageView iv_playback) {
            this.iv_playback = iv_playback;
        }

        public ImageView getIv_set() {
            return iv_set;
        }

        public void setIv_set(ImageView iv_set) {
            this.iv_set = iv_set;
        }

        public RelativeLayout getR_online_state() {
            return r_online_state;
        }

        public void setR_online_state(RelativeLayout r_online_state) {
            this.r_online_state = r_online_state;
        }

        public LinearLayout getL_ap() {
            return l_ap;
        }

        public void setL_ap(LinearLayout l_ap) {
            this.l_ap = l_ap;
        }

        public ImageView getIv_ap_state() {
            return iv_ap_state;
        }

        public void setIv_ap_state(ImageView iv_ap_state) {
            this.iv_ap_state = iv_ap_state;
        }

        public ImageView getIv_call() {
            return iv_call;
        }

        public void setIv_call(ImageView iv_call) {
            this.iv_call = iv_call;
        }

    }
}
