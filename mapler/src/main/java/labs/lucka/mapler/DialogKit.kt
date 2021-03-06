package labs.lucka.mapler

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.defaultSharedPreferences

/**
 * Display dialogs in a bit better way.
 *
 * @author lucka-me
 * @since 0.1
 */
class DialogKit {

    companion object {

        /**
         * Display a dialog
         *
         * @param [context] The context
         * @param [titleId] Resource ID for Title
         * @param [message] String for message
         * @param [positiveButtonTextId] Resource ID for PositiveButton text, CONFIRM for default
         * @param [positiveButtonListener] OnClickListener for PositiveButton, nullable
         * @param [negativeButtonTextId] Resource ID for NegativeButton text, nullable
         * @param [negativeButtonListener] OnClickListener for NegativeButton, nullable
         * @param [icon] Icon for dialog, nullable
         * @param [cancelable] Could dialog canceled by tapping outside or back button, nullable
         *
         * @see <a href="https://www.jianshu.com/p/6bd7dd1cd491">使用着色器修改 Drawable 颜色 | 简书</a>
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showDialog(
            context: Context, titleId: Int, message: String?,
            positiveButtonTextId: Int = R.string.button_confirm,
            positiveButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            negativeButtonTextId: Int? = null,
            negativeButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            icon: Drawable? = null,
            cancelable: Boolean? = null
        ) {

            val builder = MaterialAlertDialogBuilder(context)
                .setTitle(titleId)
                .setIcon(icon)
                .setMessage(message)
                .setPositiveButton(positiveButtonTextId, positiveButtonListener)

            if (negativeButtonTextId != null)
                builder.setNegativeButton(negativeButtonTextId, negativeButtonListener)
            if (cancelable != null) builder.setCancelable(cancelable)

            builder.show()

        }

        /**
         * Display a dialog
         *
         * @param [context] The context
         * @param [titleId] Resource ID for Title
         * @param [messageId] Resource ID for message
         * @param [positiveButtonTextId] Resource ID for PositiveButton text, CONFIRM for default
         * @param [positiveButtonListener] OnClickListener for PositiveButton, nullable
         * @param [negativeButtonTextId] Resource ID for NegativeButton text, nullable
         * @param [negativeButtonListener] OnClickListener for NegativeButton, nullable
         * @param [icon] Icon for dialog, nullable
         * @param [cancelable] Could dialog canceled by tapping outside or back button, nullable
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showDialog(
            context: Context, titleId: Int, messageId: Int,
            positiveButtonTextId: Int = R.string.button_confirm,
            positiveButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            negativeButtonTextId: Int? = null,
            negativeButtonListener: ((DialogInterface, Int) -> (Unit))? = null,
            icon: Drawable? = null,
            cancelable: Boolean? = null
        ) {

            showDialog(
                context, titleId, context.getString(messageId), positiveButtonTextId,
                positiveButtonListener,
                negativeButtonTextId, negativeButtonListener,
                icon,
                cancelable
            )

        }

        /**
         * Display a simple alert with a CONFIRM button and un-cancelable
         *
         * @param [context] The context
         * @param [message] String for message to display
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showSimpleAlert(context: Context, message: String?) {
            showDialog(context, R.string.dialog_title_error, message, cancelable = false)
        }

        /**
         * Display a simple alert with a CONFIRM button and un-cancelable
         *
         * @param [context] The context
         * @param [messageId] Resource ID for message to display
         *
         * @author lucka-me
         * @since 0.1
         */
        fun showSimpleAlert(context: Context, messageId: Int) {
            showSimpleAlert(context, context.getString(messageId))
        }

        fun showSaveImageDialog(
            context: Context, image: Bitmap, onSaveButtonClick: () -> Unit, onDismiss: () -> Unit
        ) {
            val dialogLayout = View.inflate(context, R.layout.dialog_image, null)
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_result)
                .setView(dialogLayout)
                .setPositiveButton(R.string.button_save) { dialog, _ ->
                    dialog.dismiss()
                    onSaveButtonClick()
                }
                .setNegativeButton(R.string.button_cancel, null)
                .setOnDismissListener { onDismiss() }
                .show()
            dialogLayout.findViewById<ImageView>(R.id.imageView).setImageBitmap(image)
        }

//        fun showAddNewStyleTypeSelectDialog(context: Context, onSelected: (type: StyleData.Type) -> Unit) {
//            val itemList: ArrayList<CharSequence> = arrayListOf()
//            itemList.add(context.getString(R.string.dialog_add_style_from_json))
//            if (
//                !context.defaultSharedPreferences
//                    .getBoolean(context.getString(R.string.pref_mapbox_use_default_token), true)
//            ) {
//                itemList.add(context.getString(R.string.dialog_add_style_from_url))
//            }
//            MaterialAlertDialogBuilder(context)
//                .setTitle(R.string.dialog_title_add_style_from)
//                .setItems(itemList.toTypedArray()) { _, which ->
//                    val type = when (which) {
//                        0 -> StyleData.Type.LOCAL
//                        1 -> StyleData.Type.ONLINE
//                        else -> null
//                    } ?: return@setItems
//                    onSelected(type)
//                }
//                .show()
//        }
//
//        fun showAddNewStyleFromJsonDialog(context: Context, onAddClick: (StyleData) -> Unit) {
//            val layout = View.inflate(context, R.layout.dialog_add_style_json, null)
//            val editTextName: TextInputEditText = layout.findViewById(R.id.text_input_edit_name)
//            val editTextAuthor: TextInputEditText = layout.findViewById(R.id.text_input_edit_author)
//
//            MaterialAlertDialogBuilder(context)
//                .setTitle(R.string.dialog_title_add_style_from_json)
//                .setView(layout)
//                .setPositiveButton(R.string.button_save) { _, _ ->
//                    context.defaultSharedPreferences.edit {
//                        putString(context.getString(R.string.pref_style_author_last), editTextAuthor.text.toString())
//                    }
//                    onAddClick(
//                        StyleData(
//                            name = editTextName.text.toString(), author = editTextAuthor.text.toString(),
//                            type = StyleData.Type.LOCAL, inRandom = false
//                        )
//                    )
//                }
//                .setNegativeButton(R.string.button_cancel, null)
//                .show()
//
//            editTextAuthor.setText(
//                context.defaultSharedPreferences.getString(
//                    context.getString(R.string.pref_style_author_last),
//                    context.getString(R.string.pref_style_author_last_default)
//                )
//            )
//        }

