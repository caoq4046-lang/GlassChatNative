package top.glasschat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public final class MainActivity extends AppCompatActivity {
    private static final int BG = Color.rgb(12, 16, 22);
    private static final int PANEL = Color.rgb(24, 30, 39);
    private static final int TEXT = Color.rgb(241, 245, 249);
    private static final int MUTED = Color.rgb(148, 163, 184);
    private ScrollView messageScroll;
    private LinearLayout messages;
    private EditText composer;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        setContentView(buildRoot(this));
    }

    private View buildRoot(Context context) {
        LinearLayout root = column(context, BG);
        LinearLayout top = row(context, PANEL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(18), dp(8), dp(12), dp(8));
        top.addView(label(context, "GlassChat", 21, TEXT), new LinearLayout.LayoutParams(0, -1, 1));
        top.addView(textButton(context, "构建", v -> chooseProjectDirectory()), size(76, 52));
        top.addView(textButton(context, "⋯", v -> toast("更多功能")), size(52, 52));
        root.addView(top, size(-1, 64));

        TextView section = label(context, "当前会话", 13, MUTED);
        section.setPadding(dp(18), dp(14), dp(18), dp(4));
        root.addView(section, size(-1, 38));
        TextView contact = label(context, "本地联系人\n在线聊天", 16, TEXT);
        contact.setPadding(dp(18), 0, dp(18), 0);
        root.addView(contact, size(-1, 58));

        messageScroll = new ScrollView(context);
        messages = column(context, Color.TRANSPARENT);
        messages.setPadding(dp(16), dp(10), dp(16), dp(10));
        messageScroll.addView(messages, new ViewGroup.LayoutParams(-1, -2));
        root.addView(messageScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composerBar = row(context, PANEL);
        composerBar.setGravity(Gravity.CENTER_VERTICAL);
        composerBar.setPadding(dp(10), dp(8), dp(10), dp(8));
        composer = new EditText(context);
        composer.setSingleLine(false);
        composer.setHint("输入消息");
        composer.setHintTextColor(MUTED);
        composer.setTextColor(TEXT);
        composer.setTextSize(16);
        composer.setPadding(dp(14), dp(8), dp(14), dp(8));
        composer.setBackground(round(Color.rgb(35, 43, 54), 18));
        composerBar.addView(composer, new LinearLayout.LayoutParams(0, dp(52), 1));
        composerBar.addView(textButton(context, "➤", v -> sendMessage()), size(52, 52));
        root.addView(composerBar, size(-1, 70));

        addMessage("欢迎使用 GlassChat", false);
        addMessage("这是原生 Android 界面，不依赖网页或 WebView。", false);
        return root;
    }

    private void chooseProjectDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, 4101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 4101 || resultCode != RESULT_OK || data == null) return;
        Uri tree = data.getData();
        if (tree == null) return;
        try {
            getContentResolver().takePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some providers do not support persisted permissions.
        }
        toast("已选择工程目录。原生上传模块正在接入：" + tree.getLastPathSegment());
    }

    private void sendMessage() {
        String text = composer.getText().toString().trim();
        if (text.isEmpty()) return;
        addMessage(text, true);
        composer.setText("");
        messageScroll.post(() -> messageScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void addMessage(String text, boolean mine) {
        TextView bubble = label(this, text, 16, TEXT);
        bubble.setPadding(dp(16), dp(10), dp(16), dp(10));
        bubble.setBackground(round(mine ? Color.rgb(33, 105, 170) : PANEL, 18));
        LinearLayout line = row(this, Color.TRANSPARENT);
        line.setGravity(mine ? Gravity.END : Gravity.START);
        line.setPadding(0, dp(5), 0, dp(5));
        line.addView(bubble, new LinearLayout.LayoutParams(-2, -2));
        messages.addView(line, size(-1, -2));
    }

    private TextView textButton(Context context, String text, View.OnClickListener listener) {
        TextView button = label(context, text, 24, TEXT);
        button.setGravity(Gravity.CENTER);
        button.setBackground(round(Color.TRANSPARENT, 16));
        button.setOnClickListener(listener);
        return button;
    }

    private TextView label(Context context, String text, float textSize, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(textSize);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private LinearLayout column(Context context, int color) {
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setBackgroundColor(color);
        return view;
    }

    private LinearLayout row(Context context, int color) {
        LinearLayout view = column(context, color);
        view.setOrientation(LinearLayout.HORIZONTAL);
        return view;
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width < 0 ? width : dp(width), height < 0 ? height : dp(height));
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}