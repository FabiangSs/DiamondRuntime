package dev.lcehub.emerald;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import dev.lcehub.emerald.container.Container;
import dev.lcehub.emerald.container.ContainerManager;
import dev.lcehub.emerald.contents.ContentsManager;
import dev.lcehub.emerald.core.AppUtils;
import dev.lcehub.emerald.core.DefaultVersion;
import dev.lcehub.emerald.core.WineInfo;
import dev.lcehub.emerald.xenvironment.ImageFsInstaller;

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
        if (!ImageFsInstaller.installIfNeeded(this, this::ensureContainer)) {
            ensureContainer();
        }
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
            if (container.getAudioDriver().equals("alsa")) {
                container.setAudioDriver(Container.DEFAULT_AUDIO_DRIVER);
                container.saveData();
            }
            onContainerReady(container);
            return;
        }

        try {
            JSONObject data = new JSONObject();
            data.put("name", CONTAINER_NAME);
            data.put("wineVersion", WineInfo.MAIN_WINE_VERSION.identifier());
            data.put("box64Version", DefaultVersion.WOWBOX64);
            data.put("fexcoreVersion", DefaultVersion.FEXCORE);
            data.put("drives", getDrivesString());
            ContentsManager contentsManager = new ContentsManager(this);
            manager.createContainerAsync(data, contentsManager, this::onContainerCreated);
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
        if (ACTION_PLAY.equals(action)) {
            Intent intent = new Intent(this, XServerDisplayActivity.class);
            intent.putExtra("container_id", container.id);
            intent.putExtra("exec_path", instancePath + "/" + GAME_EXECUTABLE);
            startActivity(intent);
        }
        else if (ACTION_OPEN.equals(action)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("container_id", container.id);
            intent.putExtra("start_path", instancePath);
            startActivity(intent);
        }
        else if (ACTION_SETTINGS.equals(action)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("container_settings_id", container.id);
            startActivity(intent);
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
        return "D:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "E:" + instancePath;
    }
}
