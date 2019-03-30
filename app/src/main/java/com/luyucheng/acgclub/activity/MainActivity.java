package com.luyucheng.acgclub.activity;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.luyucheng.acgclub.R;
import com.luyucheng.acgclub.adapter.ViewPagerAdapter;
import com.luyucheng.acgclub.fragment.BaseFragment;
import com.luyucheng.acgclub.glide.GlideCatchUtil;
import com.luyucheng.acgclub.util.FileUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    MenuItem prevMenuItem;
    private ViewPager viewPager;
//    private String title[] = {"moeimg", "cosplay", "gamersky"};
//    private BaseFragment baseFragment0, baseFragment1, baseFragment2;
//    private Fragment[] fragments = {baseFragment0, baseFragment1, baseFragment2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTranslucent();
        setContentView(R.layout.activity_main);
//        final RxPermissions rxPermissions = new RxPermissions(this);
//        rxPermissions
//                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .subscribe(granted -> {
//                    if (granted) {
//                        FileUtil.deleteCache();
//                    } else {
//
//                    }
//                });
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.item_moeimg:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.item_cosplay:
                            viewPager.setCurrentItem(1);
                            break;
                        case R.id.item_gamersky:
                            viewPager.setCurrentItem(2);
                            break;
                    }
                    return false;
                });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 如果想禁止滑动，可以把下面的代码取消注释
//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
        setupViewPager(viewPager);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(BaseFragment.newInstance("moeimg"));
        adapter.addFragment(BaseFragment.newInstance("cosplay"));
        adapter.addFragment(BaseFragment.newInstance("gamersky"));
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_clear_cache:
                clearCache();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void clearCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        builder.setMessage("确定清除缓存？");
        builder.setCancelable(true);
        builder.setPositiveButton("确定", (dialog, which) -> {
            final RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted) {
                            FileUtil.deleteCache();
                            if (GlideCatchUtil.getInstance().clearCacheDiskSelf())
                                showToast("清除完成");
                        } else {
                            showToast("没有存储权限，没法进行下一步了");
                        }
                    });
        });
        builder.setNegativeButton("取消", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
