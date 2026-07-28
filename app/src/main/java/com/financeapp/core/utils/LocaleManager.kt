package com.financeapp.core.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import com.financeapp.core.domain.model.AppLanguage
import java.util.Locale

object LocaleManager {
    fun deviceLanguage(): AppLanguage = when (Locale.getDefault().language) {
        "ru" -> AppLanguage.RU
        else -> AppLanguage.EN
    }

    fun contextWithLocale(context: Context, language: AppLanguage): Context {
        val targetLocale = Locale(language.tag)
        Locale.setDefault(targetLocale)

        val config = Configuration(context.resources.configuration).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                setLocales(android.os.LocaleList(targetLocale))
            } else {
                @Suppress("DEPRECATION")
                setLocale(targetLocale)
            }
        }
        val localeCtx = context.createConfigurationContext(config)
        return LocaleContextWrapper(context, localeCtx)
    }
}

private class LocaleContextWrapper(base: Context, private val localeContext: Context) : ContextWrapper(base) {
    override fun getResources() = localeContext.resources
    override fun getAssets() = localeContext.assets
    override fun getContentResolver() = localeContext.contentResolver
}
