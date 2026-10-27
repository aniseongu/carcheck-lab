package edu.skku.map.carchecklab

data class SavedCarResult(
    var vehicleName: String,
    var score: Int,
    var grade: String,
    var recallCount: Int,
    var complaintCount: Int,
    var safetyRating: String
)