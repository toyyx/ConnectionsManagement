package com.example.connectionsmanagement.ConnectionsMap


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import com.example.connectionsmanagement.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime


class PopAddHumanActivity : AppCompatActivity() {
    lateinit var imageUri: Uri  //图片地址
    private lateinit var getPicturesFromCameraActivity: ActivityResultLauncher<Uri>//拍照获取图片-启动器
    private lateinit var getPicturesFromAlbumActivity: ActivityResultLauncher<String> //相册获取图片-启动器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_add_human)
        //拍照获取图片处理过程
        getPicturesFromCameraActivity =registerForActivityResult(ActivityResultContracts.TakePicture()){
            if(it) {
                val imageView: ImageView? =findViewById<CircleImageView>(R.id.addImage)
                imageView?.setImageURI(imageUri)
            }
        }

        //相册获取图片处理过程
        getPicturesFromAlbumActivity = registerForActivityResult(ActivityResultContracts.GetContent()) {
            val imageView: ImageView? =findViewById<CircleImageView>(R.id.addImage)
            imageView?.setImageURI(it)
            imageUri=it
        }

        //设置取消按钮关闭弹窗
        findViewById<Button>(R.id.popAddCancelButton).setOnClickListener {
            finish()
        }

        //设置人物图像并保存至本地
        findViewById<CircleImageView>(R.id.addImage).setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.camera_or_album,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //拍照获取图片
                    R.id.menu_camera -> {
                        //设置图片存储位置
                        val humanImage= File(externalCacheDir,"human_image${LocalDateTime.now()}.jpg")
                        if(!humanImage.exists()){
                            humanImage.createNewFile()
                        }
                        //获取uri
                        imageUri=if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                            FileProvider.getUriForFile(this,"com.example.connectionsmanegement.fileprovider",humanImage)
                        }else{
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

        //确定按钮的功能
        findViewById<Button>(R.id.popAddSureButton).setOnClickListener {
            //保存照片至本地
            val imageBitmap=getBitmapFromUri(imageUri)
            saveBitmap(imageBitmap)

            //转为将bitmap转为字节数组，便于后续数据库存储
            val stream = ByteArrayOutputStream()

            //quality设为100时，程序将因照片过大而崩溃，有待后续优化
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 75, stream)
            val imageByteArray = stream.toByteArray()

            //存储数据
            val dbHelper= ConnectionsDatabaseHelper(this,"ConnectionsStore.db",1)
            val db=dbHelper.writableDatabase
            db.insert("Human",null, contentValuesOf(
                "name" to "${findViewById<EditText>(R.id.addNameText).text.toString()}",
                "notes" to "${findViewById<EditText>(R.id.addNotesText).text.toString()}",
                "image_data" to imageByteArray)
            )
            Toast.makeText(this,"创建成功",Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    //uri转变为bitmap
    fun getBitmapFromUri(uri:Uri?)=uri?.let {  contentResolver.openFileDescriptor(uri,"r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }}

    //保存bitmap图片至本地
    fun saveBitmap(bitmap: Bitmap?){
        val humanImageFile= File(externalCacheDir,"${findViewById<EditText>(R.id.addNameText).text.toString()}${findViewById<EditText>(
            R.id.addNotesText
        ).text.toString()}.jpg")
        if(!humanImageFile.exists()){
            humanImageFile.createNewFile()
        }
        val saveHumanImageFileStream= FileOutputStream(humanImageFile)
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,saveHumanImageFileStream)
    }
}