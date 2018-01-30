package com.reactlibrary.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactlibrary.R;
import com.reactlibrary.react.ReactAppCompatActivity;
import com.reactlibrary.util.TurbolinksAction;
import com.reactlibrary.util.TurbolinksRoute;

import static com.reactlibrary.RNTurbolinksModule.INTENT_INITIAL_VISIT;
import static com.reactlibrary.RNTurbolinksModule.INTENT_NAVIGATION_BAR_HIDDEN;

public class NativeActivity extends ReactAppCompatActivity {

    private TurbolinksRoute route;
    private Boolean navigationBarHidden;
    private Boolean initialVisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        route = new TurbolinksRoute(getIntent());
        initialVisit = getIntent().getBooleanExtra(INTENT_INITIAL_VISIT, true);
        navigationBarHidden = getIntent().getBooleanExtra(INTENT_NAVIGATION_BAR_HIDDEN, false);
        setContentView(R.layout.activity_native);
        renderToolBar();
        renderReactRootView();
    }

    @Override
    public void onBackPressed() {
        if (initialVisit) {
            moveTaskToBack(true);
        } else {
            if (!route.getModal()) super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (route.getActions() == null) return true;
        getMenuInflater().inflate(R.menu.turbolinks_menu, menu);
        for (int i = 0; i < route.getActions().size(); i++) {
            Bundle bundle = route.getActions().get(i);
            TurbolinksAction action = new TurbolinksAction(bundle);
            MenuItem menuItem = menu.add(Menu.NONE, Menu.NONE, i, action.getTitle());
            renderActionIcon(menu, menuItem, action.getIcon());
            if (action.getButton()) menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        WritableMap params = Arguments.createMap();
        params.putString("url", null);
        params.putString("path", null);
        params.putString("component", route.getComponent());
        params.putInt("position", item.getOrder());
        getEventEmitter().emit("turbolinksActionSelected", params);
        return super.onOptionsItemSelected(item);
    }

    private void renderReactRootView() {
        ReactRootView mReactRootView = (ReactRootView) findViewById(R.id.native_view);
        ReactInstanceManager mReactInstanceManager = getReactInstanceManager();
        mReactRootView.startReactApplication(mReactInstanceManager, route.getComponent(), route.getPassProps());
    }

    private void renderToolBar() {
        Toolbar turbolinksToolbar = (Toolbar) findViewById(R.id.native_toolbar);
        setSupportActionBar(turbolinksToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!initialVisit);
        getSupportActionBar().setDisplayShowHomeEnabled(!initialVisit);
        getSupportActionBar().setTitle(route.getTitle());
        getSupportActionBar().setSubtitle(route.getSubtitle());
        handleTitlePress(turbolinksToolbar);
        if (navigationBarHidden || route.getModal()) getSupportActionBar().hide();
    }

    private void handleTitlePress(Toolbar toolbar) {
        toolbar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                WritableMap params = Arguments.createMap();
                params.putString("component", route.getComponent());
                params.putString("url", null);
                params.putString("path", null);
                getEventEmitter().emit("turbolinksTitlePress", params);
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void renderActionIcon(Menu menu, MenuItem menuItem, Bundle icon) {
        if (icon == null) return;
        if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);
        Uri uri = Uri.parse(icon.getString("uri"));
        Drawable drawableIcon = Drawable.createFromPath(uri.getPath());
        menuItem.setIcon(drawableIcon);
    }

    private DeviceEventManagerModule.RCTDeviceEventEmitter getEventEmitter() {
        return getReactInstanceManager().getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

}
