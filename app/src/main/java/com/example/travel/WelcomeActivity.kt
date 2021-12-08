package com.example.travel

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.travel.helpers.GlobalHelper
import com.example.travel.helpers.SharedPreference
import com.example.travel.slider.IntroSlide
import com.example.travel.slider.IntroSliderAdapter
import com.google.android.material.button.MaterialButton
import com.mapbox.bindgen.Value
import com.mapbox.common.*
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    val PREF_NAME = "FirstStart";
    private val introSliderAdapter = IntroSliderAdapter(
        listOf(
            IntroSlide(
                R.string.welcome_title,
                R.drawable.welcome
            ),
            IntroSlide(
                R.string.welcome_title_location,
                R.drawable.welcome
            ),
            IntroSlide(
                R.string.welcome_title_download_map,
                R.drawable.welcome
            )
        )
    )

    var checkLocation = false
    var checkDownloadMap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreference = SharedPreference(this)

        if (sharedPreference.getValueBoolien(PREF_NAME, false)) {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        } else {
            setContentView(R.layout.activity_welcome)

            introSliderViewPager.setUserInputEnabled(false);
            introSliderViewPager.adapter = introSliderAdapter;

            setupIndicators()
            setCurrentIndicator(0)
            introSliderViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setCurrentIndicator(position)
                }
            })

            buttonNext.setOnClickListener {
                if (introSliderViewPager.currentItem + 1 < introSliderAdapter.itemCount) {
                    if (introSliderViewPager.currentItem == 0) {
                        buttonNext.text = "Разрешить доступ с текущему месторасположению"
                        introSliderViewPager.currentItem += 1
                    } else if (introSliderViewPager.currentItem == 1) {
                        if (!checkLocation) {
                            GlobalHelper.accessLocation(this)
                            checkLocation = true
                            buttonNext.text = "Далее"
                        } else {
                            introSliderViewPager.currentItem += 1
                            buttonNext.text = "Скачать карты и навигацию"
                        }
                    }

                } else {
                    if (!checkDownloadMap) {
                        val textTitle = introSliderViewPager.findViewById<TextView>(R.id.textTitle)
                        val progressBar =
                            introSliderViewPager.findViewById<ProgressBar>(R.id.progressBar)
                        val titleProgressBar =
                            introSliderViewPager.findViewById<TextView>(R.id.titleProgressBar)
                        textTitle.visibility = View.GONE
                        buttonNext.isEnabled = false
                        progressBar.visibility = View.VISIBLE
                        titleProgressBar.visibility = View.VISIBLE
                        downloadMap()
                    } else {
                        Intent(applicationContext, WelcomeActivity::class.java).also {
                            sharedPreference.save(PREF_NAME, true)
                            startActivity(it)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = indicatorContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorContainer.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun downloadMap() {
        val progressBar = introSliderViewPager.findViewById<ProgressBar>(R.id.progressBar)
        val titleProgressBar = introSliderViewPager.findViewById<TextView>(R.id.titleProgressBar)

        val offlineManager: OfflineManager =
            OfflineManager(MapInitOptions.getDefaultResourceOptions(this))
        val coordinationPoint: Point? = Point.fromLngLat(27.561481, 53.902496)
        var progressStatus = 0

        /*** Пакет стилей ***/
        val stylePackLoadOptions = StylePackLoadOptions.Builder()
            .glyphsRasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
            .acceptExpired(false)
            .build()

        /*** Пакет стилей ***/

        val tilesetDescriptor = offlineManager.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI(Style.OUTDOORS)
                .minZoom(0)
                .maxZoom(16)
                .build()
        )

        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .geometry(coordinationPoint)
            .descriptors(listOf(tilesetDescriptor))
            .acceptExpired(true)
            .networkRestriction(NetworkRestriction.NONE)
            .build()


        /** Скачать пакет стилей **/
        val stylePackCancelable = offlineManager.loadStylePack(
            Style.OUTDOORS,
            stylePackLoadOptions,
            { progress ->
                progressStatus =
                    (progress.completedResourceCount.toDouble() / progress.requiredResourceCount.toDouble() * 100).toInt()
                Log.i("SearchApiExample", "Loading progress: $progressStatus")
                progressBar.progress = progressStatus
                titleProgressBar.text =
                    "${getString(R.string.download_style_map)}: $progressStatus%"
            },
            { expected ->
                if (expected.isValue) {
                    expected.value?.let { stylePack ->
                        // Style pack download finished successfully
                        progressStatus = 0
                        Log.i("SearchApiExample", "Finishes Style")

                        /** Скачать область плитки **/
                        val tileStore = TileStore.create(filesDir.path + "/mapbox").also {
                            // Set default access token for the created tile store instance
                            it.setOption(
                                TileStoreOptions.MAPBOX_ACCESS_TOKEN,
                                TileDataDomain.MAPS,
                                Value(getString(R.string.mapbox_access_token))
                            )
                            it.setOption(
                                TileStoreOptions.MAPBOX_ACCESS_TOKEN,
                                TileDataDomain.NAVIGATION,
                                Value(getString(R.string.mapbox_access_token))
                            )
                        }
                        val tileRegionCancelable = tileStore.loadTileRegion(
                            "Belarus",
                            tileRegionLoadOptions,
                            { progress ->
                                Log.i(
                                    "SearchApiExample",
                                    "Loading progress download map: $progress"
                                )
                                progressStatus =
                                    (progress.completedResourceCount.toDouble() / progress.requiredResourceCount * 100).toInt()
                                progressBar.progress = progressStatus
                                titleProgressBar.text =
                                    "${getString(R.string.download_map)} $progressStatus%"
                            }
                        ) { expected ->
                            if (expected.isValue) {
                                // Tile region download finishes successfully
                                Log.i("SearchApiExample", "Finishes map")

                                completeDownloadMap()
                                checkDownloadMap = true
                            }
                            expected.error?.let {
                                // Handle errors that occurred during the tile region download.
                                Log.i("SearchApiExample", "ERROR MAP")
                            }
                        }
                        /** Скачать область плитки **/

                    }
                }
                expected.error?.let {
                    // Handle errors that occurred during the style pack download.
                    Log.i("SearchApiExample", "ERROR STYLE")
                }
            }
        )
        /** Скачать пакет стилей **/
    }

    private fun completeDownloadMap() {
        runOnUiThread {
            val textTitle = introSliderViewPager.findViewById<TextView>(R.id.textTitle)
            val progressBar = introSliderViewPager.findViewById<ProgressBar>(R.id.progressBar)
            val titleProgressBar =
                introSliderViewPager.findViewById<TextView>(R.id.titleProgressBar)

            textTitle.visibility = View.VISIBLE
            textTitle.text = getString(R.string.complite_start)
            progressBar.visibility = View.GONE
            titleProgressBar.visibility = View.GONE
//            buttonNext.visibility = View.VISIBLE
            buttonNext.isEnabled = true
            buttonNext.text = "Завершить"
        }
    }
}