        fun showAddNewStyleFromUrlDialog(context: Context, onSaveClick: (StyleData) -> Unit) {
            val layout = View.inflate(context, R.layout.dialog_add_style_url, null)
            val textInputEditUrl: TextInputEditText = layout.findViewById(R.id.text_input_edit_url)
            val editTextName: TextInputEditText = layout.findViewById(R.id.text_input_edit_name)
            val editTextAuthor: TextInputEditText = layout.findViewById(R.id.text_input_edit_author)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_add_style_from_url)
                .setView(layout)
                .setPositiveButton(R.string.button_save) { _, _ ->
                    context.defaultSharedPreferences.edit {
                        putString(
                            context.getString(R.string.pref_style_author_last),
                            editTextAuthor.text.toString()
                        )
                    }
                    onSaveClick(
                        StyleData(
                            name = editTextName.text.toString(),
                            author = editTextAuthor.text.toString(),
                            uri = textInputEditUrl.text.toString()
                        )
                    )
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()

            editTextAuthor.setText(context.defaultSharedPreferences.getString(
                context.getString(R.string.pref_style_author_last),
                context.getString(R.string.pref_style_author_last_default)
            ))
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            textInputEditUrl.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    saveButton.isEnabled = false
                    return@setOnFocusChangeListener
                }
                saveButton.isEnabled = textInputEditUrl.text.toString().isNotBlank()
            }
        }

        fun showStyleInformationDialog(
            context: Context, style: StyleData,
            onEditClick: () -> Unit, onShouldLoadPreviewImage: (ImageView) -> Unit
        ) {
            val layout = View.inflate(context, R.layout.dialog_style_info, null)
            val textName: TextView = layout.findViewById(R.id.text_name)
            val textAuthor: TextView = layout.findViewById(R.id.text_author)
            val imageType: ImageView = layout.findViewById(R.id.image_type)
            val imagePreview: ImageView = layout.findViewById(R.id.image_preview)
            val switchInRandom: SwitchMaterial = layout.findViewById(R.id.switch_in_random)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_style_information)
                .setView(layout)
                .setPositiveButton(R.string.button_edit) { _, _ -> onEditClick() }
                .setNegativeButton(R.string.button_dismiss) { _, _ ->
                    style.inRandom = switchInRandom.isChecked
                }
                .show()

            textName.text = style.name
            textAuthor.text = context.getString(R.string.style_author, style.author)
            imageType
                .setImageResource(if (style.isLocal) R.drawable.ic_local else R.drawable.ic_online)

            // Disable Edit button if it's default style
            when (style.type) {

                StyleData.Type.MAPBOX, StyleData.Type.EXTRA -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }

                else -> {
                }

            }
            val image = DataKit.loadStylePreviewImage(context, style)
            if (image == null) {
                // Prevent the snapshotter issue
                if (style.type != StyleData.Type.LOCAL) onShouldLoadPreviewImage(imagePreview)
            } else {
                imagePreview.setImageBitmap(image)
            }

            switchInRandom.isChecked = style.inRandom

        }

        fun showEditStyleDialog(context: Context, style: StyleData, onSaveClick: () -> Unit) {
            val layout = View.inflate(context, R.layout.dialog_edit_style, null)
            val editTextName: TextInputEditText = layout.findViewById(R.id.text_input_edit_name)
            val editTextAuthor: TextInputEditText = layout.findViewById(R.id.text_input_edit_author)
            val editTextUrl: TextInputEditText = layout.findViewById(R.id.text_input_edit_url)

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_edit_style)
                .setView(layout)
                .setPositiveButton(R.string.button_save) { _, _ ->
                    context.defaultSharedPreferences.edit {
                        putString(context.getString(R.string.pref_style_author_last), editTextAuthor.text.toString())
                    }
                    style.name = editTextName.text.toString()
                    style.author = editTextAuthor.text.toString()
                    if (style.type == StyleData.Type.ONLINE) style.uri = editTextUrl.text.toString()
                    onSaveClick()
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()

            editTextName.setText(style.name)
            editTextAuthor.setText(style.author)

            // Handle the uri
            if (style.type == StyleData.Type.LOCAL) {
                layout.findViewById<TextInputLayout>(R.id.text_input_layout_url)
                    .visibility = View.GONE
            } else {
                editTextUrl.setText(style.uri)
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.isEnabled = false
                editTextUrl.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        saveButton.isEnabled = false
                        return@setOnFocusChangeListener
                    }
                    saveButton.isEnabled = editTextUrl.text.toString().isNotBlank()
                }
            }

        }

    }
}