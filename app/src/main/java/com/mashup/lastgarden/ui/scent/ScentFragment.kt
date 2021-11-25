package com.mashup.lastgarden.ui.scent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.mashup.base.autoCleared
import com.mashup.base.extensions.loadImage
import com.mashup.base.image.GlideRequests
import com.mashup.lastgarden.R
import com.mashup.lastgarden.data.vo.Story
import com.mashup.lastgarden.databinding.FragmentScentBinding
import com.mashup.lastgarden.extensions.dateConverter
import com.mashup.lastgarden.extensions.numberFormatter
import com.mashup.lastgarden.ui.BaseViewModelFragment
import com.mashup.lastgarden.ui.scent.comment.ScentCommentBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ScentFragment : BaseViewModelFragment(), ScentViewPagerAdapter.OnClickListener {
    private var binding by autoCleared<FragmentScentBinding>()
    private val viewModel: ScentViewModel by activityViewModels()

    private lateinit var perfumeStoryAdapter: ScentPagingAdapter

    @Inject
    lateinit var glideRequests: GlideRequests

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        perfumeStoryAdapter = ScentPagingAdapter(glideRequests)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScentBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onSetupViews(view: View) {
        super.onSetupViews(view)
        setupBottomSheet()
        setupRecyclerView()

        binding.closeButton.setOnClickListener {
            requireActivity().finish()
        }
        binding.detailButton.setOnClickListener {
            findNavController().navigate(
                R.id.actionScentFragmentToPerfumeDetailFragment,
                bundleOf(
                    "perfumeId" to requireArguments().getInt("perfumeId")
                )
            )
        }
    }

    override fun onBindViewModelsOnCreate() {
        val mainStorySet: MainStorySet? = requireArguments().getParcelable("mainStorySet")
        val storyIndex = requireArguments().getInt("storyIndex")
        val perfumeId = requireArguments().getInt("perfumeId")
        val storyId = requireArguments().getInt("storyId")

        when {
            mainStorySet != null -> {
                viewModel.getTodayAndHotStoryList(mainStorySet, storyIndex)
            }
            perfumeId != 0 -> {
                lifecycleScope.launchWhenCreated {
                    viewModel.getPerfumeStoryLists(perfumeId).collectLatest {
                        perfumeStoryAdapter.submitData(it)
                        binding.sortButton.isVisible = true
                    }
                }
            }
            storyId != 0 -> {
                viewModel.getPerfumeStory(storyId)
            }
        }
    }

    override fun onBindViewModelsOnViewCreated() {
        super.onBindViewModelsOnViewCreated()

        viewModel.perfumeStoryList.observe(viewLifecycleOwner) {
            binding.scentRecyclerView.adapter = ScentViewPagerAdapter(it, glideRequests, this)
            binding.detailButton.isVisible = true
        }
        viewModel.storyIndex.observe(viewLifecycleOwner) {
            binding.scentRecyclerView.scrollToPosition(it)
        }

        viewModel.perfumeStory.observe(viewLifecycleOwner) {
            if (it != null) {
                setScentItem(it)
            }
        }

        viewModel.sortOrder.observe(viewLifecycleOwner) {
            when (it) {
                Sort.POPULARITY -> binding.sortButton.text = getString(R.string.radio_btn_top)
                Sort.LATEST -> binding.sortButton.text = getString(R.string.radio_btn_middle)
                else -> binding.sortButton.text = getString(R.string.radio_btn_bottom)
            }
            //TODO 필터 API 적용
        }
    }

    private fun setupBottomSheet() {
        binding.sortButton.setOnClickListener {
            val bottomSheetDialog = ScentSortBottomSheetFragment()
            bottomSheetDialog.show(requireActivity().supportFragmentManager, "")
        }
    }

    private fun setupRecyclerView() {
        binding.scentRecyclerView.adapter = perfumeStoryAdapter
        binding.scentRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.scentRecyclerView)
    }

    private fun setScentItem(item: Story) {
        binding.scentRecyclerView.isVisible = false
        binding.sortButton.isVisible = false
        binding.includeScentLayout.root.isVisible = true
        binding.includeScentLayout.run {
            pageCountTextView.isVisible = false
            profileImageView.setImageUrl(glideRequests, item.perfumeImageUrl)
            nicknameTextView.text = item.userNickname
            dateTextView.text = dateConverter(item.createdAt)
            tagListTextView.text = item.tags?.joinToString(" ") { "#" + it.contents + " " }
            likeCountTextView.text = item.likeCount?.let { numberFormatter(it) }
            commentImageView.setOnClickListener { onCommentClick(item.storyId) }
            likeImageView.setOnClickListener { onLikeClick(item.storyId) }
            likeImageView.loadImage(glideRequests, R.drawable.ic_dislike)
            //TODO like 이미지 설정
        }
    }

    override fun onCommentClick(scentId: Int) {
        ScentCommentBottomSheetFragment().show(requireActivity().supportFragmentManager, "")
    }

    override fun onLikeClick(scentId: Int) {
        viewModel.getPerfumeStoryLike(scentId)
    }

}