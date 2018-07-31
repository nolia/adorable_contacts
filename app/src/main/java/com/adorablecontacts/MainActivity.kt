package com.adorablecontacts

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.adorablecontacts.data.Contact
import com.adorablecontacts.data.ContactsProvider
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

class MainActivity : AppCompatActivity() {

  private val contactProvider = ContactsProvider(this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)


    recyclerView.layoutManager = LinearLayoutManager(this)
    checkPermissions()

    loadDataAsync()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    when (requestCode) {
      100 -> loadDataAsync()
      else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private fun checkPermissions(): Boolean {
    return if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          arrayOf(Manifest.permission.READ_CONTACTS),
          100)
      false
    } else true
  }

  private fun loadDataAsync() = async(UI) {
    try {
      val contacts = async { contactProvider.contacts }.await()

      recyclerView.adapter = ContactsAdapter(LayoutInflater.from(this@MainActivity), contacts)
    } catch (e: Exception) {
      Log.e("Contacts", "error", e)
      Toast.makeText(this@MainActivity, "Error " + e.message, Toast.LENGTH_LONG).show()
    } finally {
      progressBar.visibility = View.GONE
      recyclerView.visibility = View.VISIBLE
    }

  }
}

internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
  val phoneTextView: TextView = itemView.findViewById(android.R.id.text2)
}

internal class ContactsAdapter(
    private val layoutInflater: LayoutInflater,
    private val contacts: List<Contact>) : RecyclerView.Adapter<ViewHolder>() {


  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder =
      ViewHolder(itemView = layoutInflater.inflate(android.R.layout.simple_list_item_2, viewGroup, false))

  override fun getItemCount(): Int =
      contacts.size

  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
    val (name, phoneNumber) = contacts[position]

    viewHolder.nameTextView.text = name
    viewHolder.phoneTextView.text = phoneNumber
  }

}


