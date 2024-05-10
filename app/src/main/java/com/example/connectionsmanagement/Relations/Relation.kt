package com.example.connectionsmanagement.Relations

//人脉中的人物
data class Relation (
    // 从 JSON 对象中获取数据
    var personId:Int,
    var relationship:String,
    val name:String,
    val gender:String,
    val image_path:String,
    val phone_number:String,
    val email:String,
    val notes:String
)

