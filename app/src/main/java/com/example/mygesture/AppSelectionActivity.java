package com.example.mygesture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygesture.adapter.AppListAdapter;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.manager.AppManager;

import java.util.List;

/**
 * 应用选择Activity
 */
public class AppSelectionActivity extends AppCompatActivity {
    private static final String TAG = "AppSelectionActivity";
    
    private TabHost tabHost;
    private ListView allAppsListView;
    private ListView thirdPartyAppsListView;
    private ListView systemAppsListView;
    private EditText searchEditText;
    private TextView appCountTextView;
    
    private AppManager appManager;
    private AppListAdapter allAppsAdapter;
    private AppListAdapter thirdPartyAppsAdapter;
    private AppListAdapter systemAppsAdapter;
    private AppListAdapter searchAdapter;
    
    private List<AppManager.AppInfo> allApps;
    private List<AppManager.AppInfo> thirdPartyApps;
    private List<AppManager.AppInfo> systemApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        
        initViews();
        initData();
        setupTabs();
        setupSearch();
        setupListeners();
    }

    private void initViews() {
        tabHost = findViewById(R.id.tabHost);
        allAppsListView = findViewById(R.id.listView_all_apps);
        thirdPartyAppsListView = findViewById(R.id.listView_third_party_apps);
        systemAppsListView = findViewById(R.id.listView_system_apps);
        searchEditText = findViewById(R.id.editText_search);
        appCountTextView = findViewById(R.id.textView_app_count);
    }

    private void initData() {
        appManager = AppManager.getInstance(this);
        
        // 获取应用列表
        allApps = appManager.getAllApps();
        thirdPartyApps = appManager.getThirdPartyApps();
        systemApps = appManager.getSystemApps();
        
        // 创建适配器
        allAppsAdapter = new AppListAdapter(this, allApps);
        thirdPartyAppsAdapter = new AppListAdapter(this, thirdPartyApps);
        systemAppsAdapter = new AppListAdapter(this, systemApps);
        
        // 设置适配器
        allAppsListView.setAdapter(allAppsAdapter);
        thirdPartyAppsListView.setAdapter(thirdPartyAppsAdapter);
        systemAppsListView.setAdapter(systemAppsAdapter);
        
        // 更新应用数量显示
        updateAppCount();
    }

    private void setupTabs() {
        tabHost.setup();
        
        // 所有应用标签
        TabHost.TabSpec allAppsTab = tabHost.newTabSpec("all");
        allAppsTab.setContent(R.id.tab_all_apps);
        allAppsTab.setIndicator("全部 (" + allApps.size() + ")");
        tabHost.addTab(allAppsTab);
        
        // 第三方应用标签
        TabHost.TabSpec thirdPartyTab = tabHost.newTabSpec("third_party");
        thirdPartyTab.setContent(R.id.tab_third_party_apps);
        thirdPartyTab.setIndicator("第三方 (" + thirdPartyApps.size() + ")");
        tabHost.addTab(thirdPartyTab);
        
        // 系统应用标签
        TabHost.TabSpec systemTab = tabHost.newTabSpec("system");
        systemTab.setContent(R.id.tab_system_apps);
        systemTab.setIndicator("系统 (" + systemApps.size() + ")");
        tabHost.addTab(systemTab);
        
        // 设置默认标签
        tabHost.setCurrentTab(1); // 默认显示第三方应用
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // 恢复原始列表
                    restoreOriginalLists();
                } else {
                    // 执行搜索
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        // 设置列表项点击监听器
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppListAdapter adapter = (AppListAdapter) parent.getAdapter();
                AppManager.AppInfo selectedApp = adapter.getItem(position);
                
                if (selectedApp != null) {
                    selectApp(selectedApp);
                }
            }
        };
        
        allAppsListView.setOnItemClickListener(itemClickListener);
        thirdPartyAppsListView.setOnItemClickListener(itemClickListener);
        systemAppsListView.setOnItemClickListener(itemClickListener);
    }

    private void performSearch(String query) {
        List<AppManager.AppInfo> searchResults = appManager.searchApps(query);
        
        if (searchAdapter == null) {
            searchAdapter = new AppListAdapter(this, searchResults);
        } else {
            searchAdapter.updateData(searchResults);
        }
        
        // 根据当前标签页更新对应的ListView
        int currentTab = tabHost.getCurrentTab();
        switch (currentTab) {
            case 0: // 所有应用
                allAppsListView.setAdapter(searchAdapter);
                break;
            case 1: // 第三方应用
                thirdPartyAppsListView.setAdapter(searchAdapter);
                break;
            case 2: // 系统应用
                systemAppsListView.setAdapter(searchAdapter);
                break;
        }
        
        updateAppCount("搜索结果: " + searchResults.size() + " 个应用");
    }

    private void restoreOriginalLists() {
        allAppsListView.setAdapter(allAppsAdapter);
        thirdPartyAppsListView.setAdapter(thirdPartyAppsAdapter);
        systemAppsListView.setAdapter(systemAppsAdapter);
        updateAppCount();
    }

    private void selectApp(AppManager.AppInfo appInfo) {
        Log.d(TAG, "Selected app: " + appInfo.appName + " (" + appInfo.packageName + ")");
        
        // 返回选择结果
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_app_name", appInfo.appName);
        resultIntent.putExtra("selected_app_package", appInfo.packageName);
        resultIntent.putExtra(GestureConst.KEY_SELECTED_ACTION_TYPE, GestureConst.OPERATOR_TYPE_APP);
        
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        
        Toast.makeText(this, "已选择: " + appInfo.appName, Toast.LENGTH_SHORT).show();
    }

    private void updateAppCount() {
        String countText = String.format("共 %d 个应用 (第三方: %d, 系统: %d)", 
                appManager.getAppCount(), 
                appManager.getThirdPartyAppCount(), 
                appManager.getSystemAppCount());
        updateAppCount(countText);
    }

    private void updateAppCount(String text) {
        if (appCountTextView != null) {
            appCountTextView.setText(text);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
    }
}
