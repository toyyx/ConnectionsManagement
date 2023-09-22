package com.example.connectionsmanagement.ConnectionsMap
//通用方法暂存处
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


fun Any.pxToDp(px: Int): Int {
        val density = ConnectionsManagementApplication.context.resources.displayMetrics.density
        return (px / density).toInt()
    }

fun dpToPx(dp: Int): Float {
    return dp * ConnectionsManagementApplication.context.resources.displayMetrics.density
}

//bitmap变为Drawable
fun Any.bitmapToDrawable(bitmap: Bitmap?, width: Int, height: Int): Drawable? {
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!,pxToDp(width), pxToDp(height), true)

    return BitmapDrawable(ConnectionsManagementApplication.context.resources, scaledBitmap)
}

fun bitmapToDrawable(bitmap: Bitmap?, width: Int, height: Int): Drawable? {
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, width, height, true)
    return BitmapDrawable(null, scaledBitmap)
}

fun pxToDp(context: Context, px: Float): Float {
    val density = context.resources.displayMetrics.density
    return px / density
}
//保存bitmap图片至本地
//fun saveBitmap(bitmap: Bitmap?){
//    val humanImageFile= File(externalCacheDir,"human_image${LocalDateTime.now()}.jpg")
//    if(!humanImageFile.exists()){
//        humanImageFile.createNewFile()
//    }
//    val saveHumanImageFileStream= FileOutputStream(humanImageFile)
//    bitmap?.compress(Bitmap.CompressFormat.JPEG,100,saveHumanImageFileStream)
//}

//uri转变为bitmap
//    private fun getBitmapFromUri(uri:Uri?)=uri?.let {contentResolver.openFileDescriptor(uri,"r")?.use {
//        BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }}
//
//fun getBitmapFromUri(uri: Uri?): Bitmap?{uri?.let {
//    try {
//        contentResolver.openInputStream(it)?.use { inputStream ->
//            return BitmapFactory.decodeStream(inputStream)
//        }
//    } catch (e: Exception) {
//        Toast.makeText(this,"Error loading bitmap: ${e.message}", Toast.LENGTH_SHORT).show()
//        Log.e("BitmapLoading", "Error loading bitmap: ${e.message}")
//    }
//}
//    return null
//}