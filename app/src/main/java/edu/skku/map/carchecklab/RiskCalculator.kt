package edu.skku.map.carchecklab

class RiskCalculator {
    companion object {
        fun calculateScore(
            recallCount: Int,
            complaintCount: Int,
            safetyRating: String
        ): Int {
            return calculateScore(recallCount, complaintCount, safetyRating, null)
        }

        fun calculateScore(
            recallCount: Int,
            complaintCount: Int,
            safetyRating: String,
            localSafetyScore: Int?
        ): Int {
            var score = 76

            score -= calculateRecallPenalty(recallCount)
            score -= calculateComplaintPenalty(complaintCount)

            val nhtsaRating = parseSafetyRating(safetyRating)

            if (nhtsaRating > 0) {
                score += calculateNhtsaSafetyAdjustment(nhtsaRating)
            } else if (localSafetyScore != null) {
                score += calculateLocalSafetyAdjustment(localSafetyScore)
            } else {
                score -= 4
            }

            if (score < 18) {
                score = 18
            }

            if (score > 98) {
                score = 98
            }

            return score
        }

        fun getGrade(score: Int): String {
            if (score >= 80) {
                return "LOW RISK"
            }

            if (score >= 60) {
                return "MEDIUM RISK"
            }

            return "HIGH RISK"
        }

        fun getComment(
            score: Int,
            recallCount: Int,
            complaintCount: Int,
            safetyRating: String
        ): String {
            if (score >= 80) {
                return "This vehicle has relatively strong safety signals. Still check service records before purchase."
            }

            if (score >= 60) {
                return "This vehicle is not an automatic rejection, but recall completion and complaint patterns should be checked."
            }

            if (recallCount >= 5 && complaintCount >= 150) {
                return "This vehicle has strong caution signals. Ask for recall completion, inspection result, and maintenance records."
            }

            if (parseSafetyRating(safetyRating) == 0) {
                return "NHTSA safety rating is unavailable for this exact vehicle, so the app uses recall, complaint, and local baseline data."
            }

            return "This vehicle needs careful review. Compare alternatives before deciding."
        }

        fun makeSafetyDisplay(
            safetyRating: String,
            localSafetyScore: Int?
        ): String {
            val rating = parseSafetyRating(safetyRating)

            if (rating > 0) {
                return "NHTSA Safety Rating: " + rating.toString() + " / 5"
            }

            if (localSafetyScore != null) {
                return "NHTSA Safety Rating: Not available\nLocal Safety Baseline: " +
                        localSafetyScore.toString() + " / 100"
            }

            return "NHTSA Safety Rating: Not available"
        }

        private fun calculateRecallPenalty(recallCount: Int): Int {
            if (recallCount <= 0) return 0
            if (recallCount == 1) return 5
            if (recallCount == 2) return 9
            if (recallCount == 3) return 13
            if (recallCount == 4) return 17
            if (recallCount <= 7) return 22

            return 28
        }

        private fun calculateComplaintPenalty(complaintCount: Int): Int {
            if (complaintCount <= 0) return 0
            if (complaintCount <= 10) return 3
            if (complaintCount <= 30) return 7
            if (complaintCount <= 75) return 12
            if (complaintCount <= 150) return 18
            if (complaintCount <= 300) return 24
            if (complaintCount <= 600) return 30

            return 36
        }

        private fun calculateNhtsaSafetyAdjustment(rating: Int): Int {
            if (rating >= 5) return 12
            if (rating == 4) return 7
            if (rating == 3) return 0
            if (rating == 2) return -8
            if (rating == 1) return -14

            return 0
        }

        private fun calculateLocalSafetyAdjustment(localSafetyScore: Int): Int {
            if (localSafetyScore >= 90) return 10
            if (localSafetyScore >= 80) return 6
            if (localSafetyScore >= 70) return 2
            if (localSafetyScore >= 60) return -3

            return -8
        }

        private fun parseSafetyRating(safetyRating: String): Int {
            val clean = safetyRating.trim()

            if (clean.contains("5")) return 5
            if (clean.contains("4")) return 4
            if (clean.contains("3")) return 3
            if (clean.contains("2")) return 2
            if (clean.contains("1")) return 1

            return 0
        }
    }
}