package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.model.Stock;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    Stock getDataAtPosition(int position) {
        cursor.moveToPosition(position);
        Stock stock = new Stock();
        stock.setId(cursor.getInt(Contract.Quote.POSITION_ID));
        stock.setSymbol(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        stock.setPrice(cursor.getFloat(Contract.Quote.POSITION_PRICE));
        stock.setAbsoluteChange(cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
        stock.setPercentageChange(cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE));
        stock.setDayHistory(cursor.getString(Contract.Quote.POSITION_DAILY_HISTORY));
        stock.setWeekHistory(cursor.getString(Contract.Quote.POSITION_WEEKLY_HISTORY));
        stock.setMonthHistory(cursor.getString(Contract.Quote.POSITION_MONTHLY_HISTORY));
        stock.setStockExchange(cursor.getString(Contract.Quote.POSITION_STOCK_EXCHANGE));
        stock.setStockName(cursor.getString(Contract.Quote.POSITION_STOCK_NAME));
        stock.setDayHighest(cursor.getFloat(Contract.Quote.POSITION_DAY_HIGHEST));
        stock.setDayLowest(cursor.getFloat(Contract.Quote.POSITION_DAY_LOWEST));
        return stock;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);


        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));
        holder.price.setContentDescription(context.getString(R.string.stock_price_cd, holder.price.getText()));


        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.change.setText(change);
        } else {
            holder.change.setText(percentage);
        }

        int color;
        int contentDescription;

        if(rawAbsoluteChange == 0.00) {
            color = R.drawable.percent_change_pill_blue;
            contentDescription = R.string.stock_decreased_cd;
        } else if (rawAbsoluteChange > 0) {
            color = R.drawable.percent_change_pill_green;
            contentDescription = R.string.stock_increased_cd;
        } else {
            color = R.drawable.percent_change_pill_red;
            contentDescription = R.string.stock_decreased_cd;
        }
        holder.change.setBackgroundResource(color);
        holder.change.setContentDescription(
                String.format(context.getString(contentDescription), holder.change.getText()));


    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(StockViewHolder viewHolder, Stock data);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            clickHandler.onClick(this, getDataAtPosition(adapterPosition));
        }


    }
}
