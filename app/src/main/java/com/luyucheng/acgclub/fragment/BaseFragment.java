package com.luyucheng.acgclub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.luyucheng.acgclub.R;
import com.luyucheng.acgclub.activity.PictureActivity;
import com.luyucheng.acgclub.adapter.BasePictureAdapter;
import com.luyucheng.acgclub.bean.AcgBean;
import com.luyucheng.acgclub.minterface.AcgApi;
import com.luyucheng.acgclub.util.Constant;
import com.luyucheng.acgclub.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BaseFragment extends Fragment implements BasePictureAdapter.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "BaseFragment";
    private List<String> urlList = new ArrayList<>();
    private RecyclerView recyclerView;
    private BasePictureAdapter adapter;
    private String category;
//    private int curpage = 1;

    //Fragment的View加载完毕的标记
    private boolean isViewCreated;

    //Fragment对用户可见的标记
    private boolean isUIVisible;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        lazyLoad();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //isVisibleToUser这个boolean值表示:该Fragment的UI 用户是否可见
        if (isVisibleToUser) {
            isUIVisible = true;
            lazyLoad();
        } else {
            isUIVisible = false;
        }
    }

    private void lazyLoad() {
        //这里进行双重标记判断,是因为setUserVisibleHint会多次回调,并且会在onCreateView执行前回调,必须确保onCreateView加载完毕且页面可见,才加载数据
        if (isViewCreated && isUIVisible) {
            //数据加载完毕,恢复标记,防止重复加载
            isViewCreated = false;
            isUIVisible = false;
            requestData();
        }
    }

    void init(View v) {
        assert getArguments() != null;
        category = getArguments().getString("CATEGORY");
        recyclerView = v.findViewById(R.id.base_recyclerview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
//        ParallaxRecyclerViewController mParallaxRecyclerViewController = new ParallaxRecyclerViewController(linearLayoutManager, R.id.base_fragment_item_image);
        initLoadMoreListener();
//        recyclerView.addOnScrollListener(mParallaxRecyclerViewController);
        adapter = new BasePictureAdapter(getContext(), recyclerView, urlList, this);
        recyclerView.setAdapter(adapter);
        FloatingActionButton floatingActionButton = v.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
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
                    if (NetworkUtil.isNetworkAvailable(getContext())) {
//                        curpage = Constant.getRandomPage(category);
                        requestData();
                    }
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
        if (NetworkUtil.isNetworkAvailable(getContext())) {
            int curpage = Constant.getRandomPage(category);
            Log.i(TAG, "requestData: " + "加载数据" + curpage);
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
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(getContext(), "网络不可用", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        urlList.clear();
        adapter.notifyDataSetChanged();
//        curpage = Constant.getRandomPage(category);
        requestData();
    }

    @Override
    public void onItemClick(String url, int position) {
//        Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show();
        if (NetworkUtil.isNetworkAvailable(getContext())) {
            Intent intent = new Intent(getContext(), PictureActivity.class);
            intent.putExtra("IMGURL", url);
            startActivity(intent);
        } else Toast.makeText(getContext(), "网络不可用", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(String url, int position) {
//        Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show();
//        requestPermission(url);
    }


}
