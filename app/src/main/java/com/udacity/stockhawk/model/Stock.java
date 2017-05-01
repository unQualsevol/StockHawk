package com.udacity.stockhawk.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Stock implements Parcelable {

    private int id;
    private String symbol;
    private Float price;
    private Float absoluteChange;
    private Float percentageChange;
    private String monthHistory;
    private String weekHistory;
    private String dayHistory;
    private String stockExchange;
    private String stockName;
    private Float dayLowest;
    private Float dayHighest;

    public Stock() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getAbsoluteChange() {
        return absoluteChange;
    }

    public void setAbsoluteChange(Float absoluteChange) {
        this.absoluteChange = absoluteChange;
    }

    public Float getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(Float percentageChange) {
        this.percentageChange = percentageChange;
    }

    public String getMonthHistory() {
        return monthHistory;
    }

    public void setMonthHistory(String monthHistory) {
        this.monthHistory = monthHistory;
    }

    public String getWeekHistory() {
        return weekHistory;
    }

    public void setWeekHistory(String weekHistory) {
        this.weekHistory = weekHistory;
    }

    public String getDayHistory() {
        return dayHistory;
    }

    public void setDayHistory(String dayHistory) {
        this.dayHistory = dayHistory;
    }

    public String getStockExchange() {
        return stockExchange;
    }

    public void setStockExchange(String stockExchange) {
        this.stockExchange = stockExchange;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public Float getDayLowest() {
        return dayLowest;
    }

    public void setDayLowest(Float dayLowest) {
        this.dayLowest = dayLowest;
    }

    public Float getDayHighest() {
        return dayHighest;
    }

    public void setDayHighest(Float dayHighest) {
        this.dayHighest = dayHighest;
    }

    public Stock(Parcel source) {
        id = source.readInt();
        symbol = source.readString();
        price = source.readFloat();
        absoluteChange = source.readFloat();
        percentageChange = source.readFloat();
        monthHistory = source.readString();
        weekHistory = source.readString();
        dayHistory = source.readString();
        stockExchange = source.readString();
        stockName = source.readString();
        dayLowest = source.readFloat();
        dayHighest = source.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(symbol);
        dest.writeFloat(price);
        dest.writeFloat(absoluteChange);
        dest.writeFloat(percentageChange);
        dest.writeString(monthHistory);
        dest.writeString(weekHistory);
        dest.writeString(dayHistory);
        dest.writeString(stockExchange);
        dest.writeString(stockName);
        dest.writeFloat(dayLowest);
        dest.writeFloat(dayHighest);
    }

    public static final Creator<Stock> CREATOR = new Creator<Stock>() {
        @Override
        public Stock createFromParcel(Parcel in) {
            return new Stock(in);
        }

        @Override
        public Stock[] newArray(int size) {
            return new Stock[size];
        }
    };
}
