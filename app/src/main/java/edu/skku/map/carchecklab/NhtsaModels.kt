package edu.skku.map.carchecklab

import com.google.gson.annotations.SerializedName

data class RecallResponse(
    @SerializedName(value = "Count", alternate = ["count"])
    var count: Int? = 0,

    @SerializedName(value = "Message", alternate = ["message"])
    var message: String? = "",

    @SerializedName(value = "results", alternate = ["Results"])
    var results: ArrayList<RecallItem>? = ArrayList()
)

data class RecallItem(
    @SerializedName(value = "Manufacturer", alternate = ["manufacturer"])
    var manufacturer: String? = "",

    @SerializedName(value = "NHTSACampaignNumber", alternate = ["nhtsaCampaignNumber"])
    var nhtsaCampaignNumber: String? = "",

    @SerializedName(value = "Component", alternate = ["component"])
    var component: String? = "",

    @SerializedName(value = "Summary", alternate = ["summary"])
    var summary: String? = "",

    @SerializedName(value = "Consequence", alternate = ["consequence"])
    var consequence: String? = "",

    @SerializedName(value = "Remedy", alternate = ["remedy"])
    var remedy: String? = ""
)

data class ComplaintResponse(
    @SerializedName(value = "count", alternate = ["Count"])
    var count: Int? = 0,

    @SerializedName(value = "message", alternate = ["Message"])
    var message: String? = "",

    @SerializedName(value = "results", alternate = ["Results"])
    var results: ArrayList<ComplaintItem>? = ArrayList()
)

data class ComplaintItem(
    @SerializedName(value = "odiNumber", alternate = ["ODINumber"])
    var odiNumber: String? = "",

    @SerializedName(value = "components", alternate = ["Components"])
    var components: String? = "",

    @SerializedName(value = "summary", alternate = ["Summary"])
    var summary: String? = ""
)

data class SafetyVehicleResponse(
    @SerializedName(value = "Count", alternate = ["count"])
    var count: Int? = 0,

    @SerializedName(value = "Message", alternate = ["message"])
    var message: String? = "",

    @SerializedName(value = "Results", alternate = ["results"])
    var results: ArrayList<SafetyVehicle>? = ArrayList()
)

data class SafetyVehicle(
    @SerializedName(value = "VehicleId", alternate = ["vehicleId"])
    var vehicleId: String? = "",

    @SerializedName(value = "VehicleDescription", alternate = ["vehicleDescription"])
    var vehicleDescription: String? = ""
)

data class SafetyRatingResponse(
    @SerializedName(value = "Count", alternate = ["count"])
    var count: Int? = 0,

    @SerializedName(value = "Message", alternate = ["message"])
    var message: String? = "",

    @SerializedName(value = "Results", alternate = ["results"])
    var results: ArrayList<SafetyRatingItem>? = ArrayList()
)

data class SafetyRatingItem(
    @SerializedName(value = "OverallRating", alternate = ["overallRating"])
    var overallRating: String? = "",

    @SerializedName(value = "OverallFrontCrashRating", alternate = ["overallFrontCrashRating"])
    var overallFrontCrashRating: String? = "",

    @SerializedName(value = "OverallSideCrashRating", alternate = ["overallSideCrashRating"])
    var overallSideCrashRating: String? = "",

    @SerializedName(value = "RolloverRating", alternate = ["rolloverRating"])
    var rolloverRating: String? = ""
)

data class VinDecodeResponse(
    @SerializedName(value = "Count", alternate = ["count"])
    var count: Int? = 0,

    @SerializedName(value = "Message", alternate = ["message"])
    var message: String? = "",

    @SerializedName(value = "Results", alternate = ["results"])
    var results: ArrayList<VinDecodeItem>? = ArrayList()
)

data class VinDecodeItem(
    @SerializedName(value = "Variable", alternate = ["variable"])
    var variable: String? = "",

    @SerializedName(value = "Value", alternate = ["value"])
    var value: String? = ""
)