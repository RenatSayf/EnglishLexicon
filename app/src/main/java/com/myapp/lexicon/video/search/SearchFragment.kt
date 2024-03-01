@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.video.search

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentSearchBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.video.models.query.HistoryQuery
import com.myapp.lexicon.video.models.query.SearchQuery

class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private var binding: FragmentSearchBinding? = null

    private val searchVM: SearchViewModel by lazy {
        val factory = SearchViewModel.Factory()
        ViewModelProvider(this, factory)[SearchViewModel::class.java]
    }

    private val searchAdapter: SearchQueryAdapter by lazy {
        SearchQueryAdapter.getInstance()
    }

    private val locker: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            rvSuggestions.apply {
                adapter = searchAdapter
            }

            etQuery.doOnTextChanged { text, start, before, count ->
                searchVM.fetchSuggestions(
                    query = text.toString(),
                    onStart = {
                        locker.lock()
                    },
                    onComplete =  {
                        locker.unLock()
                    },
                    onResult = { result: Result<List<SearchQuery>> ->
                        result.onSuccess { value: List<SearchQuery> ->
                            searchAdapter.submitList(value)
                        }
                        result.onFailure { exception ->
                            exception.printStackTraceIfDebug()
                        }
                    },
                    onFailure = { e: Exception ->
                        e.printStackTraceIfDebug()
                    }
                )
            }

            searchAdapter.setOnQueryItemClick { item: SearchQuery ->

            }

            searchAdapter.setOnHistoryItemClick { item: HistoryQuery ->

            }

        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

}