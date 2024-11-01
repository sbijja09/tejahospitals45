package com.example.sqlitedatabase

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sqlitedatabase.databinding.ActivityStudentBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

class Student : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding
    private var imageByteArray: ByteArray? = null
    private var db: SQLiteDatabase? = null
    private var myFile: File? = null
    private lateinit var msg: String
    private var sId:Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        myFile = ConnectionClass.myFile
        db = SQLiteDatabase.openOrCreateDatabase(myFile!!.absolutePath, null, null)
        val i = intent
        msg = i.getStringExtra("msg").toString()
        sId = i.getIntExtra("sid",0)
        when(msg){
            "add" -> binding.btnSave.text = "Insert Data"
            "edit" -> {
                binding.btnSave.text = "Update Data"
                binding.btnDel.visibility = View.VISIBLE
                showData()
            }

        }

    }

    fun uploadStudentImage(view: View) {
        val myFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        myFileIntent.type = "image/*"
        activityResultLauncher.launch(myFileIntent)
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data!!.data
            try {
                val inputStream =
                    contentResolver.openInputStream(uri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                imageByteArray = stream.toByteArray()
                binding.imageView.setImageBitmap(myBitmap)
            } catch (ex: java.lang.Exception) {
                Toast.makeText(this, ex.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun insertData() {
        val sName = binding.edtName.text.toString()
        val sAddress = binding.edtAddress.text.toString()
        val sClass = binding.edtClass.text.toString()
        val sAge = binding.edtAge.text.toString()

        if (binding.edtName.text.toString().isEmpty()) {
            Toast.makeText(this, "Input student Name", Toast.LENGTH_SHORT).show()
            binding.edtName.requestFocus()
        } else if (binding.edtClass.text.toString().isEmpty()) {
            Toast.makeText(this, "Input class Name", Toast.LENGTH_SHORT).show()
            binding.edtName.requestFocus()
        } else {
            try {
                val values = ContentValues()
                values.put("studentName", sName)
                values.put("address", sAddress)
                values.put("class", sClass)
                values.put("age", sAge)
                values.put("studentPhoto", imageByteArray)
                db!!.insert("student", null, values)
                Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show()

                clr()

            } catch (ex: Exception) {
                Toast.makeText(this, "insert: " + ex.message.toString(), Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateData() {
        val sName = binding.edtName.text.toString()
        val sAddress = binding.edtAddress.text.toString()
        val sClass = binding.edtClass.text.toString()
        val sAge = binding.edtAge.text.toString()

        if (binding.edtName.text.toString().isEmpty()) {
            Toast.makeText(this, "Input student Name", Toast.LENGTH_SHORT).show()
            binding.edtName.requestFocus()
        } else if (binding.edtClass.text.toString().isEmpty()) {
            Toast.makeText(this, "Input class Name", Toast.LENGTH_SHORT).show()
            binding.edtName.requestFocus()
        } else {
            try {
                val values = ContentValues()
                values.put("studentName", sName)
                values.put("address", sAddress)
                values.put("class", sClass)
                values.put("age", sAge)
                values.put("studentPhoto", imageByteArray)
                db!!.update("student", values, "studentId =$sId", null)
                Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
                clr()

            } catch (ex: Exception) {
                Toast.makeText(this,   ex.message.toString(), Toast.LENGTH_LONG).show()
            }
        }

    }


    private fun clr() {
        binding.edtName.text.clear()
        binding.edtAddress.text.clear()
        binding.edtClass.text.clear()
        binding.edtAge.text.clear()
        binding.imageView.setImageBitmap(null)
        imageByteArray = null

    }

    fun btnClick(view: View) {
        when(msg){
            "add" -> insertData()
            "edit" -> updateData()
        }

    }

    fun delData(view: View) {
        val values = ContentValues()
        values.put("studentId", sId)
        db!!.delete("student",  "studentId =$sId", null)
        Toast.makeText(this, "Data Deleted", Toast.LENGTH_SHORT).show()
        finish()
    }
    private fun showData(){
        try {
            val cursor =
                db!!.rawQuery("SELECT * FROM student WHERE studentId = $sId", null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    binding.edtName.setText(cursor.getString(1).toString())
                    binding.edtAddress.setText(cursor.getString(2).toString())
                    binding.edtClass.setText(cursor.getString(2).toString())
                    binding.edtAge.setText(cursor.getString(4).toString())
                    var x: ByteArray? = null
                    if (cursor.getBlob(5) != null) {
                        x = cursor.getBlob(5)
                        val bitmap = BitmapFactory.decodeByteArray(x, 0, x.size)
                        binding.imageView.setImageBitmap(bitmap)
                        imageByteArray = x
                    }
                }

            }

        } catch (ex: Exception) {
            Toast.makeText(this, ex.message.toString(), Toast.LENGTH_SHORT).show()
        }

    }
}