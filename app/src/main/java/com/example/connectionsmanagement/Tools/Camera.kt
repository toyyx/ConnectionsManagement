package com.example.connectionsmanagement.Tools

import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.example.connectionsmanagement.R
import java.io.File
import java.time.LocalDateTime

class Camera() {
    var imageUri: Uri? = null  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器

    var imageUri_standby: Uri? = null  //图片地址
    private lateinit var getPicturesFromCameraActivity_standby: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity_standby: ActivityResultLauncher<String> //相册获取图片-启动器

    // 主构造函数
    constructor(activity: FragmentActivity, view_1: ImageView): this() {
        setCameraFunction(activity,view_1)

    }
    constructor(activity: FragmentActivity, view_1: ImageView, view_2: ImageView): this() {
        setCameraFunction(activity,view_1,view_2)
    }


    fun setCameraFunction(activity: FragmentActivity, view: ImageView) {
        //设置初始值
        imageUri = null

        //拍照获取图片处理过程
        getPicturesFromCameraActivity =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    view.setImageURI(imageUri)
                }else{
                    imageUri=null
                }
            }

        //相册获取图片处理过程
        getPicturesFromAlbumActivity =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) {
                if(it!=null){
                    view.setImageURI(it)
                    imageUri = it
                }
            }

        //设置人物图像并保存至本地
        view.setOnClickListener {
            val popupMenu = PopupMenu(activity, it)
            popupMenu.menuInflater.inflate(R.menu.camera_or_album, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //拍照获取图片
                    R.id.menu_camera -> {
                        //设置图片存储位置
                        val humanImage =
                            File(activity.externalCacheDir, "human_image${LocalDateTime.now()}.jpg")
                        if (!humanImage.exists()) {
                            humanImage.createNewFile()
                        }
                        //获取uri
                        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                ConnectionsManagementApplication.context,
                                "com.example.connectionsmanegement.fileprovider",
                                humanImage
                            )
                        } else {
                            Uri.fromFile(humanImage)
                        }
                        getPicturesFromCameraActivity.launch(imageUri)
                        true
                    }
                    //从相册获取图片
                    R.id.menu_album -> {
                        getPicturesFromAlbumActivity.launch("image/*")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    fun setCameraFunction(activity: FragmentActivity, view_1: ImageView,view_2: ImageView) {
        //设置初始值
        imageUri = null

        //拍照获取图片处理过程
        getPicturesFromCameraActivity =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    view_1.setImageURI(imageUri)
                }else{
                    imageUri=null
                }
            }

        //相册获取图片处理过程
        getPicturesFromAlbumActivity =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) {
                if(it!=null){
                    view_1.setImageURI(it)
                    imageUri = it
                }
            }

        //设置人物图像并保存至本地
        view_1.setOnClickListener {
            val popupMenu = PopupMenu(activity, it)
            popupMenu.menuInflater.inflate(R.menu.camera_or_album, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //拍照获取图片
                    R.id.menu_camera -> {
                        //设置图片存储位置
                        val humanImage =
                            File(activity.externalCacheDir, "human_image${LocalDateTime.now()}.jpg")
                        if (!humanImage.exists()) {
                            humanImage.createNewFile()
                        }
                        //获取uri
                        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                ConnectionsManagementApplication.context,
                                "com.example.connectionsmanegement.fileprovider",
                                humanImage
                            )
                        } else {
                            Uri.fromFile(humanImage)
                        }
                        getPicturesFromCameraActivity.launch(imageUri)
                        true
                    }
                    //从相册获取图片
                    R.id.menu_album -> {
                        getPicturesFromAlbumActivity.launch("image/*")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }

        //设置初始值
        imageUri_standby = null

        //拍照获取图片处理过程
        getPicturesFromCameraActivity_standby =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    view_2.setImageURI(imageUri_standby)
                }else{
                    imageUri_standby=null
                }
            }

        //相册获取图片处理过程
        getPicturesFromAlbumActivity_standby =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) {
                if(it!=null){
                    view_2.setImageURI(it)
                    imageUri_standby = it
                }
            }

        //设置人物图像并保存至本地
        view_2.setOnClickListener {
            val popupMenu = PopupMenu(activity, it)
            popupMenu.menuInflater.inflate(R.menu.camera_or_album, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //拍照获取图片
                    R.id.menu_camera -> {
                        //设置图片存储位置
                        val humanImage =
                            File(activity.externalCacheDir, "human_image${LocalDateTime.now()}.jpg")
                        if (!humanImage.exists()) {
                            humanImage.createNewFile()
                        }
                        //获取uri
                        imageUri_standby = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                ConnectionsManagementApplication.context,
                                "com.example.connectionsmanegement.fileprovider",
                                humanImage
                            )
                        } else {
                            Uri.fromFile(humanImage)
                        }
                        getPicturesFromCameraActivity_standby.launch(imageUri_standby)
                        true
                    }
                    //从相册获取图片
                    R.id.menu_album -> {
                        getPicturesFromAlbumActivity_standby.launch("image/*")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

}