package party.hunchbacktank.isthereanydeal;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import party.hunchbacktank.isthereanydeal.data.adapter.DealsAdapter;
import party.hunchbacktank.isthereanydeal.model.deals.Deal;
import party.hunchbacktank.isthereanydeal.model.deals.DealResponse;
import party.hunchbacktank.isthereanydeal.networking.deal.DealEndpoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Deal> deals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        setSupportActionBar(toolbar);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //TODO Sort out adapter
        mAdapter = new DealsAdapter(deals);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    public void getDeals() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://api.isthereanydeal.com")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();

        DealEndpoint dealEndpoint = retrofit.create(DealEndpoint.class);

        String apiKey = BuildConfig.API_KEY;
        Call<DealResponse> call = dealEndpoint.fetch("UK", apiKey, "UK", 0, 20);
        call.enqueue(new Callback<DealResponse>() {
            @Override
            public void onResponse(Call<DealResponse> call, retrofit2.Response<DealResponse> response) {
                if (response.body() != null) {
                    List<Deal> dealList = response.body().getData().getDeals();
                    deals.addAll(dealList);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<DealResponse> call, Throwable e) {
                e.printStackTrace();
                //TODO Prompt for second attempt, explain error to user
            }
        });
    }

}
