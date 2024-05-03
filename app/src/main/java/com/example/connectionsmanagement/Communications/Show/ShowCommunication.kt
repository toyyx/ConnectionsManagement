package com.example.connectionsmanagement.Communications.Show

import com.example.connectionsmanagement.Relations.SearchParticipants.SelectedParticipant

class ShowCommunication(
    var eventId: String,
    var startTime: String,
    var finishTime:String,
    var title:String,
    var detail:String,
    var address:String,
    var participants:ArrayList<SelectedParticipant>) {
}