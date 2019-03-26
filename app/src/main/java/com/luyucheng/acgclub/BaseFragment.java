package com.luyucheng.acgclub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import martinbzdqsm.com.parallaxscrollimageview_master.ParallaxRecyclerViewController;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BaseFragment extends Fragment implements BasePictureAdapter.OnItemClickListener {

    private static final String TAG = "BaseFragment";
    private List<String> urlList = new ArrayList<>();
    private RecyclerView recyclerView;
    private BasePictureAdapter adapter;
    private String category;
    private int curpage = 1;

    public static BaseFragment newInstance(String category) {
        Bundle args = new Bundle();
        BaseFragment fragment = new BaseFragment();
        args.putString("CATEGORY", category);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base, null);
        init(view);
        return view;
    }

    void init(View v) {
        assert getArguments() != null;
        category = getArguments().getString("CATEGORY");
        recyclerView = v.findViewById(R.id.base_recyclerview);
        adapter = new BasePictureAdapter(getContext(), urlList, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        ParallaxRecyclerViewController mParallaxRecyclerViewController = new ParallaxRecyclerViewController(linearLayoutManager, R.id.base_fragment_item_image);
        initLoadMoreListener();
        recyclerView.addOnScrollListener(mParallaxRecyclerViewController);
        recyclerView.setAdapter(adapter);

        requestData();
    }

    private void initLoadMoreListener() {
        //对Recyclerview 的一个滑动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    curpage = curpage + 1;
                    requestData();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //最后一个可见的ITEM
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });
    }

    public void requestData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.rabtman.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AcgApi acgApi = retrofit.create(AcgApi.class);
        Call<AcgBean> call = acgApi.ACG_BEAN_CALL(category, curpage);
        call.enqueue(new Callback<AcgBean>() {
            @Override
            public void onResponse(@NonNull Call<AcgBean> call, @NonNull Response<AcgBean> response) {
                if (response.isSuccessful()) {
                    AcgBean acgBean = response.body();
                    assert acgBean != null;
                    for (AcgBean.DataBean dataBean : acgBean.getData()) {
                        urlList.addAll(dataBean.getImgUrls());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AcgBean> call, @NonNull Throwable t) {

            }
        });
    }


    @Override
    public void onItemClick(String url, int position) {
//        Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext(), PictureActivity.class);
        intent.putExtra("IMGURL", url);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(String url, int position) {
//        Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show();
//        requestPermission(url);
    }


}
