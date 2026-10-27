package edu.skku.map.carchecklab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class VehicleReview(
    var userId: String,
    var vehicleKey: String,
    var rating: Int,
    var comment: String,
    var createdAt: Long,
    var updatedAt: Long
)

class ReviewManager {
    companion object {
        private const val PREF_NAME = "carcheck_reviews"
        private const val KEY_REVIEWS = "reviews_json"

        fun makeVehicleKey(
            year: Int,
            make: String,
            model: String
        ): String {
            return year.toString() + "_" + make.uppercase() + "_" + model.uppercase()
        }

        fun addOrUpdateReview(
            context: Context,
            review: VehicleReview
        ) {
            val reviews = getAllReviews(context)
            var foundIndex = -1

            for (i in 0 until reviews.size) {
                if (
                    reviews[i].vehicleKey == review.vehicleKey &&
                    reviews[i].userId == review.userId
                ) {
                    foundIndex = i
                }
            }

            if (foundIndex >= 0) {
                val old = reviews[foundIndex]
                reviews[foundIndex] = VehicleReview(
                    review.userId,
                    review.vehicleKey,
                    review.rating,
                    review.comment,
                    old.createdAt,
                    System.currentTimeMillis()
                )
            } else {
                reviews.add(review)
            }

            saveAllReviews(context, reviews)
        }

        fun deleteReview(
            context: Context,
            vehicleKey: String,
            userId: String
        ) {
            val reviews = getAllReviews(context)
            val newReviews = ArrayList<VehicleReview>()

            for (i in 0 until reviews.size) {
                val review = reviews[i]

                if (!(review.vehicleKey == vehicleKey && review.userId == userId)) {
                    newReviews.add(review)
                }
            }

            saveAllReviews(context, newReviews)
        }

        fun getMyReview(
            context: Context,
            vehicleKey: String,
            userId: String
        ): VehicleReview? {
            val reviews = getAllReviews(context)

            for (i in 0 until reviews.size) {
                val review = reviews[i]

                if (review.vehicleKey == vehicleKey && review.userId == userId) {
                    return review
                }
            }

            return null
        }

        fun getReviewsForVehicle(
            context: Context,
            vehicleKey: String
        ): ArrayList<VehicleReview> {
            val all = getAllReviews(context)
            val result = ArrayList<VehicleReview>()

            for (i in 0 until all.size) {
                if (all[i].vehicleKey == vehicleKey) {
                    result.add(all[i])
                }
            }

            return result
        }

        fun getAverageRating(
            context: Context,
            vehicleKey: String
        ): Double {
            val reviews = getReviewsForVehicle(context, vehicleKey)

            if (reviews.size == 0) {
                return 0.0
            }

            var sum = 0

            for (i in 0 until reviews.size) {
                sum += reviews[i].rating
            }

            return sum.toDouble() / reviews.size.toDouble()
        }

        fun getReviewCount(
            context: Context,
            vehicleKey: String
        ): Int {
            return getReviewsForVehicle(context, vehicleKey).size
        }

        private fun getAllReviews(context: Context): ArrayList<VehicleReview> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_REVIEWS, "[]") ?: "[]"
            val array = JSONArray(raw)
            val result = ArrayList<VehicleReview>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                val createdAt = obj.optLong("createdAt", 0L)
                val updatedAt = obj.optLong("updatedAt", createdAt)

                result.add(
                    VehicleReview(
                        obj.optString("userId", "guest"),
                        obj.optString("vehicleKey", ""),
                        obj.optInt("rating", 0),
                        obj.optString("comment", ""),
                        createdAt,
                        updatedAt
                    )
                )
            }

            return result
        }

        private fun saveAllReviews(
            context: Context,
            reviews: ArrayList<VehicleReview>
        ) {
            val array = JSONArray()

            for (i in 0 until reviews.size) {
                val review = reviews[i]
                val obj = JSONObject()
                obj.put("userId", review.userId)
                obj.put("vehicleKey", review.vehicleKey)
                obj.put("rating", review.rating)
                obj.put("comment", review.comment)
                obj.put("createdAt", review.createdAt)
                obj.put("updatedAt", review.updatedAt)
                array.put(obj)
            }

            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_REVIEWS, array.toString()).apply()
        }
    }
}