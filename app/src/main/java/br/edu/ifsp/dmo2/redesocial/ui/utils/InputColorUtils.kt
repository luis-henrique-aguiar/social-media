package br.edu.ifsp.dmo2.redesocial.ui.utils

import android.content.res.ColorStateList
import android.graphics.Color
import com.google.android.material.textfield.TextInputLayout

object InputColorUtils {
    fun applyInputColors(vararg inputs: TextInputLayout) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_hovered),
            intArrayOf()
        )

        val colors = intArrayOf(
            Color.WHITE,
            Color.GRAY,
            Color.WHITE,
            Color.WHITE
        )

        val colorStateList = ColorStateList(states, colors)

        inputs.forEach { input ->
            input.setBoxStrokeColorStateList(colorStateList)
        }
    }
}