package com.god.eye;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * InjectResultAdapter
 *
 * 行为：
 * - 长按 Caller：复制 Caller 类名
 * - 长按 Target：复制 Target 类名
 * - 长按 smali：复制调用指令
 */
public class InjectResultAdapter
        extends RecyclerView.Adapter<InjectResultAdapter.VH> {

    private final Context context;
    private final List<InjectResult> data;

    public InjectResultAdapter(Context context, List<InjectResult> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inject_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        InjectResult r = data.get(position);

        // 格式化类名，去掉开头的 L 和结尾的 ;，将 / 替换为 .
        String formattedCallerClass = r.callerClass.replace("L", "").replace(";", "").replace('/', '.');
        String formattedTargetClass = r.targetClass.replace("L", "").replace(";", "").replace('/', '.');

        h.tvCaller.setText("Caller: " + formattedCallerClass);
        h.tvTarget.setText("Target: " + formattedTargetClass);
        h.tvSmali.setText(r.smali);

        // ===== 长按 Caller：复制 Caller 类名 =====
        h.tvCaller.setOnLongClickListener(v -> {
            copyText(formattedCallerClass);  // 复制格式化后的 Caller 类名
            Toast.makeText(context, "已复制 Caller 类名", Toast.LENGTH_SHORT).show();
            return true;
        });

        // ===== 长按 Target：复制 Target 类名 =====
        h.tvTarget.setOnLongClickListener(v -> {
            copyText(formattedTargetClass);  // 复制格式化后的 Target 类名
            Toast.makeText(context, "已复制 Target 类名", Toast.LENGTH_SHORT).show();
            return true;
        });

        // ===== 长按 smali：复制调用指令 =====
        h.tvSmali.setOnLongClickListener(v -> {
            copyText(r.smali);
            Toast.makeText(context, "已复制调用指令", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * 通用复制方法
     */
    private void copyText(String text) {
        ClipboardManager cm =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("copy", text));
        }
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvCaller;
        TextView tvTarget;
        TextView tvSmali;

        VH(View v) {
            super(v);
            tvCaller = v.findViewById(R.id.tvCaller);
            tvTarget = v.findViewById(R.id.tvTarget);
            tvSmali  = v.findViewById(R.id.tvSmali);
        }
    }
}
