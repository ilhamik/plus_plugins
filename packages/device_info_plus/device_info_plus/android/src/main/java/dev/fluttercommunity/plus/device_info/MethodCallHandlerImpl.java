// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package dev.fluttercommunity.plus.device_info;

import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of {@link MethodChannel.MethodCallHandler} for the plugin. Responsible for
 * receiving method calls from method channel.
 */
class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {

  private final PackageManager packageManager;
  private final WindowManager windowManager;

  /** Substitute for missing values. */
  private static final String[] EMPTY_STRING_LIST = new String[] {};

  /**
   * Constructs DeviceInfo. {@code contentResolver}, {@code packageManager} and {@code getActivity}
   * must not be null.
   */
  MethodCallHandlerImpl(PackageManager packageManager, WindowManager windowManager) {
    this.packageManager = packageManager;
    this.windowManager = windowManager;
  }

  @Override
  public void onMethodCall(MethodCall call, @NonNull MethodChannel.Result result) {
    if (call.method.equals("getAndroidDeviceInfo")) {
      Map<String, Object> build = new HashMap<>();
      build.put("board", Build.BOARD);
      build.put("bootloader", Build.BOOTLOADER);
      build.put("brand", Build.BRAND);
      build.put("device", Build.DEVICE);
      build.put("display", Build.DISPLAY);
      build.put("fingerprint", Build.FINGERPRINT);
      build.put("hardware", Build.HARDWARE);
      build.put("host", Build.HOST);
      build.put("id", Build.ID);
      build.put("manufacturer", Build.MANUFACTURER);
      build.put("model", Build.MODEL);
      build.put("product", Build.PRODUCT);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        build.put("supported32BitAbis", Arrays.asList(Build.SUPPORTED_32_BIT_ABIS));
        build.put("supported64BitAbis", Arrays.asList(Build.SUPPORTED_64_BIT_ABIS));
        build.put("supportedAbis", Arrays.asList(Build.SUPPORTED_ABIS));
      } else {
        build.put("supported32BitAbis", Arrays.asList(EMPTY_STRING_LIST));
        build.put("supported64BitAbis", Arrays.asList(EMPTY_STRING_LIST));
        build.put("supportedAbis", Arrays.asList(EMPTY_STRING_LIST));
      }
      build.put("tags", Build.TAGS);
      build.put("type", Build.TYPE);
      build.put("isPhysicalDevice", !isEmulator());

      build.put("systemFeatures", Arrays.asList(getSystemFeatures()));

      Map<String, Object> version = new HashMap<>();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        version.put("baseOS", Build.VERSION.BASE_OS);
        version.put("previewSdkInt", Build.VERSION.PREVIEW_SDK_INT);
        version.put("securityPatch", Build.VERSION.SECURITY_PATCH);
      }
      version.put("codename", Build.VERSION.CODENAME);
      version.put("incremental", Build.VERSION.INCREMENTAL);
      version.put("release", Build.VERSION.RELEASE);
      version.put("sdkInt", Build.VERSION.SDK_INT);
      build.put("version", version);

      final Display display = windowManager.getDefaultDisplay();
      final DisplayMetrics metrics = new DisplayMetrics();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        display.getRealMetrics(metrics);
      } else {
        display.getMetrics(metrics);
      }
      Map<String, Object> displayResult = new HashMap<>();
      displayResult.put("widthPx", (double) metrics.widthPixels);
      displayResult.put("heightPx", (double) metrics.heightPixels);
      displayResult.put("xDpi", metrics.xdpi);
      displayResult.put("yDpi", metrics.ydpi);
      build.put("displayMetrics", displayResult);

      result.success(build);
    } else {
      result.notImplemented();
    }
  }

  private String[] getSystemFeatures() {
    FeatureInfo[] featureInfos = packageManager.getSystemAvailableFeatures();
    if (featureInfos == null) {
      return EMPTY_STRING_LIST;
    }
    String[] features = new String[featureInfos.length];
    for (int i = 0; i < featureInfos.length; i++) {
      features[i] = featureInfos[i].name;
    }
    return features;
  }

  /**
   * A simple emulator-detection based on the flutter tools detection logic and a couple of legacy
   * detection systems
   */
  private boolean isEmulator() {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.HARDWARE.contains("goldfish")
        || Build.HARDWARE.contains("ranchu")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || Build.PRODUCT.contains("sdk_google")
        || Build.PRODUCT.contains("google_sdk")
        || Build.PRODUCT.contains("sdk")
        || Build.PRODUCT.contains("sdk_x86")
        || Build.PRODUCT.contains("vbox86p")
        || Build.PRODUCT.contains("emulator")
        || Build.PRODUCT.contains("simulator");
  }
}
