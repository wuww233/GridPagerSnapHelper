package com.www233.gridpagersnaphelper;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        LinearLayout.LayoutParams lp_horizontal = new LinearLayout.LayoutParams(1100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp_horizontal.setMargins(10, 0, 10, 50);
        LinearLayout.LayoutParams lp_vertical = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1320);
        initRecycler(lp_horizontal, 15, 3, 0);
        initRecycler(lp_vertical, 12, 2, 1);

    }

    private void initRecycler(LinearLayout.LayoutParams lp, int page_limit, int row, int type) {
        List<View> list = new ArrayList<>();
        TextView tv;
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(30, 30);
        lp2.setMargins(5, 5, 5, 5);
        for (int i = 0; i < 51; i++) {
            tv = new TextView(this);
            tv.setText("text");
            if (i % page_limit == 0)
                tv.setBackgroundColor(getColor(R.color.blue));
            else
                tv.setBackgroundColor(getColor(R.color.red));
            tv.setLayoutParams(lp2);
            list.add(tv);
        }
        RecyclerView buttonPageScroll;
        if (type == 0) {
            buttonPageScroll = findViewById(R.id.rv1);
        } else {
            buttonPageScroll = findViewById(R.id.rv2);
        }
        buttonPageScroll.setLayoutParams(lp);
        buttonPageScroll.setBackgroundColor(getColor(R.color.green_light));
        ButtonPageScrollAdapter myAdapter = new ButtonPageScrollAdapter(page_limit, 50);

        buttonPageScroll.setAdapter(myAdapter);
        GridLayoutManager gridLayoutManager;
        if (type == 0)
            gridLayoutManager = new GridLayoutManager(this, row, GridLayoutManager.HORIZONTAL, false);
        else
            gridLayoutManager = new GridLayoutManager(this, row, GridLayoutManager.VERTICAL, false);

        buttonPageScroll.setLayoutManager(gridLayoutManager);
        GridPagerSnapHelper snapHelper = new GridPagerSnapHelper(row, page_limit);
        snapHelper.attachToRecyclerView(buttonPageScroll);
        snapHelper.setOnPageChangeListener((pageBeforeChange, pageAfterChange) -> Log.i(TAG, String.format("onChange: %d -> %d", pageBeforeChange, pageAfterChange)));

        Button btn1, btn2;

        if (type == 0) {
            btn1 = findViewById(R.id.btn_1);
            btn2 = findViewById(R.id.btn_2);
            btn1.setOnClickListener(v -> snapHelper.scrollToPage(0));
            btn2.setOnClickListener(v -> snapHelper.scrollToPage(snapHelper.getPageCount() - 1));
        } else {
            btn1 = findViewById(R.id.btn_3);
            btn2 = findViewById(R.id.btn_4);
            btn1.setOnClickListener(v -> snapHelper.smoothScrollToPage(0));
            btn2.setOnClickListener(v -> snapHelper.smoothScrollToPage(snapHelper.getPageCount() - 1));
        }


    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    class ButtonPageScrollAdapter extends RecyclerView.Adapter<ButtonPageScrollViewHolder> {

        int page_limit;
        int all_size;

        public ButtonPageScrollAdapter(int page_limit, int all_size) {
            this.page_limit = page_limit;
            this.all_size = all_size;
        }

        @NonNull
        @Override
        public ButtonPageScrollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new TextView(parent.getContext());
            return new ButtonPageScrollViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ButtonPageScrollViewHolder holder, int position) {
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(200, 200);
            lp2.setMargins(10, 10, 10, 10);
            if (position % page_limit == 0)
                holder.view.setBackgroundColor(getResources().getColor(R.color.red, getTheme()));
            else
                holder.view.setBackgroundColor(getResources().getColor(R.color.blue, getTheme()));
            holder.view.setLayoutParams(lp2);
            ((TextView) holder.view).setText(String.valueOf(position));
        }

        @Override
        public int getItemCount() {
            return all_size;
        }
    }

    static class ButtonPageScrollViewHolder extends RecyclerView.ViewHolder {
        View view;

        public ButtonPageScrollViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
        }
    }

}