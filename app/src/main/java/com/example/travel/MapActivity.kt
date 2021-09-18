package com.example.travel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
//import com.mapbox.navigation.examples.dataMapboxActivityTurnByTurnExperienceBinding
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import kotlinx.android.synthetic.main.activity_map.*
import java.util.Locale
import android.location.LocationManager

import android.R.attr.name
import android.content.Context
import android.location.LocationListener
import com.example.travel.helpers.GlobalHelper
import com.google.android.gms.location.LocationServices


class MapActivity : AppCompatActivity() {
    companion object {
        const val LAT = "lat"
        const val LON = "lon"
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    /**
     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
     */
    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    /**
     * Mapbox Maps entry point obtained from the [MapView].
     * You need to get a new reference to this object whenever the [MapView] is recreated.
     */
    private lateinit var mapboxMap: MapboxMap

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
     */
    private lateinit var mapboxNavigation: MapboxNavigation

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource


    var myLocale: Locale? = null;
    /*
    * Below are generated camera padding values to ensure that the route fits well on screen while
    * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
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
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Извлекает сообщение, которое следует сообщить водителю о предстоящем маневре.
     * По возможности загружает синтезированный аудиофайл, который можно воспроизвести в драйвере.
     */
//    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Observes when a new voice instruction should be played.
     */
//    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
//        speechApi.generate(voiceInstructions, speechCallback)
//    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                },
                { value ->
                }
            )
        }

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onRawLocationChanged(rawLocation: Location) {
// not handled
        }

        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
// update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = keyPoints
            )

// update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

// if this is the first location update the activity has received,
// it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
// update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

// draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

// update top banner with maneuver instructions
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

// update bottom trip progress summary
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

        GlobalHelper.accessLocation(this)

        val lat = intent.getStringExtra(LAT)
        val lon = intent.getStringExtra(LON)

        myLocale = Locale("ru","RU")
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

        initStyle()
//        initNavigation()

        // инициализировать навигацию Mapbox
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
                    // закомментируйте блок настройки двигателя местоположения, чтобы отключить симуляцию
                    // (comment out the location engine setting block to disable simulation)
//                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }

        /*MapboxNavigation(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        ).apply {
            // Это важно для вызова, поскольку [LocationProvider] начнет отправлять обновления местоположения только после начала сеанса поездки.
            startTripSession()
            // Регистрируем наблюдателя местоположения для прослушивания обновлений местоположения, полученных от поставщика местоположения
            registerLocationObserver(locationObserver)
        }*/

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

        // инициализировать api голосовых инструкций и проигрыватель голосовых инструкций
//        speechApi = MapboxSpeechApi(
//            this,
//            getString(R.string.mapbox_access_token),
//            myLocale!!.language
//        )

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

    }

    override fun onStart() {
        super.onStart()

        // зарегистрировать слушателей событий
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
//        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

        if (mapboxNavigation.getRoutes().isEmpty()) {
        // если моделирование включено (ReplayLocationEngine имеет значение NavigationOptions),
        // но мы еще не моделируем, отправляем образец одного местоположения, чтобы установить источник
            mapboxReplayer.pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        eventTimestamp = 0.0,
                        point = Point.fromLngLat(53.90039729131196, 27.55798847067914)
                    )
                )
            )
            mapboxReplayer.playFirstLocation()
        }
    }

    override fun onStop() {
        super.onStop()

        // отменить регистрацию прослушивателей событий, чтобы предотвратить утечки или ненужное потребление ресурсов
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
//        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        MapboxNavigationProvider.destroy()
//        speechApi.cancel()
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
                // обеспечиваем пеленг для источника запроса, чтобы гарантировать, что возвращенный маршрут смотрит в направлении текущего движения пользователя
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

        // начать симуляцию местоположения по основному маршруту
//        startSimulation(routes.first())

        // показать элементы пользовательского интерфейса
//        routeOverview.visibility = View.VISIBLE
        tripProgressCard.visibility = View.VISIBLE

        // переместите камеру в режим обзора, когда станет доступен новый маршрут
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // очистка
        mapboxNavigation.setRoutes(listOf())

        // остановить симуляцию
        mapboxReplayer.stop()

        // скрыть элементы пользовательского интерфейса
        maneuverView.visibility = View.INVISIBLE
        routeOverview.visibility = View.INVISIBLE
        tripProgressCard.visibility = View.INVISIBLE
    }

    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation() {
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigation(
                NavigationOptions.Builder(this)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build()
            ).apply {
                // Это важно для вызова, поскольку [LocationProvider] начнет отправлять обновления местоположения только после начала сеанса поездки.
                startTripSession()
                // Регистрируем наблюдателя местоположения для прослушивания обновлений местоположения, полученных от поставщика местоположения
                registerLocationObserver(locationObserver)
            }
        }
    }

    private fun initStyle() {
        // загрузить стиль карты
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            // добавить прослушиватель долгого щелчка, который ищет маршрут к месту назначения, по которому щелкнули
            mapView.gestures.addOnMapLongClickListener { point ->
                findRoute(point)
                true
            }
        }
    }

    private fun updateCamera(location: Location) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapView.camera.easeTo(
            CameraOptions.Builder()
                // Центрирует камеру в соответствии с указанными значениями долготы и широты.
                .center(Point.fromLngLat(location.longitude, location.latitude))
                // указывает значение масштабирования. Увеличение или уменьшение для увеличения или уменьшения масштаба
                .zoom(12.0)
                // укажите систему отсчета от центра.
                .padding(EdgeInsets(500.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }
}