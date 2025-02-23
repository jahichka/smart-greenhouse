package com.example.greenhouse

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class NoteDialogFragment(
    private val note: Note?,
    private val onSave: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_note, null)
        dialog.setContentView(view)

        val noteEditText: EditText = view.findViewById(R.id.edit_text_note_content)
        val saveButton: Button = view.findViewById(R.id.button_save_note)
        val cancelButton: Button = view.findViewById(R.id.button_cancel_note)

        note?.let {
            noteEditText.setText(it.content)
        }

        saveButton.setOnClickListener {
            val newText = noteEditText.text.toString().trim()
            if (newText.isNotEmpty()) {
                onSave(newText)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Note cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        return dialog
    }
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
            dialog.window?.setLayout(width, height)
        }
    }

}
