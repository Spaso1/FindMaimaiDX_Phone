package org.astral.findmaimaiultra.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import androidx.annotation.NonNull;
import org.astral.findmaimaiultra.R;

import java.util.ArrayList;
import java.util.List;

public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyWidgetFactory(this.getApplicationContext(), intent);
    }
}

class MyWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<String> dataList;

    public MyWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.dataList = intent.getStringArrayListExtra("dataList");
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
    }

    @Override
    public void onCreate() {
        // 初始化数据
    }

    @Override
    public void onDataSetChanged() {
        // 数据集发生变化时调用
    }

    @Override
    public void onDestroy() {
        // 清理资源
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @NonNull
    @Override
    public RemoteViews getViewAt(int position) {
        @SuppressLint("RemoteViewLayout") RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        views.setTextViewText(R.id.widget_item_text, dataList.get(position));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
