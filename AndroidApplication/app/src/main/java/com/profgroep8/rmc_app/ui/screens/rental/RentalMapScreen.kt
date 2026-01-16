package com.profgroep8.rmc_app.ui.screens.rental

import RmcScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.network.models.domain.RentalWithCarInfo
import com.example.network.services.UserProvider
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.viewmodel.rental.RentalMapViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.material3.DisappearingCompassButton
import org.maplibre.compose.material3.ScaleBar
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

data class SelectedRental(
    val rentalId: Int,
    val title: String,
    val snippet: String
)

@Composable
fun RentalMapScreen(
    viewModel: RentalMapViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit,
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Home.name) }
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        navigateBack()
    }

    Scaffold(
        topBar = {
            RmcAppBar(
                title = stringResource(R.string.rental_map),
                subtitle = stringResource(R.string.rental_map_sub),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_large)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: stringResource(R.string.unknown_error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = { viewModel.retry() }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                uiState.rentals.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_rentals_on_map),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    RentalMapView(
                        rentals = uiState.rentals,
                        navigateToScreen = navigateToScreen
                    )
                }
            }
        }
    }
}

@Composable
private fun RentalMapView(
    rentals: List<RentalWithCarInfo>,
    navigateToScreen: (String) -> Unit
) {
    val currentUserId = UserProvider.user?.userID
    val rentingText = stringResource(R.string.renting)
    val rentingOutText = stringResource(R.string.renting_out)
    var selectedRental by remember { mutableStateOf<SelectedRental?>(null) }

    val rentalFeatures = remember(rentals, currentUserId) {
        rentals.map { rentalWithCar ->
            createRentalFeature(rentalWithCar, currentUserId, rentingText, rentingOutText)
        }
    }

    val defaultCenter = Position(latitude = 51.5837146, longitude = 4.79711)
    val initialPosition = if (rentals.isNotEmpty()) {
        val firstRental = rentals.first().rental.startRentalLocation
        Position(
            latitude = firstRental.latitude.toDouble(),
            longitude = firstRental.longitude.toDouble()
        )
    } else {
        defaultCenter
    }

    val cameraState = rememberCameraState(
        CameraPosition(
            target = initialPosition,
            zoom = 10.0,
            tilt = 0.0,
            bearing = 0.0
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = cameraState,
            styleState = rememberStyleState(),
            onMapClick = { _, _ ->
                selectedRental = null
                ClickResult.Pass
            },
            options = MapOptions(
                ornamentOptions = OrnamentOptions.AllDisabled,
                gestureOptions = GestureOptions(
                    isTiltEnabled = false,
                    isZoomEnabled = true,
                    isRotateEnabled = false,
                    isScrollEnabled = true,
                    isDoubleTapEnabled = true
                ),
            )
        ) {
            RentalMarkersLayer(
                rentalFeatures = rentalFeatures,
                onMarkerClick = { rental ->
                    selectedRental = rental
                }
            )
        }

        OverlayOrnaments(cameraState)

        selectedRental?.let { rental ->
            RentalInfoPopup(
                rental = rental,
                onPopupClick = {
                    navigateToScreen("${RmcScreen.RentalInformation.name}/${rental.rentalId}")
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    AnimateToBounds(cameraState, rentalFeatures)
}

@Composable
private fun RentalMarkersLayer(
    rentalFeatures: List<Feature<Point, JsonObject>>,
    onMarkerClick: (SelectedRental) -> Unit
) {
    if (rentalFeatures.isEmpty()) return

    val source: GeoJsonSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            geoJson = FeatureCollection(features = rentalFeatures)
        )
    )

    SymbolLayer(
        id = "rental-markers-layer",
        source = source,
        iconImage = image(
            value = rememberVectorPainter(image = Icons.Filled.LocationOn),
            drawAsSdf = true
        ),
        iconColor = const(Color(0xFFE53935)),
        iconSize = const(1.5f),
        onClick = { features ->
            val feature = features.firstOrNull() ?: return@SymbolLayer ClickResult.Pass
            try {
                val json = Json.parseToJsonElement(feature.toJson()).jsonObject
                val properties = json["properties"]?.jsonObject ?: return@SymbolLayer ClickResult.Pass
                val rentalId = properties["rentalId"]?.jsonPrimitive?.int ?: return@SymbolLayer ClickResult.Pass
                val title = properties["title"]?.jsonPrimitive?.content ?: ""
                val snippet = properties["snippet"]?.jsonPrimitive?.content ?: ""
                onMarkerClick(SelectedRental(rentalId, title, snippet))
                ClickResult.Consume
            } catch (e: Exception) {
                ClickResult.Pass
            }
        }
    )
}

@Composable
private fun RentalInfoPopup(
    rental: SelectedRental,
    onPopupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onPopupClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = rental.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = rental.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.tap_for_details),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AnimateToBounds(
    cameraState: CameraState,
    features: List<Feature<Point, JsonObject>>
) {
    var hasAnimated by remember { mutableStateOf(false) }

    LaunchedEffect(features, hasAnimated) {
        if (hasAnimated || features.isEmpty()) return@LaunchedEffect

        val positions = features.map { feature ->
            feature.geometry.coordinates
        }

        val boundingBox = positionsToBoundingBox(positions) ?: return@LaunchedEffect

        cameraState.animateTo(
            boundingBox = boundingBox,
            padding = PaddingValues(48.dp),
            duration = 1500.milliseconds,
            tilt = 0.0
        )

        hasAnimated = true
    }
}

@Composable
private fun BoxScope.OverlayOrnaments(cameraState: CameraState) {
    ScaleBar(
        metersPerDp = cameraState.metersPerDpAtTarget,
        modifier = Modifier.align(Alignment.TopStart),
    )

    DisappearingCompassButton(
        cameraState = cameraState,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp),
    )
}

private fun createRentalFeature(
    rentalWithCar: RentalWithCarInfo,
    currentUserId: Int?,
    rentingText: String,
    rentingOutText: String
): Feature<Point, JsonObject> {
    val rental = rentalWithCar.rental
    val car = rentalWithCar.car
    val startLocation = rental.startRentalLocation

    val position = Position(
        latitude = startLocation.latitude.toDouble(),
        longitude = startLocation.longitude.toDouble()
    )

    val isOwner = currentUserId != null && car?.userID == currentUserId
    val title = if (car != null) {
        "${car.licensePlate} - ${car.brand} ${car.model}"
    } else {
        "Car #${rental.carID}"
    }
    val snippet = if (isOwner) rentingOutText else rentingText

    return Feature(
        geometry = Point(coordinates = position),
        properties = buildJsonObject {
            put("rentalId", rental.rentalID)
            put("title", title)
            put("snippet", snippet)
            put("isOwner", isOwner)
        }
    )
}

private fun positionsToBoundingBox(positions: List<Position>): BoundingBox? {
    if (positions.isEmpty()) return null

    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    var minLon = Double.POSITIVE_INFINITY
    var maxLon = Double.NEGATIVE_INFINITY

    for (p in positions) {
        minLat = min(minLat, p.latitude)
        maxLat = max(maxLat, p.latitude)
        minLon = min(minLon, p.longitude)
        maxLon = max(maxLon, p.longitude)
    }

    return BoundingBox(
        southwest = Position(longitude = minLon, latitude = minLat),
        northeast = Position(longitude = maxLon, latitude = maxLat)
    )
}
