/**
 * package org.ast.findmaimaidx.ui;
 *
 * import android.os.Bundle;
 * import androidx.appcompat.app.AppCompatActivity;
 * import com.live2d.cubism.widget.Live2DView;
 *
 * import org.ast.findmaimaidx.R;
 *
 * public class Model extends AppCompatActivity {
 *     private Live2DView live2DView;
 *
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.model);
 *
 *         live2DView = findViewById(R.id.live2d_view);
 *
 *         // 加载模型
 *         live2DView.setLive2DModelFromAssets("your_model_folder/model.moc3");
 *         live2DView.setJsonFromAssets("your_model_folder/model.json");
 *         live2DView.setMotionFromAssets("your_model_folder/motion.json");
 *         live2DView.start();
 *     }
 * }
 */