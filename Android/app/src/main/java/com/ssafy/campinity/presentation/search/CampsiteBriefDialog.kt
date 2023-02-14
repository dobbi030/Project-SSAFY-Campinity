package com.ssafy.campinity.presentation.search

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.campinity.R
import com.ssafy.campinity.databinding.DialogCampsiteBriefBinding
import com.ssafy.campinity.domain.entity.search.CampsiteBriefInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CampsiteBriefDialog(
    context: Context,
    private val position: Int,
    private val campsiteBriefData: CampsiteBriefInfo,
    private val navigationToSearchPostboxFragment: (String) -> Unit,
    private val navigationToCampsiteDetailFragment: (Int, String) -> Unit,
    private val scrapCampsite: suspend (Int, String) -> String
) : Dialog(context) {

    private lateinit var binding: DialogCampsiteBriefBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogCampsiteBriefBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDialog()
        initListener()
    }

    private fun initDialog() {
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        binding.item = campsiteBriefData
        binding.apply {
            rvCampsiteImage.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvCampsiteImage.adapter =
                CampsiteBriefImageAdapter(context, campsiteBriefData.thumbnails)

            if (campsiteBriefData.isScraped)
                btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_on)
            else
                btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_off)
        }
    }

    private fun initListener() {
        binding.btnPostbox.setOnClickListener {
            dismiss()
            navigationToSearchPostboxFragment(campsiteBriefData.campsiteId)
        }

        binding.btnBookmark.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val isScraped = scrapCampsite(position, campsiteBriefData.campsiteId)
                if (isScraped == "true")
                    binding.btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_on)
                else if (isScraped == "false")
                    binding.btnBookmark.setBackgroundResource(R.drawable.ic_bookmark_off)
            }
        }

        binding.clCampsiteBrief.setOnClickListener {
            dismiss()
            navigationToCampsiteDetailFragment(position, campsiteBriefData.campsiteId)
        }
    }
}