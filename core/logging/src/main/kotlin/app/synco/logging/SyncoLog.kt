package app.synco.logging

import android.util.Log

object SyncoLog {

    val clipboard = area("Clipboard")
    val transfer = area("Transfer")
    val discovery = area("Discovery")
    val session = area("Session")
    val engine = area("Engine")
    val identity = area("Identity")

    private fun area(name: String) = LogArea("Synco$name")
}
