package edu.skku.map.carchecklab

import kotlin.math.min

data class MemeResult(
    var title: String,
    var memeTag: String,
    var formulaSummary: String,
    var comment: String,
    var dealerQuestion: String,
    var geudonssiScore: Int,
    var hachagamScore: Int,
    var practicalityScore: Int,
    var cleanDataScore: Int
)

class MemeAnalyzer {
    companion object {
        fun analyze(
            make: String,
            model: String,
            category: String,
            riskScore: Int,
            recallCount: Int,
            complaintCount: Int,
            safetyRating: String,
            priority: String
        ): MemeResult {
            val marketCar: MarketCar? = MarketData.findBestMatch(make, model)

            val hachagamScore: Int = marketCar?.hachagamScore ?: calculateFallbackHachagam(make, model, category)
            val practicalityScore: Int = marketCar?.let { MarketData.calculatePracticalityScore(it) } ?: calculateFallbackPracticality(category)
            val cleanDataScore: Int = calculateCleanDataScore(recallCount, complaintCount, safetyRating)

            val budgetDefenseScore: Int =
                calculateBudgetDefenseScore(
                    make,
                    model,
                    category,
                    riskScore,
                    hachagamScore,
                    practicalityScore,
                    cleanDataScore,
                    priority,
                    marketCar
                )

            return MemeResult(
                makeTitle(budgetDefenseScore, riskScore, priority),
                makeMemeTag(make, category, hachagamScore, practicalityScore, cleanDataScore, marketCar),
                makeFormulaSummary(riskScore, budgetDefenseScore, hachagamScore, practicalityScore, cleanDataScore, marketCar, priority),
                makeComment(budgetDefenseScore, riskScore, hachagamScore, practicalityScore, cleanDataScore, priority),
                makeDealerQuestion(recallCount, complaintCount, safetyRating, marketCar, priority),
                budgetDefenseScore,
                hachagamScore,
                practicalityScore,
                cleanDataScore
            )
        }

        private fun calculateFallbackHachagam(make: String, model: String, category: String): Int {
            var score: Int = 55
            val upperMake: String = make.uppercase()
            val upperModel: String = model.uppercase()
            val upperCategory: String = category.uppercase()

            if (
                upperMake.contains("BMW") ||
                upperMake.contains("MERCEDES") ||
                upperMake.contains("AUDI") ||
                upperMake.contains("PORSCHE") ||
                upperMake.contains("GENESIS") ||
                upperMake.contains("LEXUS")
            ) {
                score += 22
            }

            if (upperMake.contains("TESLA")) score += 18
            if (upperCategory.contains("EV")) score += 8
            if (upperCategory.contains("PREMIUM")) score += 12
            if (upperCategory.contains("PERFORMANCE")) score += 15

            if (
                upperModel.contains("911") ||
                upperModel.contains("CORVETTE") ||
                upperModel.contains("M3") ||
                upperModel.contains("CYBERTRUCK")
            ) {
                score += 10
            }

            if (upperCategory.contains("MINIVAN")) score -= 12

            return clamp(score)
        }

        private fun calculateFallbackPracticality(category: String): Int {
            var score: Int = 60
            val upperCategory: String = category.uppercase()

            if (upperCategory.contains("MINIVAN")) score += 30
            if (upperCategory.contains("SUV")) score += 18
            if (upperCategory.contains("SEDAN")) score += 10
            if (upperCategory.contains("TRUCK")) score += 8
            if (upperCategory.contains("EV")) score += 8
            if (upperCategory.contains("PERFORMANCE")) score -= 12

            return clamp(score)
        }

        private fun calculateCleanDataScore(
            recallCount: Int,
            complaintCount: Int,
            safetyRating: String
        ): Int {
            var score: Int = 100

            score -= min(35, recallCount * 7)
            score -= min(40, complaintCount / 3)

            val rating: Int = parseSafetyRating(safetyRating)

            if (rating >= 5) score += 6
            else if (rating == 4) score += 2
            else if (rating == 3) score -= 8
            else if (rating in 1..2) score -= 18
            else score -= 10

            return clamp(score)
        }

        private fun calculateBudgetDefenseScore(
            make: String,
            model: String,
            category: String,
            riskScore: Int,
            hachagamScore: Int,
            practicalityScore: Int,
            cleanDataScore: Int,
            priority: String,
            marketCar: MarketCar?
        ): Int {
            val marketDecisionScore: Int =
                if (marketCar != null) {
                    MarketData.calculateDecisionScore(marketCar, priority)
                } else {
                    (riskScore + practicalityScore + cleanDataScore) / 3
                }

            val marketWalletScore: Int =
                if (marketCar != null) marketCar.walletScore else 65

            val priorityScore: Int =
                if (marketCar != null) {
                    MarketData.getPriorityScore(marketCar, priority)
                } else {
                    riskScore
                }

            val comparableCandidates: ArrayList<MarketCar> =
                MarketData.getComparableCandidates(make, model, category, priority, 8)

            var bestAlternativeDecision: Int = marketDecisionScore

            for (i in 0 until comparableCandidates.size) {
                val candidate: MarketCar = comparableCandidates[i]
                val decision: Int = MarketData.calculateDecisionScore(candidate, priority)

                if (decision > bestAlternativeDecision) {
                    bestAlternativeDecision = decision
                }
            }

            val alternativePressure: Int =
                if (bestAlternativeDecision > marketDecisionScore) {
                    min(18, (bestAlternativeDecision - marketDecisionScore) / 2)
                } else {
                    0
                }

            var score: Int =
                (riskScore * 22 +
                        marketWalletScore * 18 +
                        cleanDataScore * 18 +
                        practicalityScore * 15 +
                        priorityScore * 17 +
                        hachagamScore * 10) / 100

            score -= alternativePressure

            if (priority == "Wallet Survivor" && marketWalletScore < 55) score -= 8
            if (priority == "Hachagam Hunter" && hachagamScore >= 88) score += 5
            if (priority == "Weather Safety First" && marketCar != null && marketCar.badWeatherScore >= 88) score += 5

            return clamp(score)
        }

        private fun makeTitle(score: Int, riskScore: Int, priority: String): String {
            if (score >= 82 && riskScore >= 75) return "Defendable Purchase"
            if (score >= 68) return "Conditional Approval"
            if (score >= 52) return "Negotiation Required"
            if (priority == "Hachagam Hunter" && riskScore >= 60) return "Attractive but Needs Evidence"
            return "Budget Defense Warning"
        }

        private fun makeMemeTag(
            make: String,
            category: String,
            hachagamScore: Int,
            practicalityScore: Int,
            cleanDataScore: Int,
            marketCar: MarketCar?
        ): String {
            val upperMake: String = make.uppercase()
            val upperCategory: String = category.uppercase()

            if (upperMake.contains("TESLA")) return "Tesla Fan Audit"
            if (upperMake.contains("HYUNDAI") || upperMake.contains("KIA") || upperMake.contains("GENESIS")) return "K-Brand Evidence"
            if (upperMake.contains("BMW") || upperMake.contains("MERCEDES") || upperMake.contains("AUDI") || upperMake.contains("PORSCHE")) return "Badge Invoice Warning"
            if (upperMake.contains("TOYOTA") || upperMake.contains("LEXUS")) return "Reliability Club Exhibit"
            if (upperCategory.contains("MINIVAN")) return "Adult Functionality"
            if (hachagamScore >= 90) return "Style Risk"
            if (practicalityScore >= 85 && hachagamScore < 65) return "Boring but Correct"
            if (cleanDataScore < 55) return "Paperwork Caution"
            if (marketCar != null && marketCar.badWeatherScore >= 90) return "Rainy Day Survivor"

            return "Car Court Evidence"
        }

        private fun makeComment(
            score: Int,
            riskScore: Int,
            hachagamScore: Int,
            practicalityScore: Int,
            cleanDataScore: Int,
            priority: String
        ): String {
            if (score >= 82) {
                return "The data can defend this car. Compare records, but the purchase is not absurd."
            }

            if (score >= 68) {
                return "This car is not a risk case, but it still needs inspection records and negotiation."
            }

            if (cleanDataScore < 55) {
                return "The paperwork is weak. Ask for recall completion, maintenance records, and accident history."
            }

            if (priority == "Wallet Survivor" && score < 65) {
                return "Your profile says Wallet Survivor, but this car may stretch the budget logic."
            }

            if (priority == "Hachagam Hunter" && hachagamScore >= 88 && riskScore >= 60) {
                return "The visual appeal is strong, so the data should supervise the decision."
            }

            if (practicalityScore >= 85 && score >= 60) {
                return "This is practical enough to be boring. Boring often survives ownership better than excitement."
            }

            return "Budget Defense is weak. Compare alternatives before committing to payment."
        }

        private fun makeDealerQuestion(
            recallCount: Int,
            complaintCount: Int,
            safetyRating: String,
            marketCar: MarketCar?,
            priority: String
        ): String {
            if (recallCount > 0) return "Can you show proof that every open recall has been completed?"
            if (complaintCount >= 30) return "Can you explain the NHTSA complaint history and show maintenance records?"
            if (parseSafetyRating(safetyRating) == 0) return "Why is the safety rating missing or unavailable?"
            if (marketCar != null && marketCar.walletScore < 55) return "Can you provide recent fuel, tire, brake, and maintenance cost records?"
            if (marketCar != null && marketCar.badWeatherScore < 60 && priority == "Weather Safety First") return "How does this car behave in rain or bad road conditions?"
            if (marketCar != null && marketCar.hachagamScore >= 90) return "The car looks attractive, but can you show the service history?"

            return "Can you show the maintenance history, accident record, title status, and inspection result?"
        }

        private fun makeFormulaSummary(
            riskScore: Int,
            budgetDefenseScore: Int,
            hachagamScore: Int,
            practicalityScore: Int,
            cleanDataScore: Int,
            marketCar: MarketCar?,
            priority: String
        ): String {
            val marketText: String =
                if (marketCar != null) {
                    "Dataset match: " +
                            marketCar.year.toString() + " " +
                            marketCar.make + " " +
                            marketCar.model +
                            " / price $" + marketCar.estimatedPrice.toString()
                } else {
                    "Dataset match: not found, fallback formula used"
                }

            return marketText +
                    "\nFormula: Budget Defense = risk data + wallet score + clean data + practicality + user priority + Hachagam - stronger same-budget alternative pressure." +
                    "\nScores: Risk " + riskScore.toString() +
                    ", Budget Defense " + budgetDefenseScore.toString() +
                    ", Hachagam " + hachagamScore.toString() +
                    ", Practicality " + practicalityScore.toString() +
                    ", Clean Data " + cleanDataScore.toString() +
                    ", Priority " + priority + "."
        }

        private fun parseSafetyRating(safetyRating: String): Int {
            val clean: String = safetyRating.trim()

            if (clean.contains("5")) return 5
            if (clean.contains("4")) return 4
            if (clean.contains("3")) return 3
            if (clean.contains("2")) return 2
            if (clean.contains("1")) return 1

            return 0
        }

        private fun clamp(value: Int): Int {
            if (value < 0) return 0
            if (value > 100) return 100
            return value
        }
    }
}