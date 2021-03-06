package party.hunchbacktank.lowscore.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import io.realm.Realm;
import io.realm.RealmResults;
import party.hunchbacktank.lowscore.R;
import party.hunchbacktank.lowscore.adapters.ViewPagerAdapter;
import party.hunchbacktank.lowscore.fragments.GameInfo;
import party.hunchbacktank.lowscore.fragments.GamePrices;
import party.hunchbacktank.lowscore.helpers.AppDetailDeserializer;
import party.hunchbacktank.lowscore.helpers.PicassoSwitcherHelper;
import party.hunchbacktank.lowscore.model.Game;
import party.hunchbacktank.lowscore.model.steam.appdetails.AppDetail;
import party.hunchbacktank.lowscore.model.steam.appdetails.Screenshot;
import party.hunchbacktank.lowscore.networking.steam.AppDetailsEndpoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayGameActivity extends AppCompatActivity {
    @BindView(R.id.gamescreens) ImageSwitcher imageSwitcher;
    private PicassoSwitcherHelper picassoSwitcherHelper;
    private List<Uri> imageUris = new ArrayList<>();
    private int currentScreenshot;
    private int switcherHeight;
    private Realm realm;

    @BindView(R.id.controller_support) ImageView controller;
    @BindView(R.id.switcher_progress) ProgressBar switcherProgress;
    @BindView(R.id.app_bar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.game_detail_tabs) TabLayout tabLayout;
    @BindView(R.id.game_detail_viewpager) ViewPager viewPager;
    @BindView(R.id.gamescreens_overlay) RelativeLayout overlay;
    @BindView(R.id.overlay_text) TextView overlayText;

    private boolean titleListenerSet;
    private AppDetail appDetail;
    private ViewPagerAdapter viewPagerAdapter;
    private float x1;
    static final int MIN_DISTANCE = 150;
    private String plain;
    private String appid;
    private String TAG = "DisplayGameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_game);
        ButterKnife.bind(this);
        collapsingToolbar.setTitle("");
        realm = Realm.getDefaultInstance();

        //Toolbar
        plain = getIntent().getStringExtra("plainName");
        setSupportActionBar(toolbar);
        //Image Switcher
        setupImageSwitcher();
        picassoSwitcherHelper = new PicassoSwitcherHelper(this, imageSwitcher, overlay);
        getAppDetails(plain);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume(){
        super.onResume();
        collapsingToolbar.setTitle("");
    }

    //region LoadInfo
    private void getAppDetails(String plain) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AppDetail.class, new AppDetailDeserializer());
        Gson gson = gsonBuilder.create();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(getString(R.string.steam_api_base))
                .addConverterFactory(GsonConverterFactory.create(gson));
        Retrofit retrofit = builder.build();
        AppDetailsEndpoint appDetailsEndpoint = retrofit.create(AppDetailsEndpoint.class);

        RealmResults<Game> gameRealmResults = realm.where(Game.class).equalTo("plain", plain).findAll();
        if (!gameRealmResults.isEmpty() && gameRealmResults.first().getSteamAppId() != 0 ) {
            final String appid = Integer.toString(gameRealmResults.first().getSteamAppId());
        Call<Map<String, AppDetail>> call = appDetailsEndpoint.get(appid);
        call.enqueue(new Callback<Map<String, AppDetail>>() {
            @Override
            public void onResponse(Call<Map<String, AppDetail>> call, retrofit2.Response<Map<String, AppDetail>> response) {
                if (response.body() != null) {
                    appDetail = response.body().get(appid);
                    setUI();
                }
            }

            @Override
            public void onFailure(Call<Map<String, AppDetail>> call, Throwable e) {
                Log.e(TAG, e.getStackTrace().toString());
                //TODO Prompt for second attempt, explain error to user
            }
        });
    }
    }

    public void setTitleListenerFlag(){
        titleListenerSet = true;
    }

    public void setUI() {
        //TODO Figure out how to make the icons disappear without this dodgy code XML? Parallax?
        toolbar.setTitle(appDetail.getData().getName());
        //Set so title is hidden when not collapsed
        if (!titleListenerSet) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                double scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = -0.75*appBarLayout.getTotalScrollRange();
                    }
                    if (verticalOffset <= scrollRange) {
                        //collapsingToolbar.setTitle(appDetail.getData().getName());
                        controller.setVisibility(View.INVISIBLE);
                    } else {
                        //collapsingToolbar.setTitle("");
                        controller.setVisibility(View.VISIBLE);
                    }
                }
            });
            titleListenerSet = true;
        }
        List<Screenshot> screenshots = appDetail.getData().getScreenshots();
        if (screenshots != null) {
            for (Screenshot screenshot : screenshots) {
                imageUris.add(Uri.parse(screenshot.getPathFull()));
            }
            Picasso.with(this).load(imageUris.get(0))
                    .into(picassoSwitcherHelper);
            currentScreenshot = 0;
            switcherProgress.setMax(screenshots.size());
            switcherProgress.setProgress(1);
    }
        if (appDetail.getData().getControllerSupport() !=null && appDetail.getData().getControllerSupport().toLowerCase().equals("full")){
            Picasso.with(this).load(R.drawable.controller).into(controller);
        }

    }
    //endregion

    //region ImageSwitcher
    public ProgressBar getSwitcherProgress() {
        return switcherProgress;
    }

    public int getCurrentScreenshot() {
        return currentScreenshot;
    }
    protected void setupImageSwitcher() {
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView view = new ImageView(getApplicationContext());
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                view.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT,ImageSwitcher.LayoutParams.MATCH_PARENT));
                //Makes the image float to the top like we want
                view.setAdjustViewBounds(true);
                return view;
            }
        });
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        imageSwitcher.setInAnimation(in);
        imageSwitcher.setOutAnimation(out);
    }
    //http://stackoverflow.com/questions/6645537/how-to-detect-the-swipe-left-or-right-in-android
    @OnTouch(R.id.gamescreens)
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                Float x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // Left to Right swipe action
                    if (x2 > x1 )
                    {
                        if (imageUris !=null && currentScreenshot > 0) {
                            currentScreenshot--;
                            switcherProgress.setIndeterminate(true);
                            Picasso.with(this).load(imageUris.get(currentScreenshot))
                                    .into(picassoSwitcherHelper);

                        }
                    }

                    // Right to left swipe action
                    else if (imageUris != null && currentScreenshot < imageUris.size()-1 )
                    {
                        currentScreenshot++;
                        switcherProgress.setIndeterminate(true);
                        Picasso.with(this).load(imageUris.get(currentScreenshot))
                                .into(picassoSwitcherHelper);
                    }
                }
                else
                {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return true;
    }
    //endregion

    //region Tabs
    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        GamePrices gamePrices = GamePrices.newInstance(plain);
        viewPagerAdapter.addFragment(gamePrices, "Stores");
        viewPagerAdapter.addFragment(new GameInfo(), "Info");
        viewPager.setAdapter(viewPagerAdapter);
    }

    //endregion

    //region Menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_refresh){
           getAppDetails(plain);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //endregion

}
