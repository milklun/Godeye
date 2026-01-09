package com.god.eye;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * ResultActivity
 *
 * 结果展示页（稳定版）
 * - 不黑屏
 * - 不闪退
 * - 有/无结果都有明确显示
 */
public class ResultActivity extends AppCompatActivity {

    private TextView tvSubTitle;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 当前阶段：先用演示数据，验证 UI 完整性
        List<InjectResult> demoResults = buildDemoData();

        bindData(demoResults);
    }

    /**
     * 绑定数据到 UI
     */
    private void bindData(List<InjectResult> results) {
        if (results == null || results.isEmpty()) {
            // 无结果：显示空状态
            tvSubTitle.setText("未发现可疑注入代码");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // 有结果：显示列表
            tvSubTitle.setText("发现 " + results.size() + " 条可疑调用");
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setAdapter(
                    new InjectResultAdapter(this, results)
            );
        }
    }

    /**
     * 演示数据（仅用于当前阶段 UI 验证）
     * 下一步会被真实分析结果替换
     */
    private List<InjectResult> buildDemoData() {
        List<InjectResult> list = new ArrayList<>();

        list.add(new InjectResult(
                "Lcom/app/MainActivity;",
                "Lcom/ah7x5m9u/n61wd523/CardCheckSimpleNew;",
                "invoke-static {p0, v0, v1}, " +
                        "Lcom/ah7x5m9u/n61wd523/CardCheckSimpleNew;->show(Landroid/app/Activity;Ljava/lang/String;Ljava/lang/String;)V"
        ));

        return list;
    }
}
