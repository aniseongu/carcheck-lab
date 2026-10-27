package edu.skku.map.carchecklab

data class WeatherAdvice(
    var modeName: String,
    var advice: String,
    var colorSuggestion: String
)

class WeatherAdvisor {
    companion object {

        fun advise(
            weather: WeatherInfo,
            city: String,
            priority: String,
            category: String
        ): WeatherAdvice {
            if (!weather.success) {
                return WeatherAdvice(
                    "Weather Lens: Profile fallback",
                    "Check: tires · brakes · wipers",
                    "Ask: Any water leak, rust, or battery issue?"
                )
            }

            val main = weather.main.uppercase()
            val temp = weather.tempC
            val isElectric = VehicleCategoryHelper.isPureElectric(category)

            if (
                main.contains("RAIN") ||
                main.contains("DRIZZLE") ||
                main.contains("THUNDER")
            ) {
                return WeatherAdvice(
                    "Weather Lens: Rain check",
                    "Check: tire tread · brake response · water leaks",
                    "Ask: Any door, trunk, sunroof, or underbody water damage?"
                )
            }

            if (main.contains("SNOW") || temp <= 0.0) {
                if (isElectric) {
                    return WeatherAdvice(
                        "Weather Lens: EV cold range",
                        "Check: battery warranty · winter range · heat pump",
                        "Ask: What is the real winter range and charging history?"
                    )
                }

                return WeatherAdvice(
                    "Weather Lens: Cold start check",
                    "Check: 12V battery · heater · defroster",
                    "Ask: Was it used with winter tires or AWD service records?"
                )
            }

            if (temp >= 30.0) {
                if (isElectric) {
                    return WeatherAdvice(
                        "Weather Lens: EV heat check",
                        "Check: battery cooling · tire wear · A/C",
                        "Ask: Any battery cooling, charging, or summer range issue?"
                    )
                }

                return WeatherAdvice(
                    "Weather Lens: Heat check",
                    "Check: A/C · cooling fan · coolant smell",
                    "Ask: Can I test the A/C while idling and driving?"
                )
            }

            if (isElectric) {
                return WeatherAdvice(
                    "Weather Lens: EV range check",
                    "Check: battery warranty · charging history · tire wear",
                    "Ask: What is the real winter or summer range?"
                )
            }

            if (isUrbanCity(city)) {
                return WeatherAdvice(
                    "Weather Lens: City daily check",
                    "Check: parking sensors · camera · bumper scratches",
                    "Ask: Any parking damage, wheel scratches, or sensor repair?"
                )
            }

            return WeatherAdvice(
                "Weather Lens: Normal test drive",
                "Check: tires · brakes · visibility",
                "Ask: Can I see recent tire and brake service records?"
            )
        }

        private fun isUrbanCity(city: String): Boolean {
            val upper = city.uppercase()

            return upper.contains("SUWON") ||
                    upper.contains("SEOUL") ||
                    upper.contains("INCHEON") ||
                    upper.contains("SEONGNAM") ||
                    upper.contains("SEOSAN") ||
                    upper.contains("YONGIN") ||
                    city.contains("수원") ||
                    city.contains("서울") ||
                    city.contains("인천") ||
                    city.contains("성남") ||
                    city.contains("서산") ||
                    city.contains("용인")
        }
    }
}