package com.example.flightmobileapp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HostViewModel(application: Application) : AndroidViewModel(application) {
    fun insert(host: Host, hostDao: HostDao?) = viewModelScope.launch(Dispatchers.IO) {
        hostDao?.insertHost(host);
    }
    fun clear(hostDao: HostDao?) = viewModelScope.launch(Dispatchers.IO) {
    }

    fun getAllHosts(hostDao: HostDao?) : List<Host>? {
        return hostDao?.getAll()
    }

    private fun deleteHost(localHost: String, hostDao: HostDao?) = viewModelScope.launch(Dispatchers.IO) {
    }
}