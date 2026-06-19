package it.unibo.psm.ricettasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import it.unibo.psm.ricettasi.data.sync.SyncWorker
import it.unibo.psm.ricettasi.ui.navigation.RootGate
import it.unibo.psm.ricettasi.ui.theme.RicettaSiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SyncWorker.schedule(this)
        setContent {
            RicettaSiTheme {
                RootGate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SyncWorker.triggerNow(this)
    }
}
