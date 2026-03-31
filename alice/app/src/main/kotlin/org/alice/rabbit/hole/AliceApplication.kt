package org.alice.rabbit.hole

import android.app.Application
import org.alice.rabbit.hole.core.surveillance.injection.surveillanceModule
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import java.security.Security

private const val SYSTEM_BOUNCY_CASTLE_PROVIDER = "BC"
private const val PRIMARY_PROVIDER_POSITION = 1

@OptIn(KoinExperimentalAPI::class)
class AliceApplication : Application(), KoinStartup {

    override fun onKoinStartup() = koinConfiguration {
        androidContext(this@AliceApplication)
        modules(surveillanceModule)
    }

    override fun onCreate() {
        super.onCreate()
        replaceSecurityProvider()
    }

    private fun replaceSecurityProvider() {
        Security.removeProvider(SYSTEM_BOUNCY_CASTLE_PROVIDER)
        Security.insertProviderAt(BouncyCastleProvider(), PRIMARY_PROVIDER_POSITION)
    }
}
