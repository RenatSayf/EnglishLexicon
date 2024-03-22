package com.myapp.lexicon.video.web.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogBookmarksBinding
import com.myapp.lexicon.video.constants.BOOKMARKS
import com.myapp.lexicon.video.models.Bookmark

class BookmarksDialog: BottomSheetDialogFragment() {

    companion object {

        val TAG = "${BookmarksDialog::class.java.simpleName}.tag_325987"
        const val KEY_BOOKMARK_RESULT = "KEY_BOOKMARK_LISTENER_125987"
        const val KEY_SELECTED_BOOKMARK = "KEY_SELECTED_BOOKMARK_452879"

        fun newInstance(): BookmarksDialog {
            return BookmarksDialog()
        }
    }

    private var binding: DialogBookmarksBinding? = null
    private val bookmarksAdapter: BookmarkAdapter by lazy {
        BookmarkAdapter()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {

        setStyle(STYLE_NO_TITLE, R.style.AppBottomDialog)
        isCancelable = true

        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBookmarksBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            rvBookmarks.apply {
                adapter = bookmarksAdapter.apply {
                    setItemClickListener { bookmark: Bookmark ->
                        setFragmentResult(KEY_BOOKMARK_RESULT, Bundle().apply {
                            putString(KEY_SELECTED_BOOKMARK, bookmark.toString())
                        })
                        this@BookmarksDialog.dismiss()
                    }
                    submitList(BOOKMARKS)
                }
            }
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }


}