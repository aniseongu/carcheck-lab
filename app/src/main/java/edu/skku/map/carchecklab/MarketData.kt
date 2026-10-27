package edu.skku.map.carchecklab

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

data class MarketCar(
    var year: Int,
    var make: String,
    var model: String,
    var category: String,
    var estimatedPrice: Int,
    var annualFuelCost: Int,
    var safetyScore: Int,
    var walletScore: Int,
    var hachagamScore: Int,
    var techScore: Int,
    var familyScore: Int,
    var badWeatherScore: Int
)

data class MarketRecommendation(
    var title: String,
    var firstCar: String,
    var secondCar: String,
    var thirdCar: String,
    var reason: String
)

class MarketData {
    companion object {
        private val cars: ArrayList<MarketCar> = ArrayList<MarketCar>()
        private var initialized: Boolean = false

        fun init(context: Context) {
            if (initialized && cars.size > 0) {
                return
            }

            cars.clear()

            try {
                val inputStream = context.assets.open("market_cars.txt")
                val reader = BufferedReader(InputStreamReader(inputStream))

                var line: String? = reader.readLine()

                while (line != null) {
                    val cleanLine: String = line.trim()

                    if (
                        cleanLine.isNotEmpty() &&
                        !cleanLine.startsWith("#") &&
                        !cleanLine.lowercase().startsWith("year,")
                    ) {
                        val car: MarketCar? = parseMarketCar(cleanLine)

                        if (car != null) {
                            cars.add(car)
                        }
                    }

                    line = reader.readLine()
                }

                reader.close()
                initialized = true

            } catch (e: Exception) {
                initialized = true
                loadEmergencyFallbackCars()
            }
        }

        private fun parseMarketCar(line: String): MarketCar? {
            try {
                val parts: List<String> = line.split(",")

                if (parts.size < 12) {
                    return null
                }

                return MarketCar(
                    parts[0].trim().toInt(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim().toInt(),
                    parts[5].trim().toInt(),
                    clamp(parts[6].trim().toInt()),
                    clamp(parts[7].trim().toInt()),
                    clamp(parts[8].trim().toInt()),
                    clamp(parts[9].trim().toInt()),
                    clamp(parts[10].trim().toInt()),
                    clamp(parts[11].trim().toInt())
                )

            } catch (e: Exception) {
                return null
            }
        }

        private fun loadEmergencyFallbackCars() {
            cars.add(MarketCar(2022, "TESLA", "MODEL 3", "EV", 31000, 560, 90, 94, 84, 95, 72, 80))
            cars.add(MarketCar(2022, "KIA", "EV6", "EV", 35000, 650, 88, 92, 89, 94, 80, 86))
            cars.add(MarketCar(2022, "TOYOTA", "CAMRY", "SEDAN", 26000, 1300, 89, 82, 59, 70, 80, 76))
            cars.add(MarketCar(2022, "BMW", "330I", "GERMAN PREMIUM SEDAN", 39000, 1800, 85, 66, 90, 88, 70, 76))
        }

        fun getCars(): ArrayList<MarketCar> {
            return cars
        }

        fun findBestMatch(make: String, model: String): MarketCar? {
            val targetMake: String = normalize(make)
            val targetModel: String = normalize(model)

            var bestCar: MarketCar? = null
            var bestScore: Int = 0

            for (i in 0 until cars.size) {
                val car: MarketCar = cars[i]
                val carMake: String = normalize(car.make)
                val carModel: String = normalize(car.model)

                var score: Int = 0

                if (carMake == targetMake) {
                    score += 80
                }

                if (carModel == targetModel) {
                    score += 120
                } else if (carModel.contains(targetModel) || targetModel.contains(carModel)) {
                    score += 70
                }

                if (score > bestScore) {
                    bestScore = score
                    bestCar = car
                }
            }

            if (bestScore < 80) {
                return null
            }

            return bestCar
        }

        fun recommendAlternative(
            make: String,
            model: String,
            category: String,
            priority: String
        ): MarketRecommendation {
            val currentCar: MarketCar? = findBestMatch(make, model)
            val baseCar: MarketCar = currentCar ?: makeFallbackCar(make, model, category)

            val candidates: ArrayList<MarketCar> = getCandidatesInPriceRange(
                baseCar,
                priority,
                0.80,
                1.35,
                true,
                ""
            )

            val finalCandidates: ArrayList<MarketCar> =
                if (candidates.size >= 3) {
                    candidates
                } else {
                    getCandidatesInPriceRange(baseCar, priority, 0.60, 1.80, false, "")
                }

            finalCandidates.sortWith { a, b ->
                calculateAlternativeScore(b, baseCar, priority)
                    .compareTo(calculateAlternativeScore(a, baseCar, priority))
            }

            val first: String =
                if (finalCandidates.size > 0) makeRecommendationLine(1, finalCandidates[0], baseCar, priority)
                else "1. No strong alternative found"

            val second: String =
                if (finalCandidates.size > 1) makeRecommendationLine(2, finalCandidates[1], baseCar, priority)
                else "2. No second candidate found"

            val third: String =
                if (finalCandidates.size > 2) makeRecommendationLine(3, finalCandidates[2], baseCar, priority)
                else "3. No third candidate found"

            return MarketRecommendation(
                "Top 3 Budget Alternatives",
                first,
                second,
                third,
                "Alternatives are selected from market_cars.txt using price similarity, objective score, profile preference, and improvement over the selected vehicle."
            )
        }

        fun getComparableCandidates(
            make: String,
            model: String,
            category: String,
            priority: String,
            limit: Int
        ): ArrayList<MarketCar> {
            val currentCar: MarketCar? = findBestMatch(make, model)
            val baseCar: MarketCar = currentCar ?: makeFallbackCar(make, model, category)

            val candidates: ArrayList<MarketCar> =
                getCandidatesInPriceRange(baseCar, priority, 0.80, 1.30, true, "")

            candidates.sortWith { a, b ->
                calculateAlternativeScore(b, baseCar, priority)
                    .compareTo(calculateAlternativeScore(a, baseCar, priority))
            }

            val result: ArrayList<MarketCar> = ArrayList<MarketCar>()

            for (i in 0 until candidates.size) {
                if (result.size < limit) {
                    result.add(candidates[i])
                }
            }

            return result
        }

        fun getBlackHoleCandidates(
            make: String,
            model: String,
            category: String,
            priority: String,
            mode: String,
            maxCount: Int
        ): ArrayList<MarketCar> {
            val currentCar: MarketCar? = findBestMatch(make, model)
            val baseCar: MarketCar = currentCar ?: makeFallbackCar(make, model, category)

            val available: ArrayList<MarketCar> = ArrayList<MarketCar>()

            for (i in 0 until cars.size) {
                val car: MarketCar = cars[i]

                if (
                    !isSameVehicleFamily(baseCar, car) &&
                    car.estimatedPrice > baseCar.estimatedPrice &&
                    isAllowedByMode(car, mode)
                ) {
                    available.add(car)
                }
            }

            val result: ArrayList<MarketCar> = ArrayList<MarketCar>()
            var lastPrice: Int = baseCar.estimatedPrice
            var stage: Int = 0

            while (result.size < maxCount && stage < 30) {
                val next: MarketCar? =
                    findNextBlackHoleCandidate(
                        available,
                        result,
                        baseCar,
                        lastPrice,
                        priority,
                        mode,
                        stage
                    )

                if (next == null) {
                    break
                }

                result.add(next)
                lastPrice = next.estimatedPrice
                stage += 1
            }

            return result
        }

        private fun findNextBlackHoleCandidate(
            available: ArrayList<MarketCar>,
            selected: ArrayList<MarketCar>,
            baseCar: MarketCar,
            lastPrice: Int,
            priority: String,
            mode: String,
            stage: Int
        ): MarketCar? {
            val minPrice: Int = lastPrice + getMinimumPriceJump(lastPrice, stage)
            val primaryMax: Int = lastPrice + getPrimaryPriceWindow(lastPrice, stage)
            val secondaryMax: Int = lastPrice + getSecondaryPriceWindow(lastPrice, stage)

            val primary: ArrayList<MarketCar> =
                filterBlackHoleCandidates(available, selected, baseCar, priority, minPrice, primaryMax, true)

            if (primary.size > 0) {
                return pickBlackHoleCandidate(primary, baseCar, lastPrice, priority, mode)
            }

            val secondary: ArrayList<MarketCar> =
                filterBlackHoleCandidates(available, selected, baseCar, priority, minPrice, secondaryMax, true)

            if (secondary.size > 0) {
                return pickBlackHoleCandidate(secondary, baseCar, lastPrice, priority, mode)
            }

            val loose: ArrayList<MarketCar> =
                filterBlackHoleCandidates(available, selected, baseCar, priority, minPrice, 999999, false)

            if (loose.size > 0) {
                return pickBlackHoleCandidate(loose, baseCar, lastPrice, priority, mode)
            }

            return null
        }

        private fun filterBlackHoleCandidates(
            available: ArrayList<MarketCar>,
            selected: ArrayList<MarketCar>,
            baseCar: MarketCar,
            priority: String,
            minPrice: Int,
            maxPrice: Int,
            requireBetter: Boolean
        ): ArrayList<MarketCar> {
            val result: ArrayList<MarketCar> = ArrayList<MarketCar>()

            for (i in 0 until available.size) {
                val car: MarketCar = available[i]

                if (
                    car.estimatedPrice >= minPrice &&
                    car.estimatedPrice <= maxPrice &&
                    !containsSameCar(selected, car)
                ) {
                    if (!requireBetter || isBetterThan(car, baseCar, priority)) {
                        result.add(car)
                    }
                }
            }

            return result
        }

        private fun pickBlackHoleCandidate(
            candidates: ArrayList<MarketCar>,
            baseCar: MarketCar,
            lastPrice: Int,
            priority: String,
            mode: String
        ): MarketCar {
            candidates.sortWith { a, b ->
                calculateBlackHoleScore(b, baseCar, lastPrice, priority, mode)
                    .compareTo(calculateBlackHoleScore(a, baseCar, lastPrice, priority, mode))
            }

            return candidates[0]
        }

        private fun calculateBlackHoleScore(
            candidate: MarketCar,
            baseCar: MarketCar,
            lastPrice: Int,
            priority: String,
            mode: String
        ): Int {
            val decisionScore: Int = calculateDecisionScore(candidate, priority)
            val priorityScore: Int = getPriorityScore(candidate, priority)
            val escalationScore: Int = calculateEscalationScore(candidate.estimatedPrice, lastPrice)
            val modeScore: Int = getModePriority(candidate, mode)

            var betterBonus: Int = 0

            if (isBetterThan(candidate, baseCar, priority)) {
                betterBonus = 20
            }

            return (decisionScore * 30) / 100 +
                    (priorityScore * 20) / 100 +
                    (escalationScore * 30) / 100 +
                    (modeScore * 20) / 100 +
                    betterBonus
        }

        private fun calculateAlternativeScore(
            candidate: MarketCar,
            baseCar: MarketCar,
            priority: String
        ): Int {
            val decisionScore: Int = calculateDecisionScore(candidate, priority)
            val objectiveScore: Int = calculateObjectiveScore(candidate)
            val priorityScore: Int = getPriorityScore(candidate, priority)
            val priceFitScore: Int =
                calculatePriceSimilarityScore(candidate.estimatedPrice, baseCar.estimatedPrice)
            val improvementScore: Int =
                calculateImprovementScore(candidate, baseCar, priority)

            return (decisionScore * 30) / 100 +
                    (objectiveScore * 20) / 100 +
                    (priorityScore * 25) / 100 +
                    (priceFitScore * 15) / 100 +
                    (improvementScore * 10) / 100
        }

        fun calculateDecisionScore(car: MarketCar, priority: String): Int {
            val objectiveScore: Int = calculateObjectiveScore(car)
            val priorityScore: Int = getPriorityScore(car, priority)
            val valueScore: Int = calculateValueScore(car)

            return clamp(
                (objectiveScore * 35 +
                        priorityScore * 30 +
                        valueScore * 15 +
                        car.hachagamScore * 10 +
                        car.techScore * 10) / 100
            )
        }

        fun calculateObjectiveScore(car: MarketCar): Int {
            return clamp(
                (car.safetyScore * 30 +
                        car.walletScore * 25 +
                        car.badWeatherScore * 15 +
                        car.familyScore * 15 +
                        car.techScore * 10 +
                        car.hachagamScore * 5) / 100
            )
        }

        fun calculatePracticalityScore(car: MarketCar): Int {
            return clamp(
                (car.familyScore * 35 +
                        car.walletScore * 25 +
                        car.badWeatherScore * 20 +
                        car.safetyScore * 15 +
                        car.techScore * 5) / 100
            )
        }

        fun getPriorityScore(car: MarketCar, priority: String): Int {
            if (priority == "Safety Nerd") return car.safetyScore
            if (priority == "Wallet Survivor") return car.walletScore
            if (priority == "Hachagam Hunter") return car.hachagamScore
            if (priority == "Weather Safety First") return car.badWeatherScore
            if (priority == "Bad Weather Coward") return car.badWeatherScore
            if (priority == "Tech Bro") return car.techScore
            if (priority == "Family Commander") return car.familyScore
            return calculateObjectiveScore(car)
        }

        fun isBetterThan(
            candidate: MarketCar,
            baseCar: MarketCar,
            priority: String
        ): Boolean {
            val candidateDecision: Int = calculateDecisionScore(candidate, priority)
            val baseDecision: Int = calculateDecisionScore(baseCar, priority)

            val candidatePriority: Int = getPriorityScore(candidate, priority)
            val basePriority: Int = getPriorityScore(baseCar, priority)

            val candidateObjective: Int = calculateObjectiveScore(candidate)
            val baseObjective: Int = calculateObjectiveScore(baseCar)

            return candidateDecision >= baseDecision + 4 ||
                    candidatePriority >= basePriority + 7 && candidateObjective >= baseObjective - 5 ||
                    candidate.safetyScore >= baseCar.safetyScore + 6 && candidate.walletScore >= baseCar.walletScore - 7 ||
                    candidate.walletScore >= baseCar.walletScore + 10 && candidate.safetyScore >= baseCar.safetyScore - 6 ||
                    candidate.hachagamScore >= baseCar.hachagamScore + 12 && candidateDecision >= baseDecision - 2 && candidate.walletScore >= 40 ||
                    priority == "Weather Safety First" && candidate.badWeatherScore >= baseCar.badWeatherScore + 8
        }

        fun makeImprovementSummary(
            candidate: MarketCar,
            baseCar: MarketCar,
            priority: String
        ): String {
            val improvements: ArrayList<String> = ArrayList<String>()

            val candidateDecision: Int = calculateDecisionScore(candidate, priority)
            val baseDecision: Int = calculateDecisionScore(baseCar, priority)

            val candidatePriority: Int = getPriorityScore(candidate, priority)
            val basePriority: Int = getPriorityScore(baseCar, priority)

            if (candidateDecision > baseDecision) improvements.add("overall +" + (candidateDecision - baseDecision).toString())
            if (candidatePriority > basePriority) improvements.add("profile +" + (candidatePriority - basePriority).toString())
            if (candidate.safetyScore > baseCar.safetyScore) improvements.add("safety +" + (candidate.safetyScore - baseCar.safetyScore).toString())
            if (candidate.walletScore > baseCar.walletScore) improvements.add("wallet +" + (candidate.walletScore - baseCar.walletScore).toString())
            if (candidate.hachagamScore > baseCar.hachagamScore) improvements.add("Hachagam +" + (candidate.hachagamScore - baseCar.hachagamScore).toString())
            if (candidate.badWeatherScore > baseCar.badWeatherScore) improvements.add("bad-weather +" + (candidate.badWeatherScore - baseCar.badWeatherScore).toString())

            if (improvements.size == 0) {
                return "Condition logic: This candidate barely survived the data filter."
            }

            val builder: StringBuilder = StringBuilder()
            builder.append("Condition logic: ")

            for (i in 0 until improvements.size) {
                builder.append(improvements[i])

                if (i != improvements.size - 1) {
                    builder.append(" / ")
                }
            }

            return builder.toString()
        }

        private fun getCandidatesInPriceRange(
            baseCar: MarketCar,
            priority: String,
            minMultiplier: Double,
            maxMultiplier: Double,
            requireBetter: Boolean,
            mode: String
        ): ArrayList<MarketCar> {
            val result: ArrayList<MarketCar> = ArrayList<MarketCar>()

            val minPrice: Int = (baseCar.estimatedPrice * minMultiplier).toInt()
            val maxPrice: Int = (baseCar.estimatedPrice * maxMultiplier).toInt()

            for (i in 0 until cars.size) {
                val car: MarketCar = cars[i]

                if (
                    !isSameVehicleFamily(baseCar, car) &&
                    car.estimatedPrice >= minPrice &&
                    car.estimatedPrice <= maxPrice &&
                    isAllowedByMode(car, mode)
                ) {
                    if (!requireBetter || isBetterThan(car, baseCar, priority)) {
                        result.add(car)
                    }
                }
            }

            return result
        }

        private fun calculateImprovementScore(candidate: MarketCar, baseCar: MarketCar, priority: String): Int {
            val decisionDiff: Int = calculateDecisionScore(candidate, priority) - calculateDecisionScore(baseCar, priority)
            val priorityDiff: Int = getPriorityScore(candidate, priority) - getPriorityScore(baseCar, priority)
            val safetyDiff: Int = candidate.safetyScore - baseCar.safetyScore
            val walletDiff: Int = candidate.walletScore - baseCar.walletScore

            return clamp(50 + decisionDiff * 2 + priorityDiff + safetyDiff + walletDiff)
        }

        private fun calculateValueScore(car: MarketCar): Int {
            var priceScore: Int =
                if (car.estimatedPrice <= 20000) 92
                else if (car.estimatedPrice <= 30000) 84
                else if (car.estimatedPrice <= 45000) 74
                else if (car.estimatedPrice <= 65000) 60
                else if (car.estimatedPrice <= 90000) 45
                else 25

            if (car.annualFuelCost <= 900) {
                priceScore += 8
            } else if (car.annualFuelCost >= 2500) {
                priceScore -= 10
            }

            return clamp(priceScore)
        }

        private fun calculatePriceSimilarityScore(candidatePrice: Int, basePrice: Int): Int {
            if (basePrice <= 0) return 60

            val gap: Int = abs(candidatePrice - basePrice)
            var score: Int = 100 - ((gap * 100) / basePrice)

            if (candidatePrice > basePrice) {
                score -= 5
            }

            return clamp(score)
        }

        private fun calculateEscalationScore(candidatePrice: Int, lastPrice: Int): Int {
            val gap: Int = candidatePrice - lastPrice

            if (gap <= 0) {
                return 0
            }

            val idealGap: Int = maxOf(3000, lastPrice / 5)
            val distance: Int = abs(gap - idealGap)

            return clamp(100 - ((distance * 100) / idealGap))
        }

        private fun getMinimumPriceJump(lastPrice: Int, stage: Int): Int {
            if (stage <= 1) return maxOf(1000, lastPrice / 22)
            if (stage <= 3) return maxOf(2000, lastPrice / 16)
            return maxOf(3000, lastPrice / 12)
        }

        private fun getPrimaryPriceWindow(lastPrice: Int, stage: Int): Int {
            if (stage <= 1) return maxOf(6500, lastPrice / 4)
            if (stage <= 3) return maxOf(10000, lastPrice / 3)
            return maxOf(14000, lastPrice / 2)
        }

        private fun getSecondaryPriceWindow(lastPrice: Int, stage: Int): Int {
            if (stage <= 1) return maxOf(13000, lastPrice / 2)
            if (stage <= 3) return maxOf(20000, (lastPrice * 3) / 4)
            return maxOf(30000, lastPrice)
        }

        private fun makeRecommendationLine(rank: Int, car: MarketCar, baseCar: MarketCar, priority: String): String {
            val decision: Int = calculateDecisionScore(car, priority)
            val baseDecision: Int = calculateDecisionScore(baseCar, priority)
            val priceDiff: Int = car.estimatedPrice - baseCar.estimatedPrice

            val priceText: String =
                if (priceDiff >= 0) "+$" + priceDiff.toString()
                else "-$" + abs(priceDiff).toString()

            return rank.toString() + ". " +
                    car.year.toString() + " " +
                    car.make + " " +
                    car.model +
                    " / $" + car.estimatedPrice.toString() +
                    " / decision " + decision.toString() +
                    " (" + priceText + ", current " + baseDecision.toString() + ")"
        }

        private fun makeFallbackCar(make: String, model: String, category: String): MarketCar {
            return MarketCar(
                2022,
                make,
                model,
                category,
                getDefaultPrice(category),
                1600,
                82,
                70,
                65,
                70,
                72,
                74
            )
        }

        fun getDefaultPrice(category: String): Int {
            val upper: String = category.uppercase()

            if (upper.contains("EV")) return 36000
            if (upper.contains("PREMIUM")) return 45000
            if (upper.contains("SUV")) return 33000
            if (upper.contains("SEDAN")) return 26000
            if (upper.contains("MINIVAN")) return 36000
            if (upper.contains("TRUCK")) return 42000

            return 30000
        }

        private fun containsSameCar(list: ArrayList<MarketCar>, target: MarketCar): Boolean {
            for (i in 0 until list.size) {
                if (isSameVehicleFamily(list[i], target)) {
                    return true
                }
            }

            return false
        }

        private fun isSameVehicleFamily(a: MarketCar, b: MarketCar): Boolean {
            val makeA: String = normalize(a.make)
            val makeB: String = normalize(b.make)
            val modelA: String = normalize(a.model)
            val modelB: String = normalize(b.model)

            if (makeA == makeB && modelA == modelB) return true

            if (makeA == makeB) {
                if (modelA.contains(modelB) || modelB.contains(modelA)) {
                    return true
                }
            }

            return false
        }

        private fun isAllowedByMode(car: MarketCar, mode: String): Boolean {
            if (mode == "Tesla Fan Mode") return isTeslaCar(car)
            if (mode == "K-Brand Pride Mode") return isKoreanBrand(car.make)
            if (mode == "German Badge Mode") return isGermanBadge(car.make)
            if (mode == "Toyota Reliability Club") return isToyotaClub(car.make)
            return true
        }

        private fun getModePriority(car: MarketCar, mode: String): Int {
            if (mode == "Tesla Fan Mode") return if (isTeslaCar(car)) 100 else 0
            if (mode == "K-Brand Pride Mode") return if (isKoreanBrand(car.make)) 100 else 0
            if (mode == "German Badge Mode") return if (isGermanBadge(car.make)) 100 else 0
            if (mode == "Toyota Reliability Club") return if (isToyotaClub(car.make)) 100 else 0

            return calculateDecisionScore(car, "Safety Nerd")
        }

        private fun isTeslaCar(car: MarketCar): Boolean {
            val makeText: String = car.make.uppercase()
            val modelText: String = car.model.uppercase()

            return makeText.contains("TESLA") ||
                    modelText.contains("MODEL 3") ||
                    modelText.contains("MODEL Y") ||
                    modelText.contains("MODEL S") ||
                    modelText.contains("MODEL X") ||
                    modelText.contains("CYBERTRUCK")
        }

        private fun isKoreanBrand(make: String): Boolean {
            val upper: String = make.uppercase()
            return upper.contains("HYUNDAI") ||
                    upper.contains("KIA") ||
                    upper.contains("GENESIS")
        }

        private fun isGermanBadge(make: String): Boolean {
            val upper: String = make.uppercase()
            return upper.contains("BMW") ||
                    upper.contains("MERCEDES") ||
                    upper.contains("AUDI") ||
                    upper.contains("PORSCHE") ||
                    upper.contains("VOLKSWAGEN")
        }

        private fun isToyotaClub(make: String): Boolean {
            val upper: String = make.uppercase()
            return upper.contains("TOYOTA") || upper.contains("LEXUS")
        }

        private fun normalize(text: String): String {
            return text
                .uppercase()
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "")
                .replace("/", "")
                .replace(".", "")
        }

        private fun clamp(value: Int): Int {
            if (value < 0) return 0
            if (value > 100) return 100
            return value
        }
    }
}