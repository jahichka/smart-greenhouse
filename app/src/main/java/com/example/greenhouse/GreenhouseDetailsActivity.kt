package com.example.greenhouse

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class GreenhouseDetailsActivity : AppCompatActivity() {
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var addNoteButton: Button
    private lateinit var editNoteButton: Button
    private lateinit var deleteNoteButton: Button
    private lateinit var addPhotoButton: Button

    private lateinit var noteAdapter: NotesAdapter
    private lateinit var photoAdapter: PhotosAdapter

    private val notesList = mutableListOf<Note>()
    private val photosList = mutableListOf<Photo>()

    private var greenhouseId: String? = null
    private var selectedNote: Note? = null

    private lateinit var database: GreenhouseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greenhouse_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = GreenhouseDatabase.getDatabase(this)

        notesRecyclerView = findViewById(R.id.recycler_view_notes)
        photosRecyclerView = findViewById(R.id.recycler_view_photos)
        addNoteButton = findViewById(R.id.button_add_note)
        editNoteButton = findViewById(R.id.button_edit_note)
        deleteNoteButton = findViewById(R.id.button_delete_note)
        addPhotoButton = findViewById(R.id.button_add_photo)

        greenhouseId = intent.getStringExtra("greenhouseId") ?: return

        setupRecyclerViews()
        loadNotes()
        loadPhotos()

        addNoteButton.setOnClickListener { openNoteDialog(null) }
        editNoteButton.setOnClickListener {
            selectedNote?.let { openNoteDialog(it) } ?: showToast("Select a note first")
        }
        deleteNoteButton.setOnClickListener { deleteNote() }
        addPhotoButton.setOnClickListener { selectImage() }
    }

    private fun setupRecyclerViews() {
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NotesAdapter(notesList) { note -> selectedNote = note; showToast("Selected: ${note.content}") }
        notesRecyclerView.adapter = noteAdapter

        photosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        photoAdapter = PhotosAdapter(photosList) { photo -> deletePhoto(photo) }
        photosRecyclerView.adapter = photoAdapter
    }

    private fun loadNotes() {
        notesList.clear()
        notesList.addAll(database.greenhouseDao().getNotes(greenhouseId!!))
        noteAdapter.notifyDataSetChanged()
    }

    private fun openNoteDialog(note: Note?) {
        val dialog = NoteDialogFragment(note) { newText ->
            if (note != null && newText == note.content) {
                showToast("No changes made")
                return@NoteDialogFragment
            }

            if (note == null) addNote(newText) else updateNote(note, newText)
        }
        dialog.show(supportFragmentManager, "NoteDialog")
    }

    private fun addNote(text: String) {
        val note = Note(greenhouseId = greenhouseId!!, content = text)
        database.greenhouseDao().insertNote(note)
        loadNotes()
        showToast("Note added")
    }

    private fun updateNote(oldNote: Note, newText: String) {
        val updatedNote = oldNote.copy(content = newText)
        database.greenhouseDao().updateNote(updatedNote)
        loadNotes()
        showToast("Note updated")
    }

    private fun deleteNote() {
        selectedNote?.let {
            database.greenhouseDao().deleteNote(it)
            loadNotes()
            selectedNote = null
            showToast("Note deleted")
        } ?: showToast("Select a note first")
    }

    private fun loadPhotos() {
        photosList.clear()
        photosList.addAll(database.greenhouseDao().getPhotos(greenhouseId!!))
        photoAdapter.notifyDataSetChanged()
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return
            savePhotoToInternalStorage(imageUri)
        }
    }

    private fun savePhotoToInternalStorage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val filename = "photo_${UUID.randomUUID()}.jpg"
            val file = File(filesDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val savedUri = Uri.fromFile(file).toString()
            val photo = Photo(greenhouseId = greenhouseId!!, uri = savedUri)
            database.greenhouseDao().insertPhoto(photo)
            photosList.add(photo)
            photoAdapter.notifyDataSetChanged()
            showToast("Photo saved locally")
        } catch (e: IOException) {
            showToast("Failed to save photo")
        }
    }

    private fun deletePhoto(photo: Photo) {
        val file = File(Uri.parse(photo.uri).path!!)
        if (file.exists()) {
            file.delete()
        }
        database.greenhouseDao().deletePhoto(photo)
        photosList.remove(photo)
        photoAdapter.notifyDataSetChanged()
        showToast("Photo deleted")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
