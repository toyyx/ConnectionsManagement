package com.example.connectionsmanagement.Communications

//交际数据
class Communication(
    var eventId: String,
    var startTime: String,
    var finishTime:String,
    var title:String,
    var detail:String,
    var address:String,
    var participants:ArrayList<String>) {
}