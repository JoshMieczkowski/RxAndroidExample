package com.markit.rxandroidexample;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
    private static final String DATA_TAG = "DATA_TAG";
    Button btnLookup;
    EditText editSymbol;
    ListView listQuote;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private Quote quote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLookup = (Button)findViewById(R.id.btnLookup);
        editSymbol = (EditText)findViewById(R.id.editSymbol);
        listQuote = (ListView) findViewById(R.id.listQuote);

        if(savedInstanceState != null && savedInstanceState.containsKey(DATA_TAG)){
            setQuote((Quote)savedInstanceState.getParcelable(DATA_TAG));
        }

        btnLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String symbol = editSymbol.getText().toString();
                if (symbol.length() > 0) {
                    Observable<Quote> quoteObservable = Quote.getQuote(symbol).cache();
                    compositeSubscription.add(quoteObservable.subscribe(new Observer<Quote>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Snackbar.make(btnLookup, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onNext(Quote quote) {
                            setQuote(quote);
                        }
                    }));

                    compositeSubscription.add(quoteObservable.subscribe(new Observer<Quote>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Quote quote) {
                            Snackbar.make(btnLookup, quote.getSymbol() + " - " + quote.getName(), Snackbar.LENGTH_LONG).show();
                        }
                    }));

                } else {
                    Snackbar.make(view, "Please enter a symbol", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(quote != null){
            outState.putParcelable(DATA_TAG, quote);
        }
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.unsubscribe();
        compositeSubscription.clear();

        super.onDestroy();
    }

    public void setQuote(Quote quote){
        this.quote = quote;

        ArrayList<String> data = new ArrayList<String>();
        data.add("Symbol: " + quote.getSymbol());
        data.add("Name: " + quote.getName());
        data.add("Exchange: " + quote.getExchange());
        data.add("LastPrice: " + quote.getLastPrice());
        data.add("Change: " + quote.getChange());
        data.add("ChangePercent: " + quote.getChangePercent());
        data.add("Volume: " + quote.getVolume());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, data);
        listQuote.setAdapter(arrayAdapter);
    }
}
