package app.synco.service

import android.content.Context
import app.synco.sync.SyncoGraph

interface SyncoGraphOwner {
    val syncoGraph: SyncoGraph
}

fun Context.syncoGraphOrNull(): SyncoGraph? = (applicationContext as? SyncoGraphOwner)?.syncoGraph

fun Context.requireSyncoGraph(): SyncoGraph = requireNotNull(syncoGraphOrNull()) {
    "the application class must implement SyncoGraphOwner"
}
