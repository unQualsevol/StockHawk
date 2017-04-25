package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.utils.NetworkUtils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    //Error Codes
    public static final int STOCK_STATUS_OK = 0;
    public static final int STOCK_STATUS_SERVER_DOWN = 1;
    public static final int STOCK_STATUS_UNKNOWN = 2;
    public static final int STOCK_STATUS_EMPTY = 3;
    public static final int STOCK_STATUS_INVALID = 4;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);
        String invalid = null;

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            int countRequestItems = stockPref.size();
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (countRequestItems == 0) {
                setStockStatus(context, STOCK_STATUS_EMPTY);
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);

            if (quotes.isEmpty()) {
                setStockStatus(context, STOCK_STATUS_SERVER_DOWN);
                return;
            }

            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();
            while (iterator.hasNext()) {
                String symbol = iterator.next();

                Stock stock = quotes.get(symbol);
                //if stock name is null then does not exists
                if(stock == null || stock.getName() == null) {
                    PrefUtils.removeStock(context, symbol);
                    PrefUtils.saveInvalidStock(context, symbol, false);
                    continue;
                }
                StockQuote quote = stock.getQuote();

                String stockName = stock.getName();
                String exchangeName = stock.getStockExchange();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                float dayHighest = (quote.getDayHigh() == null)? -1: quote.getDayHigh().floatValue();
                float dayLowest = (quote.getDayLow() == null)? -1: quote.getDayLow().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x

                String dailyHistory;
                String weeklyHistory;
                String monthlyHistory;
                // don't know why for SSS get history fails x_X
                try {

                    from = Calendar.getInstance();
                    from.add(Calendar.DAY_OF_YEAR, -5);
                    dailyHistory = getHistory(stock, from, to, Interval.DAILY);

                    from = Calendar.getInstance();
                    from.add(Calendar.DAY_OF_YEAR, -35);
                    weeklyHistory = getHistory(stock, from, to, Interval.WEEKLY);

                    from = Calendar.getInstance();
                    from.add(Calendar.MONTH, -4);
                    monthlyHistory = getHistory(stock, from, to, Interval.MONTHLY);
                } catch (IOException ioe) {
                    invalid = symbol;
                    continue;
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_DAILY_HISTORY, dailyHistory);
                quoteCV.put(Contract.Quote.COLUMN_WEEKLY_HISTORY, weeklyHistory);
                quoteCV.put(Contract.Quote.COLUMN_MONTHLY_HISTORY, monthlyHistory);
                quoteCV.put(Contract.Quote.COLUMN_DAY_HIGHEST, dayHighest);
                quoteCV.put(Contract.Quote.COLUMN_DAY_LOWEST, dayLowest);
                quoteCV.put(Contract.Quote.COLUMN_STOCK_NAME, stockName);
                quoteCV.put(Contract.Quote.COLUMN_STOCK_EXCHANGE, exchangeName);

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));
            int countValidStocks = PrefUtils.getStocks(context).size();
            if(!TextUtils.isEmpty(invalid)) {
                PrefUtils.removeStock(context, invalid);
                PrefUtils.saveInvalidStock(context, invalid, true);
                return;
            } else if(countValidStocks == 0) {
                setStockStatus(context, STOCK_STATUS_EMPTY);
            } else {
                setStockStatus(context, STOCK_STATUS_OK);
            }
        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
            setStockStatus(context, STOCK_STATUS_SERVER_DOWN);
        } catch (Exception e) {
            Timber.e(e, "Unknown Error");
            setStockStatus(context, STOCK_STATUS_UNKNOWN);
        } finally {
            updateWidget(context);
        }
    }

    private static String getHistory(Stock stock, Calendar from, Calendar to, Interval interval) throws IOException {

        List<HistoricalQuote> history = stock.getHistory(from, to, interval);

        // time to time it returns less than 5 results, for example:
        // to solve that I tried to retrieve history more times and seems to work
//        if (interval.equals(Interval.DAILY)) {
//            while (history.size() < 5) {
//                history = stock.getHistory(from, to, interval);
//                from.add(Calendar.DAY_OF_YEAR, -1);
//            }
//        }

        StringBuilder historyBuilder = new StringBuilder();
        for (HistoricalQuote it : history) {
            historyBuilder.append(it.getDate().getTimeInMillis());
            historyBuilder.append(", ");
            historyBuilder.append(it.getClose());
            historyBuilder.append("\n");
        }
        return null;
    }

    public static void updateWidget(Context context) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        context.sendBroadcast(dataUpdatedIntent);
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        if (NetworkUtils.isNetworkUp(context)) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }

    private static void setStockStatus(Context c, @StockStatus int stockStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_stock_status_key), stockStatus);
        spe.apply();
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STOCK_STATUS_OK, STOCK_STATUS_SERVER_DOWN, STOCK_STATUS_UNKNOWN, STOCK_STATUS_EMPTY, STOCK_STATUS_INVALID})
    public @interface StockStatus {
    }
}
