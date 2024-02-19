package com.wherami

import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import wherami.lbs.sdk.Client
import wherami.lbs.sdk.core.MapEngine
import wherami.lbs.sdk.core.MapEngineFactory
import wherami.lbs.sdk.data.Location
import java.io.StreamCorruptedException
import java.net.URISyntaxException


class WheramiModule(val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext),MapEngine.LocationUpdateCallback {

  private var engine: MapEngine? = null
  private var mLocation: Location? = null
  private val fragment:Fragment = Fragment()
  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  companion object {
    const val NAME = "Wherami"
  }
  @ReactMethod
  fun checkPermission(promise: Promise){
    var allPermissions: Array<String>? = null
    try {
      allPermissions = reactContext.packageManager
        .getPackageInfo(reactContext.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
    } catch (e: NameNotFoundException) {
      e.printStackTrace()
    }

    if (allPermissions != null) {
      var allPermissionsAlreadyGranted = true
      if (Build.VERSION.SDK_INT >= 23) {
        val permissions2request: MutableList<String> = ArrayList()
        for (permission in allPermissions) {
          Log.d("Permission",permission)
          Log.d("Permission",checkSelfPermission(reactContext,permission).toString())

          permissions2request.add(permission)
          if (permission == "android.permission.SYSTEM_ALERT_WINDOW") continue
          if (checkSelfPermission(reactContext,permission) != PackageManager.PERMISSION_GRANTED) {
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
      //TODO: fix merge permission issue
      if (true||allPermissionsAlreadyGranted) {
        init()
      }
    }
  }
  @ReactMethod
  fun start(){
    if (engine == null) {
      engine = MapEngineFactory.Create(reactContext)
      Log.d("LocationInit", engine.toString())
      try {
        engine?.initialize()
      } catch (e: StreamCorruptedException) {
        e.printStackTrace()
      }
    }
    engine?.attachLocationUpdateCallback(this)
    engine?.start()
  }

  private fun init() {
    Log.d("LocationInit", "initialize")
    var isInitialized = false
    try {
      // The Dataset is downloaded from a server
      // This is a development server serves as debug purpose only
      // One should setup a http(s) server and host the files under
      // http(s)://<host>/generated_assets/SciencePark-1719W/offline_data/
      Client.Configure("http://43.252.40.60", "HKUST_fusion", reactContext)
      val config: HashMap<String, Any> = HashMap<String,Any>()
      config.put(
        "wherami.lbs.sdk.core.MapEngineFactory:EngineType",
        "wherami.lbs.sdk.core.NativeMapEngine"
      )
      Client.ConfigExtra(config)
      isInitialized = true
    } catch (e: URISyntaxException) {
      e.printStackTrace()
    } catch (e: StreamCorruptedException) {
      e.printStackTrace()
    }
    checkDataUpdate()
  }
  private fun checkDataUpdate() {
    //Start only when the app has the latest data
    Log.d("LocationInit","Checking Update...")
    Log.d("LocationInit", "checkDataUpdate")
    Client.CheckDataUpdate(object : Client.DataUpdateQueryCallback {

      override fun onQueryFailed(e: Exception?) {
        Log.d("LocationInit", "onQueryFailed")
        Toast.makeText(reactContext, "Failed to check data update", Toast.LENGTH_LONG)
        if (Client.GetDataVersion() != null) {
          //It is possible to continue by using old data
          Log.d("LocationInit","START")
        } else {
          //If no data is downloaded previously, it is impossible to continue. Please retry under network environment
        }
      }

      override fun onUpdateAvailable(s: String?) {
        Log.d("LocationInit","Updating data...")
        Log.d("LocationInit", "onUpdateAvailable")
        Client.UpdateData(object : Client.DataUpdateCallback {
          override fun onProgressUpdated(i: Int) {
            Log.d("LocationInit", "onProgressUpdated")
          }

          override fun onCompleted() {
            Log.d("LocationInit", "onCompleted")
            Toast.makeText(reactContext, "Update succeeded", Toast.LENGTH_SHORT)
            Log.d("LocationInit","START")
          }

          override fun onFailed(e: Exception?) {
            Log.d("LocationInit", "onFailed")
            Toast.makeText(reactContext, "Failed to update data", Toast.LENGTH_LONG)
            if (Client.GetDataVersion() != null) {
              //It is possible to continue by using old data
              Log.d("LocationInit","START")
            } else {
              //If no data is downloaded previously, it is impossible to continue. Please retry under network environment
            }
          }
        }, reactContext)
      }

      override fun onLatestVersion() {
        Log.d("LocationInit", "onLatestVersion")
        Toast.makeText(reactContext, "On latest version", Toast.LENGTH_SHORT)
        Log.d("LocationInit","START")
      }
    }, reactContext)
  }
  @ReactMethod
  fun location(promise: Promise){
    promise.resolve("${mLocation?.x},${mLocation?.y},${mLocation?.areaId}")
  }
  override fun onLocationUpdated(location: Location) {
    object : Thread() {
      override fun run() {
        mLocation = location
        Log.d("UpdateLocation","Current Location: (${mLocation?.x},${mLocation?.y},${mLocation?.areaId}")
      }
    }.start()
  }

}
