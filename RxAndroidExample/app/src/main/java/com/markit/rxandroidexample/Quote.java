package com.markit.rxandroidexample;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.markit.rxandroidexample.utilities.JacksonParser;
import com.markit.rxandroidexample.utilities.OkHttpSingleton;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by josh.mieczkowski on 8/4/2015.
 */
public class Quote implements Parcelable {
    private static final int NUMBER_OF_RETRIES = 5;
    private static final int DELAY_IN_MS = 5000;

    @JsonProperty("Symbol")
    private String symbol;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("StockExchange")
    private String exchange;

    @JsonProperty("Bid")
    private String lastPrice;

    @JsonProperty("Change")
    private String change;

    @JsonProperty("Change_PercentChange")
    private String changePercent;

    @JsonProperty("Volume")
    private String volume;

    public static Observable<Quote> getQuoteWtihTimer(final String symbol){
        return Observable.timer(0, 5, TimeUnit.SECONDS).concatMap(new Func1<Long, Observable<Quote>>() {
            @Override
            public Observable<Quote> call(Long aLong) {
                return getQuote(symbol);
            }
        });
    }

    public static Observable<String> getJSON(final String url){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient client = OkHttpSingleton.getInstance().getOkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();

                } catch (Exception e) {
                    subscriber.onError(e);
                }

            }
        }).filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String json) {
                return json != null;
            }
        });
    }

    public static Observable<Quote> getQuote(String symbol){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("query.yahooapis.com")
                .appendPath("v1")
                .appendPath("public")
                .appendPath("yql")
                .appendQueryParameter("q", "select * from yahoo.finance.quotes where symbol in (%22"
                        + symbol + "%22)")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("diagnostics", "true")
                .appendQueryParameter("env", "store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
                .appendQueryParameter("callback", "");

        return getJSON(builder.build().toString()).map(new Func1<String, String>() {
            @Override
            public String call(String json) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if(!jsonObject.isNull("query")) {
                        JSONObject jsonQuery = jsonObject.getJSONObject("query");
                        if(!jsonQuery.isNull("results")){
                            JSONObject jsonResults = jsonQuery.getJSONObject("results");
                            if(!jsonResults.isNull("quote")){
                                JSONObject jsonQuote = jsonResults.getJSONObject("quote");
                                if(!jsonQuote.isNull("Bid")) {
                                    return jsonQuote.toString();
                                }
                            }
                        }

                    }

                    throw new Throwable("Not a valid symbol");
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return null;
            }
        }).map(new Func1<String, Quote>() {

            @Override
            public Quote call(String json) {
                Quote quote = null;
                try {
                    quote = JacksonParser.getInstance().getObjectMapper().readValue(json, Quote.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return quote;
            }
        }).filter(new Func1<Quote, Boolean>() {
            @Override
            public Boolean call(Quote quote) {
                return quote != null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWithDelay(NUMBER_OF_RETRIES, DELAY_IN_MS));
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
