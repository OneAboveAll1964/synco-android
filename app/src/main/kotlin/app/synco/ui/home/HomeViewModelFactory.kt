package app.synco.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.synco.SyncServiceGateway
import app.synco.service.requireSyncoGraph

class HomeViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val application = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            "HomeViewModelFactory cannot create $modelClass"
        }
        return HomeViewModel(
            graph = application.requireSyncoGraph(),
            service = SyncServiceGateway(application),
        ) as T
    }
}
