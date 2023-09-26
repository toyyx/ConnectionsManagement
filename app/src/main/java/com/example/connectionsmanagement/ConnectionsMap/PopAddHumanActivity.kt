package com.example.connectionsmanagement.ConnectionsMap


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
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
    lateinit var selectedGender:String//选中性别
    lateinit var selectedRelation:String//选中关系

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_add_human)

        //关系下拉框配置
        val spinner = findViewById<Spinner>(R.id.addRelationSpinner)
        val items = resources.getStringArray(R.array.relation_items)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 在选择不同选项时触发的操作
                val selectedItem = parent?.getItemAtPosition(position).toString()
                // 在这里处理选中的值（selectedItem）
                selectedRelation=selectedItem
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 当没有选项被选择时触发的操作
            }
        }

        //性别单选处理
        val radioGroup = findViewById<RadioGroup>(R.id.gender_radiogroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // 当选择不同的 RadioButton 时触发此回调
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedGender = radioButton.text.toString()
        }

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

        //确定按钮的功能：新增人物
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
            val dbHelper= ConnectionsDatabaseHelper(this,1)
            val db=dbHelper.writableDatabase

            //插入新人物信息到Person表，获取人物ID
            var newPersonId=db.insert("Person",null, contentValuesOf(
                "name" to findViewById<EditText>(R.id.addNameText).text.toString(),
                "gender" to selectedGender,
                "image_data" to imageByteArray,
                "phone_number" to findViewById<EditText>(R.id.addPhoneText).text.toString(),
                "email" to findViewById<EditText>(R.id.addEmailText).text.toString(),
                "notes" to findViewById<EditText>(R.id.addNotesText).text.toString())
            ).toInt()

            //插入到Connections表
            db.insert("Connections",null,contentValuesOf(
                "userId" to ConnectionsManagementApplication.NowUserId,
                "relationship" to selectedRelation,
                "personId" to newPersonId
            ))

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