package dev.lcehub.emerald.xenvironment;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import dev.lcehub.emerald.R;
import dev.lcehub.emerald.SettingsFragment;
import dev.lcehub.emerald.container.Container;
import dev.lcehub.emerald.container.ContainerManager;
import dev.lcehub.emerald.contents.AdrenotoolsManager;
import dev.lcehub.emerald.core.AppUtils;
import dev.lcehub.emerald.core.DownloadProgressDialog;
import dev.lcehub.emerald.core.FileUtils;
import dev.lcehub.emerald.core.PreloaderDialog;
import dev.lcehub.emerald.core.TarCompressorUtils;
import dev.lcehub.emerald.core.WineInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ImageFsInstaller {
    public static final byte LATEST_VERSION = 22;

    private static void resetContainerImgVersions(Context context) {
        ContainerManager manager = new ContainerManager(context);
        for (Container container : manager.getContainers()) {
            container.putExtra("imgVersion", null);
            container.saveData();
        }
    }

    public static boolean installWineArchive(final Context context, String version, File archiveFile) {
        File rootDir = ImageFs.find(context).getRootDir();
        File outFile = new File(rootDir, "opt/" + version);
        FileUtils.delete(outFile);
        outFile.mkdirs();
        boolean success = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, archiveFile, outFile);
        if (!success) success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, archiveFile, outFile);
        if (!success) FileUtils.delete(outFile);
        return success;
    }

    public static void installWineFromAssets(final DownloadProgressDialog dialog, final AppCompatActivity activity) {
        String[] versions = activity.getResources().getStringArray(R.array.wine_entries);
        File rootDir = ImageFs.find(activity).getRootDir();
        final byte compressionRatio = 22;

        activity.runOnUiThread(() -> dialog.setMessage(R.string.installing_wine_files));

        for (String version : versions) {
            File outFile = new File(rootDir, "opt/" + version);
            outFile.mkdirs();
            final long contentLength = (long)(FileUtils.getSize(activity, version + ".tar.zst") * (100.0f / compressionRatio));
            AtomicLong totalSizeRef = new AtomicLong();

            TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, activity, version + ".tar.zst", outFile, (file, size) -> {
                if (size > 0) {
                    long totalSize = totalSizeRef.addAndGet(size);
                    final int progress = (int)(((float)totalSize / contentLength) * 100);
                    activity.runOnUiThread(() -> dialog.setProgress(progress));
                }
                return file;
            });
         }
    }

    public static void installDriversFromAssets(final DownloadProgressDialog dialog, final AppCompatActivity activity) {
        
        activity.runOnUiThread(() -> dialog.setMessage(R.string.installing_wine_files));
        AdrenotoolsManager adrenotoolsManager = new AdrenotoolsManager(activity);
        String[] adrenotoolsAssetDrivers = activity.getResources().getStringArray(R.array.wrapper_graphics_driver_version_entries);

        for (String driver : adrenotoolsAssetDrivers) {
            final byte compressionRatio = 22;
            final long contentLength = (long)(FileUtils.getSize(activity, adrenotoolsManager.getAssetPath(driver)) * (100.0f / compressionRatio));
            AtomicLong totalSizeRef = new AtomicLong();
            adrenotoolsManager.extractDriverFromResources(driver, (file, size) -> {
                if (size > 0) {
                    long totalSize = totalSizeRef.addAndGet(size);
                    final int progress = (int)(((float)totalSize / contentLength) * 100);
                    activity.runOnUiThread(() -> dialog.setProgress(progress));
                }
                return file;
            });
         }   
    }

    public static void installFromAssets(final AppCompatActivity activity, Runnable callback) {
        AppUtils.keepScreenOn(activity);
        ImageFs imageFs = ImageFs.find(activity);
        File rootDir = imageFs.getRootDir();

        SettingsFragment.resetEmulatorsVersion(activity);

        final DownloadProgressDialog dialog = new DownloadProgressDialog(activity);
        dialog.show(R.string.installing_system_files);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            clearRootDir(rootDir);
            final byte compressionRatio = 22;
            final long contentLength = (long)(FileUtils.getSize(activity, "imagefs.tar.zst") * (100.0f / compressionRatio));
            AtomicLong totalSizeRef = new AtomicLong();

            boolean success = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, activity, "imagefs.tar.zst", rootDir, (file, size) -> {
                if (size > 0) {
                    long totalSize = totalSizeRef.addAndGet(size);
                    final int progress = (int)(((float)totalSize / contentLength) * 100);
                    activity.runOnUiThread(() -> dialog.setProgress(progress));
                }
                return file;
            });

            if (success) {
                installWineFromAssets(dialog, activity);
                installDriversFromAssets(dialog, activity);
                imageFs.createImgVersionFile(LATEST_VERSION);
                FileUtils.symlink("libSDL2-2.0.so", new File(imageFs.getLibDir(), "libSDL2-2.0.so.0").getAbsolutePath());
                resetContainerImgVersions(activity);
            }
            else AppUtils.showToast(activity, R.string.unable_to_install_system_files);
            
            dialog.closeOnUiThread();
            activity.runOnUiThread(() -> {if (callback != null) callback.run();});
        });
    }

    public static boolean installIfNeeded(final AppCompatActivity activity, Runnable callback) {
        ImageFs imageFs = ImageFs.find(activity);
        
        if (!imageFs.isValid() || imageFs.getVersion() < LATEST_VERSION) {
            installFromAssets(activity, callback);
            return true;
        }    
        
        return false;
    }

    private static void clearOptDir(File optDir) {
        File[] files = optDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("installed-wine")) continue;
                FileUtils.delete(file);
            }
        }
    }

    private static void clearRootDir(File rootDir) {
        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String name = file.getName();
                        if (name.equals("home")) {
                            continue;
                        }
                    }
                    FileUtils.delete(file);
                }
            }
        }
        else rootDir.mkdirs();
    }
}