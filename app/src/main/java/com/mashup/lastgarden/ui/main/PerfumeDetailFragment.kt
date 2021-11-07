package com.mashup.lastgarden.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mashup.base.autoCleared
import com.mashup.lastgarden.R
import com.mashup.lastgarden.databinding.FragmentPerfumeDetailBinding
import com.mashup.lastgarden.ui.BaseViewModelFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PerfumeDetailFragment : BaseViewModelFragment() {

    private var binding by autoCleared<FragmentPerfumeDetailBinding>()
    private lateinit var viewPagerAdapter: PerfumeDetailPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfumeDetailBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onSetupViews(view: View) {
        super.onSetupViews(view)
        initToolbar()
        initViewPager()
        initTabLayout()
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.navigationIcon?.setTint(Color.BLACK)
    }

    private fun initViewPager() {
        viewPagerAdapter = PerfumeDetailPagerAdapter(this)
        viewPagerAdapter.fragments = listOf(
            PerfumeInformationFragment(),
            ScentListFragment()
        )
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.setPageTransformer { page, _ ->
            updatePagerHeight(page, binding.viewPager)
        }
    }

    private fun updatePagerHeight(view: View, viewPager: ViewPager2) {
        view.post {
            val weightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(weightMeasureSpec, heightMeasureSpec)

            if (viewPager.layoutParams.height != view.measuredHeight) {
                viewPager.layoutParams =
                    (viewPager.layoutParams).also { layoutParams ->
                        layoutParams.height = view.measuredHeight
                    }
            }
        }
    }

    private fun initTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_perfume)
                1 -> tab.setIcon(R.drawable.ic_grid)
            }
        }.attach()
    }
}