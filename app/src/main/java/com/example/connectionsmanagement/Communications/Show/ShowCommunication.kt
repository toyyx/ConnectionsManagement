package com.example.connectionsmanagement.Communications.Show

import com.example.connectionsmanagement.Relations.SearchParticipants.SelectedParticipant

//展示的交际信息
class ShowCommunication(
    var eventId: String,
    var startTime: String,
    var finishTime:String,
    var title:String,
    var detail:String,
    var address:String,
    var participants:ArrayList<SelectedParticipant>) {
}