package org.astral.findmaimaiultra.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.adapter.PhotoAdapter;
import org.astral.findmaimaiultra.been.pixiv.jm.Album;


public class JMActivity extends AppCompatActivity {
    private FrameLayout overlay;
    private boolean isOverlayVisible = false;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private PhotoAdapter photoAdapter;
    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jm_dialog);

        initRecyclerView();
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n", "ResourceType"})
    private void initRecyclerView() {
        Intent intent = getIntent();
        String res = intent.getStringExtra("album");
        Album a = new Gson().fromJson(res, Album.class);
        Toast.makeText(this,"加载中", Toast.LENGTH_SHORT).show();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoAdapter = new PhotoAdapter(this, a.getImage_urls(), a.getNums(), a);
        photoAdapter.clearLoad();

        recyclerView.setAdapter(photoAdapter);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setPeekHeight(dpToPx(80));
        TextView menu = findViewById(R.id.menu);
        menu.setText(a.getName());
        TextView dec = findViewById(R.id.dec);
        dec.setText(a.getAuthors().toString().replaceAll( "\\[","").replaceAll( "]","")
                + " / " + a.getActors().toString().replaceAll( "\"","") .replaceAll( "\\[","").replaceAll( "]","")
                + " \n " + a.getTags().toString().replaceAll( "\"","") .replaceAll( "\\[","").replaceAll( "]","")
                + " \n " + a.getAlbum_id().replaceAll( "\"","").replaceAll( "\\[","").replaceAll( "]",""));
    }
    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photoAdapter != null) {
            photoAdapter.clearCache();
        }
    }
}
