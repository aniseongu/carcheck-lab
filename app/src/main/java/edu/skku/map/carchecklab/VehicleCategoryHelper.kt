package edu.skku.map.carchecklab

class VehicleCategoryHelper {
    companion object {

        fun isPureElectric(category: String): Boolean {
            val upper = category.uppercase().trim()

            if (upper.contains("PHEV")) {
                return false
            }

            if (upper.contains("HEV")) {
                return false
            }

            if (upper.contains("HYBRID")) {
                return false
            }

            if (upper.contains("PLUG-IN")) {
                return false
            }

            if (upper == "EV") {
                return true
            }

            if (upper.contains(" EV")) {
                return true
            }

            if (upper.contains("EV ")) {
                return true
            }

            if (upper.contains("ELECTRIC")) {
                return true
            }

            if (upper.contains("BEV")) {
                return true
            }

            if (upper.contains("BATTERY ELECTRIC")) {
                return true
            }

            return false
        }

        fun isTruck(category: String): Boolean {
            return category.uppercase().contains("TRUCK")
        }

        fun isSuv(category: String): Boolean {
            return category.uppercase().contains("SUV")
        }

        fun isVan(category: String): Boolean {
            val upper = category.uppercase()

            return upper.contains("VAN") ||
                    upper.contains("MINIVAN")
        }

        fun isSedan(category: String): Boolean {
            return category.uppercase().contains("SEDAN")
        }
    }
}