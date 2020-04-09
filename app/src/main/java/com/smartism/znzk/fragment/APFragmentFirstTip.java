package com.smartism.znzk.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.smartism.znzk.R;

public class APFragmentFirstTip extends Fragment implements View.OnClickListener{
  private static final String ARG_FIRST_TIP = "ARG_FIRST_TIP";

  //必须要调用Fragment的无参构造器，因此Fragment只有默认的无参构造器，Activity在销毁重建时，调用的是无参的构造器
  public APFragmentFirstTip(){

  }

  private OnAPFragmentFisrtTipListener mListener ;



    public interface OnAPFragmentFisrtTipListener{
      void apFirstFragmentNext();
  }

  private String DATA ;


  public static APFragmentFirstTip getInstance(String data){
      APFragmentFirstTip apFragmentFirstTip = new APFragmentFirstTip();
      Bundle bundle = new Bundle();
      bundle.putString(ARG_FIRST_TIP,data);
      apFragmentFirstTip.setArguments(bundle);
      return apFragmentFirstTip ;
  }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            DATA = getArguments().getString(ARG_FIRST_TIP );
        }

        if(getActivity() instanceof OnAPFragmentFisrtTipListener){
            mListener = (OnAPFragmentFisrtTipListener) getActivity();
        }else{
            throw  new IllegalArgumentException("宿主Activity必须实现OnAPFragmentFisrtTipListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apfragment_first_tip_layout,container,false);
    }

    private Button next_btn ;
    private ListView mPrincipleListView ;
    private String[] mTips ;
    private TextView mPricipleText ;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPricipleText = view.findViewById(R.id.priclple_tv);
        next_btn =view.findViewById(R.id.first_next_btn);
        mPrincipleListView = view.findViewById(R.id.buzhou_listview);
        next_btn.setOnClickListener(this);

        mPricipleText.setText(Html.fromHtml(getResources().getString(R.string.add_zhuji_by_ap_priciple_first)));
        mTips = getResources().getStringArray(R.array.add_zhuji_by_ap_priciple_tips);
        mPrincipleListView.setAdapter(new ArrayAdapter(getContext(),R.layout.text_view_with_small_left_dot,mTips));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.first_next_btn:
                mListener.apFirstFragmentNext();
                break ;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null ;
    }
}
