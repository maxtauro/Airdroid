package com.maxtauro.airdroid.wearablecomponents

import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable

class WearableDataClientManager : DataClient.OnDataChangedListener {

    fun instantiateClient(context: Context) {
        Wearable.getDataClient(context).addListener(this)
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val TAG = "WearableConnectionManager"

    }
}