package com.example.energydream;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.energydream.Model.Business;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 이명남 on 2018-11-12.
 */

public class RecyclerAdapter_venture  extends RecyclerView.Adapter<RecyclerAdapter_venture.ViewHolder>{

    FirebaseStorage storage = FirebaseStorage.getInstance();
    private ArrayList<Business> mList;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout linearLayout;
        ImageView mimageView;
        TextView titletext;
        TextView hosttext;
        ProgressBar progress;
        TextView percent;
        TextView fund;
        Button btn_donation;

        ImageView img_success;
        ImageView img_fail;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mimageView = itemView.findViewById(R.id.image);
            titletext = itemView.findViewById(R.id.title);
            hosttext=itemView.findViewById(R.id.host);
            progress = itemView.findViewById(R.id.progress);
            percent = itemView.findViewById(R.id.percent);
            fund = itemView.findViewById(R.id.fund);
            btn_donation = itemView.findViewById(R.id.donation);
            context = itemView.getContext();
            linearLayout = itemView.findViewById(R.id.linear_venture_list);

            img_success = itemView.findViewById(R.id.img_success);
            img_fail = itemView.findViewById(R.id.img_fail);
        }

        public void onClick(View v){
            Bundle args = new Bundle();
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                // 현재 프래그먼트 스택에 저장
                NavActivity.stack_fragment.push(NavActivity.manager.findFragmentById(R.id.content_fragment_layout));

                args.putString("index", position+"");
                args.putString("fragment", NavActivity.FRAGMENT_VENTURE_LIST + "");

                NavActivity.getDetailFragment().setArguments(args);
                NavActivity.changeFragment(NavActivity.FRAGMENT_DETAIL);
            }
        }
    }

    public RecyclerAdapter_venture(ArrayList<Business> items){
        mList = items;
    }

    @Override
    public RecyclerAdapter_venture.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        RecyclerAdapter_venture.ViewHolder vh = new RecyclerAdapter_venture.ViewHolder(v);

        return vh;
    }


    @Override
    public void onBindViewHolder(final RecyclerAdapter_venture.ViewHolder holder, final int position) {

        StorageReference sReference = storage.getReference().child("businessIMG/").child(mList.get(position).getBusinessIMG());
        sReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri.toString()).into(holder.mimageView);
            }
        });

        holder.titletext.setText(mList.get(position).getBusinessName());
        holder.hosttext.setText(mList.get(position).getCompanyName());
        holder.progress.setProgress((mList.get(position).getMileage()*100/mList.get(position).getBusinessGoal()));
        holder.percent.setText(mList.get(position).getMileage()*100/mList.get(position).getBusinessGoal()+ "%");
        holder.fund.setText(mList.get(position).getMileage()+"개");
        holder.img_fail.setVisibility(View.INVISIBLE);      // 초기화 반드시 필요
        holder.img_success.setVisibility(View.INVISIBLE);

        // 사업 종료 여부 확인
        String termi_date = mList.get(position).getGoalDate().trim();
        if(calc_Dday(termi_date) < 0 ){
            if(isSuccess(position)) // 목표 마일리지 모금 성공
                holder.img_success.setVisibility(View.VISIBLE);
            else          // 목표 마일리지 모금 실패
                holder.img_fail.setVisibility(View.VISIBLE);
        }

        holder.btn_donation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String business_id = mList.get(position).getBusinessID();

                if(NavActivity.login_user == null){
                    Toast.makeText(context, "로그인 시 이용할 수 있습니다", Toast.LENGTH_LONG ).show();
                }else {
                    // 현재 프래그먼트 스택에 저장
                    NavActivity.stack_fragment.push(NavActivity.manager.findFragmentById(R.id.content_fragment_layout));

                    // Bundle에 사업id 저장 후 프래그먼트 전환
                    Bundle bundle = new Bundle();
                    bundle.putString("index", position + "");
                    bundle.putBoolean("isVenture", true);

                    NavActivity.getDonationFragment().setArguments(bundle);
                    NavActivity.changeFragment(NavActivity.FRAGMENT_DONATION);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private boolean isSuccess(int index){
        if(mList.get(index).getMileage() >= mList.get(index).getBusinessGoal())
            return true;   // 모금 실패
        else
            return false;    // 모금 성공
    }

    // yyyy년 MM월 dd일 값과 현재날짜와의 차이값 계산(D-day)
    private int calc_Dday(String goal_day){
        int d_day = 0;

        String end_date_tmp = goal_day;
        String end_date = "";
        for(int i=0; i<end_date_tmp.length(); i++)
            if(Character.isDigit(end_date_tmp.charAt(i)))
                end_date += end_date_tmp.charAt(i);

        SimpleDateFormat dateFormat = new SimpleDateFormat ( "yyyyMMd");
        Date date = new Date();
        String today = dateFormat.format(date);
        d_day = Integer.parseInt(end_date) - Integer.parseInt(today);

        return d_day;
    }
}