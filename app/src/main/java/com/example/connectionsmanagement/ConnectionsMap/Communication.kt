package com.example.connectionsmanagement.ConnectionsMap

import android.graphics.Bitmap

class Communication(
    var eventId: String,
    var startTime: String,
    var finishTime:String,
    var title:String,
    var detail:String,
    var address:String,
    var participants:ArrayList<String>) {
}