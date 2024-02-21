package com.wherami

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import wherami.lbs.sdk.Client
import wherami.lbs.sdk.core.MapEngine
import wherami.lbs.sdk.core.MapEngineFactory
import wherami.lbs.sdk.data.Location
import java.io.StreamCorruptedException
import java.net.URISyntaxException


class WheramiModule(val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), MapEngine.LocationUpdateCallback {

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Wherami"
    private var engine: MapEngine? = null
    val REQUIRED_PERMISSION = arrayOf(
      "android.permission.BLUETOOTH_SCAN",
      "android.permission.ACCESS_COARSE_LOCATION",
      "android.permission.ACCESS_FINE_LOCATION",
      "android.permission.ACCESS_BACKGROUND_LOCATION"
    )
  }

  @ReactMethod
  fun checkPermission() {
    var allPermissionsAlreadyGranted = true
    if (Build.VERSION.SDK_INT >= 23) {
      val permissions2request: MutableList<String> = ArrayList()
      for (permission in REQUIRED_PERMISSION) {
        Log.d("Permission", permission)
        Log.d("Permission", checkSelfPermission(reactContext, permission).toString())
        permissions2request.add(permission)
        if (checkSelfPermission(reactContext, permission) != PackageManager.PERMISSION_GRANTED) {
          allPermissionsAlreadyGranted = false
          permissions2request.add(permission)
        }
      }
      if (permissions2request.isNotEmpty()) {
        ActivityCompat.requestPermissions(
          reactContext.currentActivity!!,
          permissions2request.toTypedArray<String>(),
          0xFFF
        )
      }
    }
    Log.d(
      "Permission",
      "allPermissionsAlreadyGranted = $allPermissionsAlreadyGranted"
    )
    if (allPermissionsAlreadyGranted) {
      init()
    }
  }

  private fun sendInitStatus(status: Boolean, msg: String) {
    val event = Arguments.createMap().apply {
      putBoolean("isInitialized", status)
      putString("msg", msg)
    }
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("onInitStatusUpdated", event)
  }

  @ReactMethod
  fun start() {
    if (engine == null) {
      engine = MapEngineFactory.Create(reactContext)
      try {
        engine?.initialize()
      } catch (e: StreamCorruptedException) {
        e.printStackTrace()
      }
    }
    try {
      engine?.attachLocationUpdateCallback(this)
      engine?.start()
    }catch (e:Exception){
      e.printStackTrace()
    }

  }

  private fun init() {
    Log.d("LocationInit", "initialize")
    sendInitStatus(false, "initialize")
    try {
      // The Dataset is downloaded from a server
      // This is a development server serves as debug purpose only
      // One should setup a http(s) server and host the files under
      // http(s)://<host>/generated_assets/SciencePark-1719W/offline_data/
      Client.Configure("http://43.252.40.60", "HKUST_fusion", reactContext)
      val config: HashMap<String, Any> = HashMap()
      config.put(
        "wherami.lbs.sdk.core.MapEngineFactory:EngineType",
        "wherami.lbs.sdk.core.NativeMapEngine"
      )
      Client.ConfigExtra(config)
    } catch (e: URISyntaxException) {
      e.printStackTrace()
    } catch (e: StreamCorruptedException) {
      e.printStackTrace()
    }
    checkDataUpdate()
  }

  private fun checkDataUpdate() {
    //Start only when the app has the latest data
    Log.d("LocationInit", "Checking Update...")
    sendInitStatus(false, "Checking Update...")

    Client.CheckDataUpdate(object : Client.DataUpdateQueryCallback {

      override fun onQueryFailed(e: Exception?) {
        Log.d("LocationInit", "onQueryFailed")
        sendInitStatus(false, "onQueryFailed")
        if (Client.GetDataVersion() != null) {
          //It is possible to continue by using old data
          Log.d("LocationInit", "START")
        } else {
          //If no data is downloaded previously, it is impossible to continue. Please retry under network environment
        }
      }

      override fun onUpdateAvailable(s: String?) {
        Log.d("LocationInit", "Updating data...")
        sendInitStatus(false, "Updating data...")
        Client.UpdateData(object : Client.DataUpdateCallback {
          override fun onProgressUpdated(i: Int) {
            Log.d("LocationInit", "onProgressUpdated")
            sendInitStatus(false, "onProgressUpdated")

          }

          override fun onCompleted() {
            Log.d("LocationInit", "onCompleted")
            Log.d("LocationInit", "START")
            sendInitStatus(true, "onCompleted - START")
          }

          override fun onFailed(e: Exception?) {
            Log.d("LocationInit", "onFailed")
            if (Client.GetDataVersion() != null) {
              //It is possible to continue by using old data
              Log.d("LocationInit", "START")
              sendInitStatus(true, "Failed update and use the old version - START")
            } else {
              //If no data is downloaded previously, it is impossible to continue. Please retry under network environment
            }
          }
        }, reactContext)
      }

      override fun onLatestVersion() {
        Log.d("LocationInit", "onLatestVersion")
        Log.d("LocationInit", "START")
        sendInitStatus(true, "onLatestVersion - START")
      }
    }, reactContext)
  }


  override fun onLocationUpdated(location: Location) {
    object : Thread() {

      override fun run() {

        val event = Arguments.createMap().apply {
          putDouble("x", location.x)
          putDouble("y", location.y)
          putString("areaId", location.areaId)
        }
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("onLocationUpdated", event)
        Log.d(
          "UpdateLocation",
          "Current Location: (${location?.x},${location?.y},${location?.areaId}"
        )
      }
    }.start()
  }

  @ReactMethod
  fun addListener(type: String?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  fun removeListeners(type: Int?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }
}
