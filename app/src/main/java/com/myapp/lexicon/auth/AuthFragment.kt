package com.myapp.lexicon.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.myapp.lexicon.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    companion object {

        val TAG = "${AuthFragment::class.java.simpleName}.TAG"
        fun newInstance() = AuthFragment()
    }

    private lateinit var binding: FragmentAuthBinding
    private val authVM: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}