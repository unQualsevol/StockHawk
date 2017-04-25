package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.model.Stock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    private static final int PAGE_BUFFER = 2;
    @BindView(R.id.tb_detail_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.pager)
    public ViewPager mViewPager;
    @BindView(R.id.tabs)
    public TabLayout mTabLayout;
    @BindView(R.id.tv_stock_name)
    public TextView mStockNameTextView;
    @BindView(R.id.tv_day_highest)
    public TextView mDayHighestTextView;
    @BindView(R.id.tv_stock_exchange)
    public TextView mStockExchangeTextView;
    @BindView(R.id.tv_day_lowest)
    public TextView mDayLowestTextView;
    @BindView(R.id.tv_stock_price)
    public TextView mStockPriceTextView;
    @BindView(R.id.tv_absolute_change)
    public TextView mAbsoluteChangeTextView;
    @BindView(R.id.stock_detail_container)
    public CardView mStockDetailCardView;

    private Stock mStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState != null) {
            //TODO: restore status
        }


        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(getString(R.string.stock_data_key))) {
                mStock = intentThatStartedThisActivity.getParcelableExtra(getString(R.string.stock_data_key));
                updateView(mStock);
            }
        }

        setupViewPager();
        mTabLayout.setupWithViewPager(mViewPager, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView(Stock stock) {
        mStockDetailCardView.setContentDescription(
                getString(R.string.stock_detail_activity_cd, stock.getStockName()));
        mStockNameTextView.setText(stock.getStockName());
        mStockExchangeTextView.setText(stock.getStockExchange());

        DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
        dollarFormat.setMaximumFractionDigits(2);
        dollarFormat.setMinimumFractionDigits(2);

        if (stock.getDayHighest() == -1) {
            mDayHighestTextView.setVisibility(View.GONE);
        } else {
            mDayHighestTextView.setText(dollarFormat.format(stock.getDayHighest()));
            mDayHighestTextView.setContentDescription(getString(R.string.day_highest_cd, mDayHighestTextView.getText()));
        }

        if (stock.getDayLowest() == -1) {
            mDayLowestTextView.setVisibility(View.GONE);
        } else {
            mDayLowestTextView.setText(dollarFormat.format(stock.getDayLowest()));
            mDayLowestTextView.setContentDescription(getString(R.string.day_lowest_cd, mDayLowestTextView.getText()));
        }

        mStockPriceTextView.setText(dollarFormat.format(stock.getPrice()));

        Float change = stock.getAbsoluteChange();
        mAbsoluteChangeTextView.setText(dollarFormat.format(change));

        int color;
        int contentDescription;
        if (change == 0.00) {
            color = R.drawable.percent_change_pill_blue;
            contentDescription = R.string.stock_decreased_cd;
        } else if (change > 0) {
            color = R.drawable.percent_change_pill_green;
            contentDescription = R.string.stock_increased_cd;
        } else {
            color = R.drawable.percent_change_pill_red;
            contentDescription = R.string.stock_decreased_cd;
        }
        mAbsoluteChangeTextView.setBackgroundResource(color);
        mAbsoluteChangeTextView.setContentDescription(
                String.format(getString(contentDescription), mAbsoluteChangeTextView.getText()));
    }

    private void setupViewPager() {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(buildDetailFragment(R.string.period_daily_key, mStock.getDayHistory()), getString(R.string.days_fragment_title));
        viewPagerAdapter.addFragment(buildDetailFragment(R.string.period_weekly_key, mStock.getWeekHistory()), getString(R.string.weeks_fragment_title));
        viewPagerAdapter.addFragment(buildDetailFragment(R.string.period_monthly_key, mStock.getMonthHistory()), getString(R.string.months_fragment_title));

        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.setOffscreenPageLimit(PAGE_BUFFER);
    }

    private DetailFragment buildDetailFragment(@PeriodKeys int periodTypeKey, String periodValue) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.fragment_period_key), getString(periodTypeKey));
        bundle.putString(getString(R.string.period_value_key), periodValue);
        detailFragment.setArguments(bundle);
        return detailFragment;
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({R.string.period_monthly_key, R.string.period_weekly_key, R.string.period_daily_key})
    public @interface PeriodKeys {
    }
}
