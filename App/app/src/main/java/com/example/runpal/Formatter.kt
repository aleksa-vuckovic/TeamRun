package com.example.runpal

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Converts a measured value to a String pair.
 * The first string is the formatted number,
 * the second string is the formatted unit.
 */
interface Formatter<T> {
    fun format(value: T): Pair<String, String>
}
object TimeFormatter: Formatter<Long> {
    override fun format(value: Long): Pair<String, String> {
        var t = value/1000
        val secs = t%60
        t/=60
        val mins = t%60
        val hours = t/60

        if (hours == 0L) return "%02d:%02d".format(mins, secs) to ""
        else return "%02d:%02d:%02d".format(hours, mins, secs) to ""
    }
}
object LongTimeFormatter: Formatter<Long> {
    override fun format(value: Long): Pair<String, String> {
        var t = value/1000
        val secs = t%60
        t/=60
        val mins = t%60
        t/=60
        val hours = t%24
        val days = t/24

        if (days >= 1L) return "%dd %dh".format(days,hours)  to ""
        else if (hours >= 1L) return "%dh %dmin".format(hours, mins) to ""
        else return "%dmin".format(mins) to ""
    }
}
object UTCDateTimeFormatter: Formatter<Long> {
    override fun format(value: Long): Pair<String, String> {
        val date = Instant.ofEpochMilli(value)
        val formatted = ZonedDateTime.ofInstant(date, ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        return (formatted + " UTC") to ""
    }
}
object LocalDateTimeFormatter: Formatter<Long> {
    override fun format(value: Long): Pair<String, String> {
        val date = Instant.ofEpochMilli(value)
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) to ""
    }
}
object TimeOnlyFormatter: Formatter<Long> {
    override fun format(value: Long): Pair<String, String> {
        val date = Instant.ofEpochMilli(value)
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss")) to ""
    }
}
object DateOnlyFormatter: Formatter<Long> {
    override fun format(value: Long): Pair<String, String> {
        val date = Instant.ofEpochMilli(value)
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) to ""
    }
}
object MetricDistanceFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        if (value < 1000) return value.toInt().toString() to "m"
        else return "%.2f".format(value/1000) to "km"
    }
}
object ImperialDistanceFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        val miles = Units.METRIC.distance.convert(value, Units.IMPERIAL)
        if (miles >= 0.5) return "%.2f".format(miles) to "mi"
        val feet = (Units.IMPERIAL.distance.scale(miles, to = "ft")).toInt()
        return "%d".format(feet) to "ft"
    }
}
object MetricSpeedFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        return "%.1f".format(value) to "m/s"
    }
}
object MetricPaceFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        if (value < 0.1) return "-" to "min/km"
        val pace = 1.0 / value / 60.0 * 1000.0
        val mins = pace.toInt()
        val secs = ((pace - mins)*60.0).toInt()
        return "%02d:%02d".format(mins, secs) to "min/km"
    }
}
object ImperialSpeedFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        val res = Units.METRIC.speed.convert(value, Units.IMPERIAL)
        return "%.1f".format(res) to "mph"
    }
}
object ImperialPaceFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        if (value < 0.1) return "-" to "min/mi"
        val res = Units.METRIC.speed.convert(value, Units.IMPERIAL)
        val pace = 1/res * 60
        val mins = pace.toInt()
        val secs = ((pace - mins)*60.0).toInt()
        return "%02d:%02d".format(mins, secs) to "min/mi"
    }
}
object AltitudeFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        val meters = value.toInt()
        if (meters >= 1000) return "%.1f".format(value/1000) to "km"
        else return meters.toString() to "m"
    }
}
object KcalFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        return value.toInt().toString() to "kcal"
    }
}
object EmptyFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        return "" to ""
    }
}
object MetricWeightFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        return value.toInt().toString() to "kg"
    }
}
object ImperialWeightFormatter: Formatter<Double> {
    override fun format(value: Double): Pair<String, String> {
        return value.toInt().toString() to "lb"
    }
}
fun Pair<String, String>.join(sep: String = ""): String = first + sep + second

enum class Units(
    val speed: Quality,
    val pace: Quality,
    val distance: Quality,
    val weight: Quality
) {
    METRIC(
        speed = Quality(
            formatter = MetricSpeedFormatter,
            subunits = listOf("m/s, km/h"),
            scales = listOf(1.0, 3.6),
            primary = "m/s",
            lowrange = "m/s",
            highrange = "m/s",
            conversions = listOf(1.0, KM_TO_MILE*3.6)
        ),
        pace = Quality(
            formatter = MetricPaceFormatter,
            subunits = listOf("min/km"),
            scales = listOf(1.0),
            primary = "min/km",
            lowrange = "min/km",
            highrange = "min/km",
            conversions = listOf(1.0, 1/KM_TO_MILE)
        ),
        distance = Quality(
            formatter = MetricDistanceFormatter,
            subunits = listOf("m", "km"),
            scales = listOf(1.0, 0.001),
            primary = "m",
            lowrange = "m",
            highrange = "km",
            conversions = listOf(1.0, KM_TO_MILE*0.001)
        ),
        weight = Quality(
            formatter = MetricWeightFormatter,
            subunits = listOf("g", "kg", "t"),
            scales = listOf(0.001, 1.0, 1000.0),
            primary = "kg",
            lowrange = "g",
            highrange = "t",
            conversions = listOf(1.0, 1/LB_TO_KG)
        )
    ),
    IMPERIAL(
        speed = Quality(
            formatter = ImperialSpeedFormatter,
            subunits = listOf("mph"),
            scales = listOf(1.0),
            primary = "mph",
            lowrange = "mph",
            highrange = "mph",
            conversions = listOf(1/(KM_TO_MILE*3.6), 1.0)
        ),
        pace = Quality(
            formatter = ImperialPaceFormatter,
            subunits = listOf("min/mi"),
            scales = listOf(1.0),
            primary = "min/mi",
            lowrange = "min/mi",
            highrange = "min/mi",
            conversions = listOf(KM_TO_MILE, 1.0)
        ),
        distance = Quality(
            formatter = ImperialDistanceFormatter,
            subunits = listOf("ft", "mi"),
            scales = listOf(MILE_TO_FT, 1.0),
            primary = "mi",
            lowrange = "ft",
            highrange = "mi",
            conversions = listOf(1/KM_TO_MILE, 1.0)
        ),
        weight = Quality(
            formatter = ImperialWeightFormatter,
            subunits = listOf("lb"),
            scales = listOf(1.0),
            primary = "lb",
            lowrange = "lb",
            highrange = "lb",
            conversions = listOf(LB_TO_KG, 1.0)
        )
    );

    val next: Units
        get() = Units.values()[(ordinal + 1) % Units.values().size]

    class Quality(
        val formatter: Formatter<Double>,
        val subunits: List<String>,
        val scales: List<Double>,
        val primary: String,
        val lowrange: String,
        val highrange: String,
        val conversions: List<Double>
    ) {
        /**
         * Convert to other units, using standard subunits.
         */
        fun convert(value: Double, units: Units): Double = conversions[units.ordinal] * value
        fun scaleOf(subunit: String) = scales[subunits.indexOfFirst { it.equals(subunit, true) }]
        fun scale(value: Double, from: String? = null, to: String? = null): Double = value / scaleOf(from ?: primary) * scaleOf(to ?: primary)
    }
}