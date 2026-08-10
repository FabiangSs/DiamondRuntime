package dev.lcehub.emerald.fexcore;

import dev.lcehub.emerald.R;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import dev.lcehub.emerald.contents.ContentProfile;
import dev.lcehub.emerald.contents.ContentsManager;
import dev.lcehub.emerald.core.AppUtils;
import dev.lcehub.emerald.core.EnvVars;
import dev.lcehub.emerald.core.KeyValueSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FEXCoreManager {
    public static void loadFEXCoreVersion(Context context, ContentsManager contentsManager, Spinner spinner, String fexcoreVersion) {
        String[] originalItems = context.getResources().getStringArray(R.array.fexcore_version_entries);
        List<String> itemList = new ArrayList<>(Arrays.asList(originalItems));
        for (ContentProfile profile : contentsManager.getInstalledProfiles(ContentProfile.ContentType.CONTENT_TYPE_FEXCORE)) {
            String entryName = ContentsManager.getEntryName(profile);
            int firstDashIndex = entryName.indexOf('-');
            itemList.add(entryName.substring(firstDashIndex + 1));
        }
        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, itemList));
        AppUtils.setSpinnerSelectionFromValue(spinner, fexcoreVersion);
    }
}
