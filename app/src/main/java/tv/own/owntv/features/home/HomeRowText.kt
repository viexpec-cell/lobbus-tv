package tv.own.owntv.features.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import tv.own.owntv.lobbus.home.LobbusTituloFileira
import tv.own.owntv.ui.theme.mpx
import tv.own.owntv.R
import tv.own.owntv.core.model.HomeLiveRowMode
import tv.own.owntv.core.model.HomeRow

@Composable
fun HomeRow.displayTitle(): String = stringResource(
    when (this) {
        HomeRow.TRENDING -> R.string.home_row_now_trending
        HomeRow.HERO -> R.string.home_row_keep_watching
        HomeRow.LIVE_SPORTS -> R.string.sports_home_row
        HomeRow.RECENT_CHANNELS -> R.string.home_row_recent_channels
        HomeRow.FAVORITE_CHANNELS -> R.string.home_row_favorite_channels
        HomeRow.FAVORITE_MOVIES -> R.string.home_row_favorite_movies
        HomeRow.FAVORITE_SERIES -> R.string.home_row_favorite_series
        HomeRow.CONTINUE_MOVIES -> R.string.home_row_continue_movies
        HomeRow.CONTINUE_SERIES -> R.string.home_row_continue_series
    },
)

@Composable
fun HomeRow.settingsDescription(): String = stringResource(
    when (this) {
        HomeRow.TRENDING -> R.string.home_row_trending_description
        HomeRow.HERO -> R.string.home_row_hero_description
        HomeRow.LIVE_SPORTS -> R.string.sports_home_row_description
        HomeRow.RECENT_CHANNELS -> R.string.home_row_recent_description
        HomeRow.FAVORITE_CHANNELS -> R.string.home_row_favorite_description
        HomeRow.FAVORITE_MOVIES -> R.string.home_row_favorite_movies_description
        HomeRow.FAVORITE_SERIES -> R.string.home_row_favorite_series_description
        HomeRow.CONTINUE_MOVIES -> R.string.home_row_continue_movies_description
        HomeRow.CONTINUE_SERIES -> R.string.home_row_continue_series_description
    },
)

/** "42 min", "1 h 05 min". */
@Composable
fun durationText(minutes: Long): String =
    if (minutes >= 60) {
        stringResource(R.string.home_duration_hours_minutes, (minutes / 60).toInt(), (minutes % 60).toInt())
    } else {
        stringResource(R.string.player_duration_minutes, minutes.toInt())
    }

/** "31 min left", "1 h 05 min left". */
@Composable
fun timeLeftText(minutes: Long): String = stringResource(R.string.home_time_left, durationText(minutes))

/**
 * `.rowh`: a Home row's title, with an optional dim word after it — the count ("Keep watching 5")
 * or "On now". Cards start 46 below its top (48 under the hero).
 *
 * LOBBUS: the Lobbus prototype draws this header its own way (last word in the accent with a full
 * stop, counter in Geist Mono after it), so the drawing lives in `lobbus/home/LobbusHeroi.kt` and
 * this stays the one place Home asks for a header. [ordem] is the section number the counter shows
 * ("[02] 5 TÍTULOS"); null leaves the counter unnumbered.
 */
@Composable
fun HomeRowHeader(title: String, small: String?, start: Dp, big: Boolean = false, ordem: Int? = null) {
    LobbusTituloFileira(
        titulo = title,
        contador = small,
        ordem = ordem,
        altura = (if (big) 48 else 46).mpx,
        inicio = start,
    )
}

@Composable
fun HomeLiveRowMode.displayLabel(): String = stringResource(
    when (this) {
        HomeLiveRowMode.CARDS -> R.string.home_row_cards
        HomeLiveRowMode.ON_NOW -> R.string.home_row_on_now
    },
)
