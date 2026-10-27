package edu.skku.map.carchecklab

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object FontHelper {

    private var loaded: Boolean = false

    private var interRegular: Typeface? = null
    private var interMedium: Typeface? = null
    private var interSemiBold: Typeface? = null
    private var interBold: Typeface? = null

    private var jakartaRegular: Typeface? = null
    private var jakartaMedium: Typeface? = null
    private var jakartaSemiBold: Typeface? = null
    private var jakartaBold: Typeface? = null
    private var jakartaExtraBold: Typeface? = null

    private fun loadFonts(context: Context) {
        if (loaded) {
            return
        }

        loaded = true

        interRegular = loadTypeface(
            context,
            "fonts/Inter_18pt-Regular.ttf",
            Typeface.NORMAL
        )

        interMedium = loadTypeface(
            context,
            "fonts/Inter_18pt-Medium.ttf",
            Typeface.NORMAL
        )

        interSemiBold = loadTypeface(
            context,
            "fonts/Inter_18pt-SemiBold.ttf",
            Typeface.BOLD
        )

        interBold = loadTypeface(
            context,
            "fonts/Inter_18pt-Bold.ttf",
            Typeface.BOLD
        )

        jakartaRegular = loadTypeface(
            context,
            "fonts/PlusJakartaSans-Regular.ttf",
            Typeface.NORMAL
        )

        jakartaMedium = loadTypeface(
            context,
            "fonts/PlusJakartaSans-Medium.ttf",
            Typeface.NORMAL
        )

        jakartaSemiBold = loadTypeface(
            context,
            "fonts/PlusJakartaSans-SemiBold.ttf",
            Typeface.BOLD
        )

        jakartaBold = loadTypeface(
            context,
            "fonts/PlusJakartaSans-Bold.ttf",
            Typeface.BOLD
        )

        jakartaExtraBold = loadTypeface(
            context,
            "fonts/PlusJakartaSans-ExtraBold.ttf",
            Typeface.BOLD
        )
    }

    private fun loadTypeface(
        context: Context,
        assetPath: String,
        fallbackStyle: Int
    ): Typeface {
        return try {
            Typeface.createFromAsset(context.assets, assetPath)
        } catch (e: Exception) {
            Typeface.create("sans-serif", fallbackStyle)
        }
    }

    fun bodyRegular(context: Context): Typeface {
        loadFonts(context)
        return interRegular ?: Typeface.create("sans-serif", Typeface.NORMAL)
    }

    fun bodyMedium(context: Context): Typeface {
        loadFonts(context)
        return interMedium ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    fun bodySemiBold(context: Context): Typeface {
        loadFonts(context)
        return interSemiBold ?: Typeface.create("sans-serif", Typeface.BOLD)
    }

    fun bodyBold(context: Context): Typeface {
        loadFonts(context)
        return interBold ?: Typeface.create("sans-serif", Typeface.BOLD)
    }

    fun dashboardRegular(context: Context): Typeface {
        loadFonts(context)
        return jakartaRegular ?: Typeface.create("sans-serif", Typeface.NORMAL)
    }

    fun dashboardMedium(context: Context): Typeface {
        loadFonts(context)
        return jakartaMedium ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    fun dashboardSemiBold(context: Context): Typeface {
        loadFonts(context)
        return jakartaSemiBold ?: Typeface.create("sans-serif", Typeface.BOLD)
    }

    fun dashboardBold(context: Context): Typeface {
        loadFonts(context)
        return jakartaBold ?: Typeface.create("sans-serif", Typeface.BOLD)
    }

    fun dashboardExtraBold(context: Context): Typeface {
        loadFonts(context)
        return jakartaExtraBold ?: Typeface.create("sans-serif", Typeface.BOLD)
    }

    /*
     * Backward-compatible names.
     * Some of the previous code used FontHelper.regular(this) or FontHelper.bold(this),
     * so these methods prevent compile errors.
     */
    fun regular(context: Context): Typeface {
        return bodyRegular(context)
    }

    fun bold(context: Context): Typeface {
        return bodyBold(context)
    }

    fun applyToViewTree(
        context: Context,
        view: View
    ) {
        loadFonts(context)

        if (view is TextView) {
            applyToTextView(context, view)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyToViewTree(context, view.getChildAt(i))
            }
        }
    }

    fun applyToTextView(
        context: Context,
        textView: TextView
    ) {
        val useDashboard = shouldUseDashboardFont(context, textView)

        val isBold =
            textView.typeface != null && textView.typeface.isBold

        if (useDashboard) {
            textView.typeface =
                if (isBold || textView.textSize >= 42f) {
                    dashboardBold(context)
                } else {
                    dashboardMedium(context)
                }
        } else {
            textView.typeface =
                if (isBold) {
                    bodySemiBold(context)
                } else {
                    bodyRegular(context)
                }
        }
    }

    private fun shouldUseDashboardFont(
        context: Context,
        textView: TextView
    ): Boolean {
        val idName =
            try {
                context.resources.getResourceEntryName(textView.id).lowercase()
            } catch (e: Exception) {
                ""
            }

        if (idName.contains("title")) return true
        if (idName.contains("score")) return true
        if (idName.contains("grade")) return true
        if (idName.contains("verdict")) return true
        if (idName.contains("dashboard")) return true
        if (idName.contains("risk")) return true
        if (idName.contains("average")) return true
        if (idName.contains("vehicle")) return true

        return false
    }
}