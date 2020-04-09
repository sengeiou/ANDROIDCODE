package com.smartism.znzk.adapter.camera;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.APContact;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.domain.camera.LocalDevice;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.widget.HeaderView;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class APContactAdapter extends BaseAdapter {
    Context context;

    public APContactAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return FList.getInstance().apListsize();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    class ViewHolder {
        private HeaderView head;
        private TextView name;
        private TextView online_state;
        private ImageView iv_defence_state;
        private ProgressBar progress_defence;
        private ImageView iv_weakpassword;
        private ImageView iv_update;
        private ImageView iv_playback;
        private ImageView iv_set;
        private RelativeLayout r_online_state;
        private LinearLayout l_ap;
        private ImageView iv_ap_state;
        private ImageView iv_editor;

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

        public TextView getOnline_state() {
            return online_state;
        }

        public void setOnline_state(TextView online_state) {
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

        public ImageView getIv_editor() {
            return iv_editor;
        }

        public void setIv_editor(ImageView iv_editor) {
            this.iv_editor = iv_editor;
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        View view = convertView;
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                    R.layout.list_device_item_no_login, null);
            holder = new ViewHolder();
            HeaderView head = (HeaderView) view.findViewById(R.id.user_icon);
            holder.setHead(head);
            TextView name = (TextView) view.findViewById(R.id.tv_name);
            holder.setName(name);
            TextView onlineState = (TextView) view
                    .findViewById(R.id.tv_online_state);
            holder.setOnline_state(onlineState);
            ImageView iv_defence_state = (ImageView) view.findViewById(R.id.iv_defence_state);
            holder.setIv_defence_state(iv_defence_state);
            ProgressBar progress_defence = (ProgressBar) view.findViewById(R.id.progress_defence);
            holder.setProgress_defence(progress_defence);
            ImageView iv_weakpassword = (ImageView) view.findViewById(R.id.iv_weakpassword);
            holder.setIv_weakpassword(iv_weakpassword);
            ImageView iv_update = (ImageView) view.findViewById(R.id.iv_update);
            holder.setIv_update(iv_update);
            ImageView iv_editor = (ImageView) view.findViewById(R.id.iv_editor);
            holder.setIv_editor(iv_editor);
            ImageView iv_playback = (ImageView) view.findViewById(R.id.iv_playback);
            holder.setIv_playback(iv_playback);
            ImageView iv_set = (ImageView) view.findViewById(R.id.iv_set);
            holder.setIv_set(iv_set);
            LinearLayout l_ap = (LinearLayout) view.findViewById(R.id.l_ap);
            holder.setL_ap(l_ap);
            RelativeLayout r_online_state = (RelativeLayout) view.findViewById(R.id.r_online_state);
            holder.setR_online_state(r_online_state);
            ImageView iv_ap_state = (ImageView) view.findViewById(R.id.iv_ap_state);
            holder.setIv_ap_state(iv_ap_state);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.getIv_weakpassword().setVisibility(View.GONE);
        final LocalDevice apdevice = FList.getInstance().getAPDdeviceByPosition(position);
        holder.getName().setText(apdevice.name);
        holder.getHead().updateImage(apdevice.getContactId(), true);
        if (apdevice.apModeState == Constants.APmodeState.LINK) {
            holder.getIv_ap_state().setBackgroundResource(R.drawable.item_ap_link);
//			holder.getIv_defence_state().setVisibility(View.VISIBLE);
//			holder.getProgress_defence().setVisibility(View.VISIBLE);
            holder.getIv_defence_state().setVisibility(
                    RelativeLayout.VISIBLE);
            if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_LOADING) {
                holder.getProgress_defence().setVisibility(
                        RelativeLayout.VISIBLE);
                holder.getIv_defence_state().setVisibility(
                        RelativeLayout.INVISIBLE);
            } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                holder.getProgress_defence().setVisibility(
                        RelativeLayout.GONE);
                holder.getIv_defence_state().setVisibility(
                        RelativeLayout.VISIBLE);
                holder.getIv_defence_state().setImageResource(
                        R.drawable.defence_on);

            } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
                holder.getProgress_defence().setVisibility(
                        RelativeLayout.GONE);
                holder.getIv_defence_state().setVisibility(
                        RelativeLayout.VISIBLE);
                holder.getIv_defence_state().setImageResource(
                        R.drawable.item_disarm);

            } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_NET) {
                holder.getProgress_defence().setVisibility(
                        RelativeLayout.GONE);
                holder.getIv_defence_state().setVisibility(
                        RelativeLayout.VISIBLE);
                holder.getIv_defence_state().setImageResource(
                        R.drawable.ic_defence_warning);
            } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                holder.getProgress_defence().setVisibility(
                        RelativeLayout.GONE);
                holder.getIv_defence_state().setVisibility(
                        RelativeLayout.VISIBLE);
                holder.getIv_defence_state().setImageResource(
                        R.drawable.ic_defence_warning);
            } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_NO_PERMISSION) {
                holder.getProgress_defence().setVisibility(
                        RelativeLayout.GONE);
                holder.getIv_defence_state().setVisibility(
                        RelativeLayout.VISIBLE);
                holder.getIv_defence_state().setImageResource(
                        R.drawable.limit);
            }
        } else if (apdevice.apModeState == Constants.APmodeState.UNLINK) {
            holder.getIv_ap_state().setBackgroundResource(R.drawable.item_ap_unlick);
            holder.getIv_defence_state().setVisibility(View.GONE);
            holder.getProgress_defence().setVisibility(View.GONE);
        } else {
            holder.getIv_ap_state().setBackgroundResource(R.drawable.item_no_search);
            holder.getIv_defence_state().setVisibility(View.GONE);
            holder.getProgress_defence().setVisibility(View.GONE);
        }
        holder.getL_ap().setVisibility(View.VISIBLE);

        holder.getIv_editor().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Contact saveContact = new Contact();
                saveContact.contactId = apdevice.contactId;
                saveContact.contactName = apdevice.name;
                saveContact.contactType = apdevice.type;
                APContact ap = DataManager.findAPContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, apdevice.contactId);
                if (ap != null) {
                    saveContact.wifiPassword = ap.Pwd;
                } else {
                    saveContact.wifiPassword = "";
                }
                saveContact.contactPassword = "0";
                saveContact.contactFlag = apdevice.flag;
                saveContact.mode = P2PValue.DeviceMode.AP_MODE;
                try {
                    saveContact.ipadressAddress = InetAddress.getByName("192.168.1.1");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                /*saveContact.messageCount = 0;
                saveContact.activeUser = NpcCommon.mThreeNum;
				Intent modify = new Intent();
				modify.setClass(context, ModifyContactActivity.class);
				modify.putExtra("contact", saveContact);
				modify.putExtra("isEditorWifiPwd", true);
				context.startActivity(modify);*/
            }
        });
        holder.getHead().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Contact saveContact = new Contact();
                saveContact.contactId = apdevice.contactId;
                saveContact.contactName = apdevice.name;
                saveContact.contactPassword = "0";
                saveContact.contactType = apdevice.type;
                saveContact.contactFlag = apdevice.flag;
                saveContact.mode = P2PValue.DeviceMode.AP_MODE;
                try {
                    saveContact.ipadressAddress = InetAddress.getByName("192.168.1.1");
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                saveContact.messageCount = 0;
                saveContact.activeUser = NpcCommon.mThreeNum;
                String mark = apdevice.address.getHostAddress();
                if (WifiUtils.getInstance().isConnectWifi(apdevice.name)) {
                    Intent apMonitor = new Intent(context, ApMonitorActivity.class);
                    apMonitor.putExtra("contact", saveContact);
                    apMonitor.putExtra("connectType", Constants.ConnectType.RTSPCONNECT);
                    context.startActivity(apMonitor);
                } else {
                    if (DataManager.isAPContactExist(context, NpcCommon.mThreeNum, saveContact.contactId)) {
                        APContact apContact = DataManager.findAPContactByActiveUserAndContactId(context, NpcCommon.mThreeNum, saveContact.contactId);
                        listner.onConnectClick(apContact);
                    } else {
                        /*Intent modify = new Intent();
						modify.setClass(context, AddApDeviceActivity.class);
						modify.putExtra("isCreatePassword", false);
						modify.putExtra("islogin", false);
						modify.putExtra("isAPModeConnect", 0);
						modify.putExtra("contact", saveContact);
						modify.putExtra("ipFlag","1");
						context.startActivity(modify);	*/
                    }
                }
            }
        });
        holder.getIv_set().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (WifiUtils.getInstance().isConnectWifi(AppConfig.Relese.APTAG + apdevice.contactId)) {
					/*Intent control = new Intent();
					control.setClass(context, MainControlActivity.class);
					Contact c = new Contact();
					c.contactName=apdevice.name;
					c.contactPassword="0";
//					c.userPassword=apdevice.userPassword;
					c.messageCount=0;
					c.contactId="1";
//					c.activeUser=apdevice.activeUser;
					c.contactType=P2PValue.DeviceType.IPC;
//					c.defenceState=apdevice.defenceState;
					try {
						c.ipadressAddress=InetAddress.getByName("192.168.1.1");
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					c.mode=P2PValue.DeviceMode.AP_MODE;
					control.putExtra("contact", c);
					control.putExtra("connectType", 1);
					control.putExtra("type", P2PValue.DeviceType.IPC);
					context.startActivity(control);*/
                } else {
                    T.showShort(context, R.string.connect_device_wifi);
                }
            }
        });
        holder.getIv_playback().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (WifiUtils.getInstance().isConnectWifi(AppConfig.Relese.APTAG + apdevice.contactId)) {
					/*Intent control = new Intent();
					control.setClass(context, PlayBackListActivity.class);
					Contact c = new Contact();
					c.contactName=apdevice.name;
					c.contactPassword="0";
//					c.userPassword=apdevice.userPassword;
					c.messageCount=0;
					c.contactId="1";
//					c.activeUser=apdevice.activeUser;
					c.contactType=P2PValue.DeviceType.IPC;
//					c.defenceState=apdevice.defenceState;
					try {
						c.ipadressAddress=InetAddress.getByName("192.168.1.1");
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					c.mode=P2PValue.DeviceMode.AP_MODE;
					control.putExtra("contact", c);
					context.startActivity(control);*/
                } else {
                    T.showShort(context, R.string.connect_device_wifi);
                }
            }
        });
        holder.getIv_defence_state().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_NET
                        || apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                    holder.getProgress_defence().setVisibility(
                            RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setVisibility(
                            RelativeLayout.INVISIBLE);
                    P2PHandler.getInstance().getDefenceStates(
                            "1", "0", MainApplication.GWELL_LOCALAREAIP);
                    FList.getInstance().setIsClickGetDefenceState(
                            apdevice.contactId, true);
                } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                    holder.getProgress_defence().setVisibility(
                            RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setVisibility(
                            RelativeLayout.INVISIBLE);
                    P2PHandler
                            .getInstance()
                            .setRemoteDefence(
                                    "1",
                                    "0",
                                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF, MainApplication.GWELL_LOCALAREAIP);
                    FList.getInstance().setIsClickGetDefenceState(
                            apdevice.contactId, true);
                } else if (apdevice.defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
                    holder.getProgress_defence().setVisibility(
                            RelativeLayout.VISIBLE);
                    holder.getIv_defence_state().setVisibility(
                            RelativeLayout.INVISIBLE);
                    P2PHandler
                            .getInstance()
                            .setRemoteDefence(
                                    "1",
                                    "0",
                                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON, MainApplication.GWELL_LOCALAREAIP);
                    FList.getInstance().setIsClickGetDefenceState(
                            apdevice.contactId, true);
                }
            }
        });

        return view;
    }

    public onConnectListner listner;

    public interface onConnectListner {
        void onConnectClick(APContact contact);
    }

    public void setOnSrttingListner(onConnectListner listner) {
        this.listner = listner;
    }

}
