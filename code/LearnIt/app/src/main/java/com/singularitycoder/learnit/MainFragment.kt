package com.singularitycoder.learnit

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.PowerManager
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.singularitycoder.learnit.databinding.FragmentMainBinding
import com.singularitycoder.learnit.helpers.BottomSheetTag
import com.singularitycoder.learnit.helpers.EditEvent
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.hideKeyboard
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onImeClick
import com.singularitycoder.learnit.helpers.onSafeClick
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        private val TAG = this::class.java.simpleName

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private var previousConfig: Configuration? = null

    private val booksAdapter = DownloadsAdapter()

    private var booksList = listOf<Subject?>()

    private lateinit var binding: FragmentMainBinding

    private var isTtsPresent = false

    private var selectedTtsLanguage: String? = Locale.getDefault().displayName

    private val bookViewModel by viewModels<BookViewModel>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<MaterialCardView>

    private var isServiceBound = false

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    override fun onResume() {
        super.onResume()
    }

    /** Since onDestroy is not a guarenteed call when app destroyed */
    override fun onPause() {
        super.onPause()
    }

    // https://stackoverflow.com/questions/59694023/listening-on-dark-theme-in-notification-area-toggle-and-be-notified-of-a-chang
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun FragmentMainBinding.setupUI() {
        rvBooks.apply {
            layoutAnimation = rvBooks.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = booksAdapter
        }
        layoutSearch.etSearch.hint = "Search"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentMainBinding.setupUserActionListeners() {
        root.setOnClickListener {}

        fabAddSubject.onSafeClick {
//            AddSubjectBottomSheetFragment.newInstance().show(parentFragmentManager, BottomSheetTag.TAG_BOOK_READER_FILTERS)
            EditBottomSheetFragment.newInstance(
                eventType = EditEvent.RENAME_DOWNLOAD_FILE
            ).show(parentFragmentManager, BottomSheetTag.TAG_EDIT)
        }

        booksAdapter.setOnItemClickListener { book, position ->

        }

        booksAdapter.setOnItemLongClickListener { book, view, position ->

        }

        layoutGrantPermission.btnGivePermission.onSafeClick {

        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                booksAdapter.subjectList = booksList
                booksAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }
            booksAdapter.subjectList = booksList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            booksAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = bookViewModel.getAllBookItemsFlow()) { booksList: List<Subject?> ->
            if (this.booksList.isNotEmpty() && this.booksList == booksList) {
                return@collectLatestLifecycleFlow
            }
            this.booksList = booksList
            booksAdapter.subjectList = booksList
            booksAdapter.notifyDataSetChanged()
//            booksAdapter.notifyItemInserted(currentBookPosition)
//            currentBookPosition++
//            binding.nestedScrollView.scrollTo(0, 0)
//            binding.rvBooks.runLayoutAnimation(globalLayoutAnimation)
        }
    }
}