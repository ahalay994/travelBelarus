package com.example.travel

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.example.travel.helpers.GlobalHelper
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.*
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.options.RoutingTilesOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {
    companion object {
        const val LAT = "lat"
        const val LON = "lon"
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    private lateinit var mapView: MapView

    /**
     * Точка входа в карты Mapbox, полученная из [MapView].
     * Вам нужно получать новую ссылку на этот объект всякий раз, когда [MapView] воссоздается.
     */
    private lateinit var mapboxMap: MapboxMap

    /**
     * Точка входа в систему навигации Mapbox. Для приложения должен быть только один экземпляр этого объекта.
     * Вы можете использовать [MapboxNavigationProvider], чтобы помочь создать и получить этот экземпляр.
     */
    private lateinit var mapboxNavigation: MapboxNavigation

    /**
     * Используется для выполнения переходов камеры на основе данных, сгенерированных [viewportDataSource].
     * Сюда входят переходы от обзора маршрута к отслеживанию маршрута и постоянное обновление камеры по мере изменения местоположения.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Создает кадры камеры на основе данных о местоположении и маршруте для выполнения [navigationCamera].
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
    * Ниже приведены сгенерированные значения заполнения камеры, чтобы гарантировать, что маршрут хорошо вписывается в экран,
    * в то время как другие элементы накладываются поверх карты (включая вид инструкций, кнопки и т. Д.)
    */
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    /**
     * Создает обновления для [MapboxManeuverView], чтобы отображать инструкции по предстоящему маневру и оставшееся расстояние до точки маневра.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Создает обновления для [MapboxTripProgressView], которые включают оставшееся время и расстояние до пункта назначения.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Создает обновления для [routeLineView] с геометрией и свойствами маршрутов, которые должны быть нарисованы на карте.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Рисует линии маршрута на карте на основе данных из [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Создает обновления для [routeArrowView] с геометрией и свойствами стрелок маневра, которые должны быть нарисованы на карте.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Рисует стрелки маневра на карте на основе данных [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * OfflineManager
     */
    var offlineManager: OfflineManager? = null

    /**
     * TileStore
     */
//    private lateinit var tileStore: TileStore

    /**
     * [NavigationLocationProvider] - служебный класс, который помогает предоставлять обновления местоположения,
     * сгенерированные SDK навигации, в SDK Maps для обновления индикатора местоположения пользователя на карте.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Получает уведомления с обновлениями местоположения.
     *
     * Предоставляет необработанные обновления, поступающие непосредственно из служб определения местоположения,
     * и обновления, улучшенные с помощью SDK для навигации (очищенные и сопоставленные с дорогой).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        /**
         * Вызывается, как только [Location] становится доступным.
         */
        override fun onRawLocationChanged(rawLocation: Location) {
            Log.i("rawLocation", rawLocation.toString())
            // Не реализовано в этом примере.
            // Однако, если вы хотите, вы также можете использовать этот обратный вызов для получения обновлений местоположения,
            // но, как следует из названия, это необработанные обновления местоположения, которые обычно шумны.
        }

        /**
         * Обеспечивает наилучшее возможное обновление местоположения, привязанное к маршруту или сопоставленное с дорогой, если это возможно.
         */
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            // обновить местоположение шайбы на карте
//            navigationLocationProvider.changePosition(
//                location = enhancedLocation,
//                keyPoints = keyPoints
//            )

            // обновить положение камеры с учетом нового местоположения
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // если это первое обновление местоположения, полученное действием,
            // лучше сразу переместить камеру в текущее местоположение пользователя
//            if (!firstLocationUpdateReceived) {
//                firstLocationUpdateReceived = true
//                navigationCamera.requestNavigationCameraToOverview(
//                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
//                        .maxDuration(0) // мгновенный переход
//                        .build()
//                )
//            }
        }
    }

    /**
     * Получает уведомление о продвижении по текущему активному маршруту.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // обновить положение камеры с учетом продвинутого фрагмента маршрута
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // Нарисуйте на карте стрелку предстоящего маневра
        val style = mapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // обновить верхний баннер с инструкциями по маневрам
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this@MapActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                maneuverView.visibility = View.VISIBLE
                maneuverView.renderManeuvers(maneuvers)
            }
        )

        // обновить сводку хода нижней поездки
        tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    /**
     * Получает уведомление при изменении отслеживаемых маршрутов.
     *
     * Изменение может означать:
     * - маршруты меняются с помощью [MapboxNavigation.setRoutes]
     * - обновляются аннотации маршрутов (например, аннотации заторов, указывающие на текущий трафик по маршруту)
     * - водитель сошёл с маршрута, и было выполнено изменение маршрута
     */
    private val routesObserver = RoutesObserver { routes ->
        if (routes.isNotEmpty()) {
            // асинхронно генерировать геометрию маршрута и отображать ее
            val routeLines = routes.map { RouteLine(it, null) }

            routeLineApi.setRoutes(
                routeLines
            ) { value ->
                mapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // обновить положение камеры, чтобы учесть новый маршрут
            viewportDataSource.onRouteChanged(routes.first())
            viewportDataSource.evaluate()
        } else {
            // удалить линию маршрута и стрелку маршрута с карты
            val style = mapboxMap.getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // удалить ссылку на маршрут из оценок положения камеры
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val lat = intent.getStringExtra(LAT)
        val lon = intent.getStringExtra(LON)

        val coordinationPoint: Point? = Point.fromLngLat(lon!!.toDouble(), lat!!.toDouble())

        /*** Запрос на получение доступа к месторасположению ***/
        GlobalHelper.accessLocation(this)
        /*** --- ***/

        // инициализировать навигацию Mapbox
        val tileStore = TileStore.create(filesDir.path + "/mapbox").apply {
            setOption(
                TileStoreOptions.MAPBOX_ACCESS_TOKEN,
                TileDataDomain.MAPS,
                Value(getString(R.string.mapbox_access_token))
            )
            setOption(
                TileStoreOptions.MAPBOX_ACCESS_TOKEN,
                TileDataDomain.NAVIGATION,
                Value(getString(R.string.mapbox_access_token)
                )
            )
        }

        val routingTilesOptions = RoutingTilesOptions.Builder()
            .tileStore(tileStore)
            .build()

        val navOptions = NavigationOptions.Builder(this)
            .accessToken(getString(R.string.mapbox_access_token))
            .routingTilesOptions(routingTilesOptions)
            .build()

        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                navOptions
            ).apply {
                // Это важно для вызова, поскольку [LocationProvider] начнет отправлять обновления местоположения только после начала сеанса поездки.
                startTripSession()
                // Регистрируем наблюдателя местоположения для прослушивания обновлений местоположения, полученных от поставщика местоположения
                registerLocationObserver(locationObserver)
            }
        }

        val resourceOptions = ResourceOptions.Builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .tileStore(tileStore)
            .tileStoreUsageMode(TileStoreUsageMode.READ_AND_UPDATE)
            .build()

        // set map options
        val mapOptions = MapOptions.Builder().applyDefaultParams(this)
            .constrainMode(ConstrainMode.HEIGHT_ONLY)
            .glyphsRasterizationOptions(
                GlyphsRasterizationOptions.Builder()
                    .rasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
                    .fontFamily("sans-serif")
                    .build()
            )
            .build()

        val initialCameraOptions = CameraOptions.Builder()
            .center(coordinationPoint)
            .zoom(13.0)
            .bearing(120.0)
            .build()

        val mapInitOptions = MapInitOptions(this, resourceOptions, mapOptions, MapInitOptions.defaultPluginList, initialCameraOptions, true)
        mapView = MapView(this, mapInitOptions)

        mapViewContainer.addView(mapView)
        mapboxMap = mapView.getMapboxMap()

        // инициализировать шайбу местоположения
        mapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@MapActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

        // инициализация стилей карты
//        initStyle()
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            bitmapFromDrawableRes(
                this@MapActivity,
                R.drawable.ic_baseline_add_location_alt_24
            )?.let {
                val annotationApi = mapView.annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(27.559299665291515, 53.900520195244425))
                    .withIconImage(it)
                pointAnnotationManager.create(pointAnnotationOptions)
            }

            addAnnotationToMap()
        }

        // инициализировать навигационную камеру
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            mapView.camera,
            viewportDataSource
        )
        // установите прослушиватель жизненного цикла анимации, чтобы убедиться, что NavigationCamera останавливается
        // автоматически следует за местоположением пользователя при взаимодействии с картой
        mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // показывает / скрывает кнопку повторного центрирования в зависимости от состояния камеры
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> recenter.visibility = View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> recenter.visibility = View.VISIBLE
            }
        }
        // установить значения отступов в зависимости от ориентации экрана и видимого view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // убедитесь, что вы используете одни и те же DistanceFormatterOptions для разных функций
        val distanceFormatterOptions = mapboxNavigation.navigationOptions.distanceFormatterOptions

        // инициализировать api маневра, который передает данные в представление маневра верхнего баннера
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // инициализировать нижний прогресс
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // инициализируем строку маршрута, withRouteLineBelowLayerId указывается для размещения
        // линия маршрута под слоем обозначений дорог на карте
        // значение этой опции будет зависеть от стиля, который вы используете
        // и под каким слоем линия маршрута должна быть помещена в стек слоев карты
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // инициализировать представление стрелки маневра для рисования стрелок на карте
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        // инициализировать взаимодействие просмотра
        stop.setOnClickListener {
            clearRouteAndStopNavigation()
        }
        recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }

        // запускаем сеанс поездки для получения обновлений местоположения на свободном вождении
        // и позже, когда маршрут установлен, также получаем обновления хода маршрута
        mapboxNavigation.startTripSession()

        /*** Прокладываем маршрут ***/
        createRoute.setOnClickListener {
            if (coordinationPoint != null) {
                findRoute(coordinationPoint)
                createRoute.visibility = View.INVISIBLE
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val mapsTilesetDescriptor = offlineManager?.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI(Style.MAPBOX_STREETS)
                .minZoom(15)
                .maxZoom(16)
                .build()
        )

        val navTilesetDescriptor = mapboxNavigation.tilesetDescriptorFactory.getLatest()

        val pointFirst = Point.fromLngLat(27.71402866991587, 53.96800608088429)
        val pointSecond = Point.fromLngLat(27.392335275633705, 53.83814912274236)
        val polygon: Polygon = Polygon.fromLngLats(listOf(listOf(pointFirst, pointSecond)))

        val polygonFeatureJson =
            """
            {
                "type": "Feature",
                "properties": {},
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [27.500334033469958,53.867920268096356],
                            [27.63680481706351,53.871462814516555],
                            [27.628908394643876,53.92456501438775],
                            [27.502393970104944,53.92860812977665]
                        ]
                    ]
                }
            }
            """

        val singleFeature = Feature.fromJson(polygonFeatureJson)
        val polygonJSON = singleFeature.geometry() as Polygon?

        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .geometry(polygonJSON)
            .descriptors(listOf(mapsTilesetDescriptor, navTilesetDescriptor))
            .build()

        // TODO ERROR
        /*val tileRegionCancelable = tileStore.loadTileRegion(
            "TOKYO",
            tileRegionLoadOptions,
            { progress ->
//                Log.i("progress", progress.toString())
            }
        ) { expected ->
            if (expected.isValue) {
                // Tile region download finishes successfully
//                Log.i("download", "Tile region download finishes successfully")
            } else {
//                Log.i("download", "Error")
                // Handle errors that occurred during the tile region download
            }
        }*/
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        downloadMap("Minsk_15-16")
    }

    override fun onStart() {
        super.onStart()
        // зарегистрировать слушателей событий
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun onStop() {
        super.onStop()
        // отменить регистрацию прослушивателей событий, чтобы предотвратить утечки или ненужное потребление ресурсов
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        MapboxNavigationProvider.destroy()
    }

    private fun findRoute(destination: Point) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        // выполняем запрос маршрута
        // рекомендуется использовать applyDefaultNavigationOptions и applyLanguageAndVoiceUnitOptions,
        // чтобы убедиться, что запрос маршрута оптимизирован для поддержки всех функций Navigation SDK
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(originPoint, destination))
                // обеспечиваем пеленг для источника запроса, чтобы гарантировать,
                // что возвращенный маршрут смотрит в направлении текущего движения пользователя
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }
            }
        )
    }

    private fun setRouteAndStartNavigation(routes: List<DirectionsRoute>) {
        // устанавливаем маршруты, где первый маршрут в списке - это основной маршрут, который будет использоваться для активного руководства
        mapboxNavigation.setRoutes(routes)

        // показать элементы пользовательского интерфейса
        routeOverview.visibility = View.VISIBLE
        tripProgressCard.visibility = View.VISIBLE

        // переместите камеру в режим обзора, когда станет доступен новый маршрут
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // очистка
        mapboxNavigation.setRoutes(listOf())

        // скрыть элементы пользовательского интерфейса
        maneuverView.visibility = View.INVISIBLE
//        routeOverview.visibility = View.INVISIBLE
        tripProgressCard.visibility = View.INVISIBLE
        createRoute.visibility = View.VISIBLE
    }

    private fun initStyle() {
        // загрузить стиль карты
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS,
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    addAnnotationToMap()
                }
            }
        )
//        {
            // добавить прослушиватель долгого щелчка, который ищет маршрут к месту назначения, по которому щелкнули
            /*mapView.gestures.addOnMapLongClickListener { point ->
                findRoute(point)
                true
            }*/
//        }
    }

    private fun addAnnotationToMap() {
        // Создайте экземпляр Annotation API и получите PointAnnotationManager.
        bitmapFromDrawableRes(
            this@MapActivity,
            R.drawable.ic_baseline_location_searching_24
        )?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)
            // Задайте параметры для результирующего слоя символов.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                // Определите географические координаты.
                .withPoint(Point.fromLngLat(18.06, 59.31))
                // Укажите растровое изображение, назначенное аннотации точки
                // Растровое изображение будет добавлено в стиль карты автоматически..
                .withIconImage(it)
                // Добавьте полученную точку аннотации на карту.
            pointAnnotationManager.create(pointAnnotationOptions)

            val position = CameraOptions.Builder()
                .center(Point.fromLngLat(51.50550, -0.07520))
                .zoom(10.0)
                .build()
        }
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) = convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))
    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // копирование рисованного объекта, чтобы не манипулировать одной и той же ссылкой
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}