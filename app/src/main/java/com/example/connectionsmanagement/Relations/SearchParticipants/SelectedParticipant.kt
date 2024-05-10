package com.example.connectionsmanagement.Relations.SearchParticipants

import java.io.Serializable

//选中的参与者
data class SelectedParticipant(var personId:Int, var name:String) : Serializable