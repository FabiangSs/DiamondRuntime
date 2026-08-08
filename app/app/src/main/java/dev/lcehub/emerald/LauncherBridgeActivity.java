package dev.lcehub.emerald;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.lcehub.emerald.container.Container;
import dev.lcehub.emerald.container.ContainerManager;
import dev.lcehub.emerald.core.AppUtils;
import dev.lcehub.emerald.xenvironment.RootFSInstaller;

import org.json.JSONException;
import org.json.JSONObject;

public class LauncherBridgeActivity extends AppCompatActivity {
    public static final String EXTRA_ACTION = "launcher_action";
    public static final String EXTRA_INSTANCE_PATH = "instance_path";

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_SETTINGS = "settings";

    public static final String CONTAINER_NAME = "DiamondEmerald";
    public static final String GAME_EXECUTABLE = "Minecraft.Client.exe";

    private String action;
    private String instancePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        action = getIntent().getStringExtra(EXTRA_ACTION);
        instancePath = getIntent().getStringExtra(EXTRA_INSTANCE_PATH);
        if (action == null || instancePath == null) {
            finish();
            return;
        }

        AppUtils.hideSystemUI(this);
        RootFSInstaller.installIfNeeded(this, this::ensureContainer);
    }

    private void ensureContainer() {
        ContainerManager manager = new ContainerManager(this);
        Container container = null;
        for (Container existing : manager.getContainers()) {
            if (CONTAINER_NAME.equals(existing.getName())) {
                container = existing;
                break;
            }
        }

        if (container != null) {
            updateDrives(container);
            onContainerReady(container);
            return;
        }

        try {
            JSONObject data = new JSONObject();
            data.put("name", CONTAINER_NAME);
            data.put("drives", getDrivesString());
            manager.createContainerAsync(data, this::onContainerCreated);
        }
        catch (JSONException e) {
            AppUtils.showToast(this, "Failed to create container");
            finish();
        }
    }

    private void onContainerCreated(Container container) {
        if (container == null) {
            AppUtils.showToast(this, "Failed to create container");
            finish();
            return;
        }
        onContainerReady(container);
    }

    private void onContainerReady(Container container) {
        switch (action) {
            case ACTION_PLAY: {
                Intent intent = new Intent(this, XServerDisplayActivity.class);
                intent.putExtra("container_id", container.id);
                intent.putExtra("exec_path", instancePath+"/"+GAME_EXECUTABLE);
                startActivity(intent);
                break;
            }
            case ACTION_OPEN: {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("container_id", container.id);
                intent.putExtra("start_path", instancePath);
                startActivity(intent);
                break;
            }
            case ACTION_SETTINGS: {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("container_settings_id", container.id);
                startActivity(intent);
                break;
            }
        }
        finish();
    }

    private void updateDrives(Container container) {
        String drives = getDrivesString();
        if (!drives.equals(container.getDrives())) {
            container.setDrives(drives);
            container.saveData();
        }
    }

    private String getDrivesString() {
        return "D:"+AppUtils.DIRECTORY_DOWNLOADS+"E:"+instancePath;
    }
}
