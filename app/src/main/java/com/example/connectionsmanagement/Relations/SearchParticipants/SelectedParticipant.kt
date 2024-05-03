package com.example.connectionsmanagement.Relations.SearchParticipants

import  android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


data class SelectedParticipant(var personId:Int, var name:String) : Serializable