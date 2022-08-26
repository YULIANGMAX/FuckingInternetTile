package fucking.internet.tile;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {

    private static final String[] SCOPE = {
            "com.android.systemui"
    };

    private static final String INTERNET = "internet";
    private static final String CELL = "cell";
    private static final String WIFI = "wifi";

    private static final String STUB = "_stub";
    private static final String INTERNET_STUB = INTERNET + STUB;
    private static final String CELL_STUB = CELL + STUB;
    private static final String WIFI_STUB = WIFI + STUB;

    @Override
    public void handleLoadPackage(final @NonNull LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals(SCOPE[0])) {

            Class<?> class_QSTileHost = XposedHelpers.findClass("com.android.systemui.qs.QSTileHost", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(
                    class_QSTileHost,
                    "loadTileSpecs",
                    Context.class, String.class,
                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[1] = ((String) param.args[1])
                                    .replace(CELL, CELL_STUB)
                                    .replace(WIFI, WIFI_STUB).replace(INTERNET, INTERNET_STUB);
                            super.beforeHookedMethod(param);
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            ArrayList<String> tiles = (ArrayList<String>) param.getResult();
                            if (tiles.contains(CELL_STUB)) {
                                tiles.set(tiles.indexOf(CELL_STUB), CELL);
                            }
                            if (tiles.contains(WIFI_STUB)) {
                                tiles.set(tiles.indexOf(WIFI_STUB), WIFI);
                            }
                            if (tiles.contains(INTERNET_STUB)) {
                                tiles.set(tiles.indexOf(INTERNET_STUB), INTERNET);
                            }
                            param.setResult(tiles);
                        }

                    }
            );

            Class<?> class_TileQueryHelper = XposedHelpers.findClass("com.android.systemui.qs.customize.TileQueryHelper", loadPackageParam.classLoader);
            Class<?> class_TileQueryHelper_TileCollector = XposedHelpers.findClass("com.android.systemui.qs.customize.TileQueryHelper$TileCollector", loadPackageParam.classLoader);
            XposedHelpers.findAndHookConstructor(
                    class_TileQueryHelper_TileCollector,
                    class_TileQueryHelper, ArrayList.class, class_QSTileHost,
                    new XC_MethodHook() {

                        @Override
                        @SuppressWarnings("unchecked")
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                            int internet_index = -1;
                            int cell_index = -1;
                            int wifi_index = -1;

                            ArrayList<Object> tilesToAdd = (ArrayList<Object>) param.args[1];
                            for (int i = tilesToAdd.size() - 1; i >= 0; i--) {
                                String tileSpec = (String) XposedHelpers.callMethod(tilesToAdd.get(i), "getTileSpec");
                                switch (tileSpec) {
                                    case INTERNET:
                                        internet_index = i;
                                        break;
                                    case CELL:
                                        cell_index = i;
                                        break;
                                    case WIFI:
                                        wifi_index = i;
                                        break;
                                    default:
                                        break;
                                }
                            }

                            Object qSTileHost = param.args[2];

                            Object qSTile_WIFI = XposedHelpers.callMethod(qSTileHost, "createTile", WIFI);
                            XposedHelpers.callMethod(qSTile_WIFI, "setTileSpec", WIFI);

                            Object qSTile_CELL = XposedHelpers.callMethod(qSTileHost, "createTile", CELL);
                            XposedHelpers.callMethod(qSTile_CELL, "setTileSpec", CELL);

                            if (internet_index >= 0) {
                                if (cell_index < 0) {
                                    tilesToAdd.add(internet_index, qSTile_CELL);
                                }
                                if (wifi_index < 0) {
                                    tilesToAdd.add(internet_index, qSTile_WIFI);
                                }
                            } else {
                                if (cell_index < 0) {
                                    tilesToAdd.add(qSTile_CELL);
                                }
                                if (wifi_index < 0) {
                                    tilesToAdd.add(qSTile_WIFI);
                                }
                            }
                            super.beforeHookedMethod(param);
                        }
                    }
            );
        }


    }


}
