package com.wherami

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

import android.util.Log
import android.widget.Toast

import java.io.StreamCorruptedException
import java.net.URISyntaxException
import java.util.HashMap

import wherami.lbs.sdk.Client

class WheramiModule(val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

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
  fun initialize(promise: Promise) {
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
    promise.resolve(isInitialized)
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
}
