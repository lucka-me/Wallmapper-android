package labs.lucka.wallmapper

import android.Manifest
import android.app.Service
import android.app.WallpaperColors
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import org.jetbrains.anko.defaultSharedPreferences

class WallmapperLiveService : WallpaperService() {

    private inner class WallmapperLiveEngine : Engine() {

        private lateinit var locationManager: LocationManager
        private val cameraBuilder: CameraPosition.Builder = CameraPosition.Builder()
        private lateinit var styleIndex: MapStyleIndex
        private lateinit var snapshotKit: SnapshotKit
        private val lastLatLng: LatLng = LatLng()
        private var lastImage: Bitmap? = null

        val onSnapshotReady: (Bitmap) -> Unit = { image ->
            lastImage = image
            redraw(image)
        }
        val onSnapshotError: (String?) -> Unit = { error ->
            Log.i("WMTEST", "ERROR: $error")
            Log.i("WMTEST", "onSnapshotError going to refresh")
            refresh()
        }

        val locationListener: LocationListener = object : LocationListener {

            override fun onLocationChanged(location: Location?) {
                if (location == null) return
                Log.i("WMTEST", "Location Changed")
                lastLatLng.latitude = location.latitude
                lastLatLng.longitude = location.longitude
                Log.i("WMTEST", "onLocationChanged going to refresh")
                refresh()
            }

            override fun onProviderDisabled(provider: String?) { }
            override fun onProviderEnabled(provider: String?) { }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)

            Mapbox.getInstance(this@WallmapperLiveService, getString(R.string.mapbox_default_access_token))
            Mapbox.setAccessToken(MapKit.getToken(this@WallmapperLiveService))

            snapshotKit = SnapshotKit(this@WallmapperLiveService)

            lastLatLng.latitude = defaultSharedPreferences
                .getFloat(getString(R.string.pref_map_last_position_latitude), 22.308F).toDouble()
            lastLatLng.longitude = defaultSharedPreferences
                .getFloat(getString(R.string.pref_map_last_position_longitude), 114.174F).toDouble()
            cameraBuilder.target(lastLatLng)

            resetFromPreferences()

            locationManager = getSystemService(Service.LOCATION_SERVICE) as LocationManager
            if (
                ContextCompat.checkSelfPermission(
                    this@WallmapperLiveService, Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                val lastLocation = locationManager.getLastKnownLocation(getProvider())
                if (lastLocation != null) {
                    lastLatLng.latitude = lastLocation.latitude
                    lastLatLng.longitude = lastLocation.longitude
                    Log.i("WMTEST", "Last location: " + lastLocation.latitude + ", " + lastLocation.longitude)
                } else {
                    Log.i("WMTEST", "Last location: UNKNOWN")
                }
            }
            requestLocationUpdates()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                Log.i("WMTEST", "VISIBLE")
                resetFromPreferences()
                Log.i("WMTEST", "onVisibilityChanged going to refresh")
                refresh()
                requestLocationUpdates()
            } else {
                Log.i("WMTEST", "INVISIBLE")
                locationManager.removeUpdates(locationListener)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.i("WMTEST", "onSurfaceChanged going to refresh")
            refresh()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            snapshotKit.onPause()
            locationManager.removeUpdates(locationListener)
        }

        override fun onComputeColors(): WallpaperColors? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val image = lastImage
                if (image != null) {
                    WallpaperColors.fromBitmap(image)
                } else {
                    null
                }
            } else {
                super.onComputeColors()
            }
        }

        private fun requestLocationUpdates() {
            if (
                ContextCompat.checkSelfPermission(
                    this@WallmapperLiveService, Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                var radius = defaultSharedPreferences
                    .getString(getString(R.string.pref_live_wallpaper_radius), "1")?.toFloatOrNull()
                if (radius == null) radius = 1F
                locationManager.requestLocationUpdates(
                    getProvider(), 0L,
                    radius * 1000,
                    locationListener
                )
            }
        }

        private fun refresh() {
            when (styleIndex.type) {

                MapStyleIndex.StyleType.LOCAL, MapStyleIndex.StyleType.CUSTOMIZED -> {
                    snapshotKit.takeSnapshotJson(
                        desiredMinimumWidth, desiredMinimumHeight,
                        SnapshotKit.handleLabels(
                            this@WallmapperLiveService,
                            DataKit.loadStyleJson(this@WallmapperLiveService, styleIndex.path)
                        ),
                        cameraBuilder.build(), onSnapshotReady, onSnapshotError)
                }

                else -> {
                    snapshotKit.takeSnapshotUrl(
                        desiredMinimumWidth, desiredMinimumHeight,
                        styleIndex.path,
                        cameraBuilder.build(), onSnapshotReady, onSnapshotError)
                }

            }
        }

        private fun redraw(image: Bitmap) {
            val canvas = surfaceHolder.lockCanvas()
            canvas.drawBitmap(image, 0.0F, 0.0F, null)
            surfaceHolder.unlockCanvasAndPost(canvas)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                notifyColorsChanged()
            }
        }

        private fun resetFromPreferences() {

            val isCameraPositionDesignated = defaultSharedPreferences
                .getBoolean(getString(R.string.pref_live_wallpaper_designate_camera), true)
            cameraBuilder.zoom(
                if (isCameraPositionDesignated) {
                    defaultSharedPreferences
                        .getInt(getString(R.string.pref_live_wallpaper_zoom), 15).toDouble()
                } else {
                    defaultSharedPreferences
                        .getFloat(getString(R.string.pref_map_last_position_zoom), 15F).toDouble()
                }
            )
            cameraBuilder.bearing(
                if (isCameraPositionDesignated) {
                    defaultSharedPreferences
                        .getInt(getString(R.string.pref_live_wallpaper_bearing), 0).toDouble()
                } else {
                    defaultSharedPreferences
                        .getFloat(getString(R.string.pref_map_last_position_bearing), 0F).toDouble()
                }
            )
            cameraBuilder.tilt(
                if (isCameraPositionDesignated) {
                    defaultSharedPreferences
                        .getInt(getString(R.string.pref_live_wallpaper_tilt), 0).toDouble()
                } else {
                    defaultSharedPreferences
                        .getFloat(getString(R.string.pref_map_last_position_tilt), 0F).toDouble()
                }
            )

            styleIndex = MapKit.getSelectedStyleIndex(this@WallmapperLiveService)
        }

        private fun getProvider(): String {
            val providerValue = defaultSharedPreferences
                .getString(getString(R.string.pref_live_wallpaper_provider), "1")?.toIntOrNull()
            return when(providerValue) {
                0 -> LocationManager.GPS_PROVIDER
                else -> LocationManager.NETWORK_PROVIDER
            }
        }
    }

    override fun onCreateEngine(): Engine { return WallmapperLiveEngine() }
}