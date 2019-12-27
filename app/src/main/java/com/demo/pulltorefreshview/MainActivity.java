package com.demo.pulltorefreshview;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.pulltorefreshview.pulltorefresh.PulltoRefreshview;
import com.demo.pulltorefreshview.pulltorefresh.RefreshListener;

public class MainActivity extends AppCompatActivity {

    private PulltoRefreshview myPulltoview;
    private ListView lvList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myPulltoview = findViewById(R.id.myPulltoview);
        lvList = findViewById(R.id.lv_list);
        initRefresh();
        lvList.setAdapter(new MyAdapter(this));
    }

    private void initRefresh() {
        myPulltoview.setRefreshListener(new RefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myPulltoview.complete();
                    }
                }, 1000);
            }

        });
    }

    private class MyAdapter extends BaseAdapter{
        private Context context;
        public MyAdapter(Context context) {
            this.context=context;
        }

        @Override
        public int getCount() {
            return 50;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView= LayoutInflater.from(context).inflate(R.layout.footer_view,null);
            return convertView;
        }
    }
}
