package org.bcu.audiorecorder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity(),OnItemClickListener {
    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var myAdapter: Adapter
    private lateinit var db: AppDatabase
    private lateinit var  searchInput:TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        records = ArrayList()

        db = Room.databaseBuilder(this, AppDatabase::class.java, "audioRecords").build()
        myAdapter = Adapter(records,this)

        recyclerView.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(context)
        }
        fetchAll()

        searchInput=findViewById(R.id.search_input)

       searchInput.addTextChangedListener(object: TextWatcher{
           override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                 var query=s.toString()
                  searchDataBase(query)
           }

           override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

           }

           override fun afterTextChanged(s: Editable?) {

           }

       })
    }

    private fun searchDataBase(query: String) {
        GlobalScope.launch {
            records.clear()
            var queryResult=db.audioRecordDao().searchDatabase("%$query%")
            records.addAll(queryResult)
            runOnUiThread {
                myAdapter.notifyDataSetChanged()
            }

        }

    }

    private fun fetchAll(){
        GlobalScope.launch {
            records.clear()
            var queryResult=db.audioRecordDao().getAll()
            records.addAll(queryResult)
            myAdapter.notifyDataSetChanged()
        }
    }

    override fun onItemClickListener(position: Int) {
      var audioRecord=records[position]
        var intent=Intent(this,AudioPlayerActivity::class.java)
        intent.putExtra("filepath",audioRecord.filePath)
        intent.putExtra("filename",audioRecord.filename)
        startActivity(intent)
    }

    override fun onItemLongClickListener(position: Int) {
        Toast.makeText(this,"long click",Toast.LENGTH_SHORT).show()
    }
}