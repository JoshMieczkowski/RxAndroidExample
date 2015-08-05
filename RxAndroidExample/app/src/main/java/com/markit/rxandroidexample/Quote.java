package com.markit.rxandroidexample;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by josh.mieczkowski on 8/4/2015.
 */
public class Quote implements Parcelable {
    private String symbol;
    private String name;
    private String exchange;
    private String lastPrice;
    private String change;
    private String changePercent;
    private String volume;

    public static Observable<Quote> getQuote(final String symbol){
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22 "
                                + symbol + "%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(!jsonObject.isNull("query")) {
                        JSONObject jsonQuery = jsonObject.getJSONObject("query");
                        if(!jsonQuery.isNull("results")){
                            JSONObject jsonResults = jsonQuery.getJSONObject("results");
                            if(!jsonResults.isNull("quote")){
                                JSONObject jsonQuote = jsonResults.getJSONObject("quote");
                                if(!jsonQuote.isNull("Bid")) {
                                    subscriber.onNext(jsonQuote);
                                    subscriber.onCompleted();
                                }
                            }
                        }

                    }
                    subscriber.onError(new Throwable("Not a valid symbol"));
                } catch (Exception e) {
                    subscriber.onError(e);
                }

            }
        }).map(new Func1<JSONObject, Quote>() {
            @Override
            public Quote call(JSONObject json) {
                try {
                    Quote quote = new Quote();
                    quote.setSymbol(json.getString("Symbol"));
                    quote.setName(json.getString("Name"));
                    quote.setExchange(json.getString("StockExchange"));
                    quote.setLastPrice(json.getString("Bid"));
                    quote.setChange(json.getString("Change"));
                    quote.setChangePercent(json.getString("Change_PercentChange"));
                    quote.setVolume(json.getString("Volume"));

                    return quote;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }).filter(new Func1<Quote, Boolean>() {
            @Override
            public Boolean call(Quote quote) {
                return quote != null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.symbol);
        dest.writeString(this.name);
        dest.writeString(this.exchange);
        dest.writeString(this.lastPrice);
        dest.writeString(this.change);
        dest.writeString(this.changePercent);
        dest.writeString(this.volume);
    }

    public Quote() {
    }

    protected Quote(Parcel in) {
        this.symbol = in.readString();
        this.name = in.readString();
        this.exchange = in.readString();
        this.lastPrice = in.readString();
        this.change = in.readString();
        this.changePercent = in.readString();
        this.volume = in.readString();
    }

    public static final Parcelable.Creator<Quote> CREATOR = new Parcelable.Creator<Quote>() {
        public Quote createFromParcel(Parcel source) {
            return new Quote(source);
        }

        public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };
}
