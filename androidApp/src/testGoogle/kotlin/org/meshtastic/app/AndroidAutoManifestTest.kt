/*
 * Copyright (c) 2026 Meshtastic LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.meshtastic.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins that the merged google-flavor manifest declares Android Auto support. ForkedUpTastic ships outside Google Play,
 * so it restores the templated car app upstream had to remove for Play's car review: both the
 * `com.google.android.gms.car.application` entry (MessagingStyle notifications on Auto) and the CarAppService (the
 * templated car screens) must be present in a default build.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class AndroidAutoManifestTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `the app declares Android Auto support`() {
        val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        assertTrue(info.metaData?.containsKey(CAR_APPLICATION) == true, "$CAR_APPLICATION lets Auto show the app")
    }

    @Test
    fun `the templated car app service is declared`() {
        val carAppServices = context.packageManager.queryIntentServices(Intent(CAR_APP_SERVICE), 0)
        assertTrue(
            carAppServices.any { it.serviceInfo.name == CAR_SERVICE_CLASS },
            "the car app service gives Auto the templated screens",
        )
    }

    @Test
    fun `the conversation action service is declared and not exported`() {
        val service =
            context.packageManager.getServiceInfo(
                ComponentName(context, "org.meshtastic.core.service.ConversationActionService"),
                0,
            )
        assertFalse(service.exported, "only our own notification actions may start it")
    }

    private companion object {
        const val CAR_APPLICATION = "com.google.android.gms.car.application"
        const val CAR_APP_SERVICE = "androidx.car.app.CarAppService"
        const val CAR_SERVICE_CLASS = "org.meshtastic.feature.car.service.MeshtasticCarAppService"
    }
}
