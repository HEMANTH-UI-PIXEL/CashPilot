package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Currency
import java.security.MessageDigest

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("moneypilot_prefs", Context.MODE_PRIVATE)

    var currencyCode: String
        get() = prefs.getString(KEY_CURRENCY_CODE, "INR") ?: "INR"
        set(value) = prefs.edit().putString(KEY_CURRENCY_CODE, value).apply()

    var isPinLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_ENABLED, value).apply()

    private var pinHash: String
        get() = prefs.getString(KEY_PIN_HASH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    fun getSelectedCurrency(): Currency {
        return Currency.findByCode(currencyCode)
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        pinHash = hash
        isPinLockEnabled = true
    }

    fun verifyPin(pin: String): Boolean {
        if (!isPinLockEnabled) return true
        val inputHash = hashPin(pin)
        return inputHash == pinHash
    }

    fun disablePin(currentPin: String): Boolean {
        if (verifyPin(currentPin)) {
            isPinLockEnabled = false
            pinHash = ""
            return true
        }
        return false
    }

    private fun hashPin(pin: String): String {
        val salt = "MoneyPilot_Salt_2026_Secure"
        val bytes = MessageDigest.getInstance("SHA-256").digest((salt + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
    }
}
