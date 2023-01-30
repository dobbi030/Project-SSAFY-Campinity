package com.ssafy.campinity.presentation.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.campinity.data.remote.Resource
import com.ssafy.campinity.domain.entity.community.NoteQuestionTitle
import com.ssafy.campinity.domain.usecase.note.NoteMyQuestionUseCase
import com.ssafy.campinity.domain.usecase.note.NoteQuestionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityNoteViewModel @Inject constructor(
    private val noteMyQuestionUseCase: NoteMyQuestionUseCase,
    private val noteQuestionUseCase: NoteQuestionUseCase
) : ViewModel() {

    private val _noteQuestions = MutableLiveData<List<NoteQuestionTitle>>()
    private val _noteMyQuestions = MutableLiveData<List<NoteQuestionTitle>>()

    val noteQuestions: LiveData<List<NoteQuestionTitle>> = _noteQuestions
    val noteMyQuestions: LiveData<List<NoteQuestionTitle>> = _noteMyQuestions

    fun requestNoteQuestions(campsiteId: String) = viewModelScope.launch {
        when (val value = noteQuestionUseCase(campsiteId)) {
            is Resource.Success<List<NoteQuestionTitle>> -> {
                val noteQuestionList = value.data
                _noteQuestions.value = noteQuestionList
            }
            is Resource.Error -> {
                Log.d("requestNoteQuestions", "NoteQuestions: ${value.errorMessage}")
                _noteQuestions.value = listOf()
            }
        }
    }

    fun requestNoteMyQuestions(campsiteId: String) = viewModelScope.launch {
        when (val value = noteMyQuestionUseCase(campsiteId)) {
            is Resource.Success<List<NoteQuestionTitle>> -> {
                val noteQuestionList = value.data
                _noteMyQuestions.value = noteQuestionList
            }
            is Resource.Error -> {
                Log.d("requestNoteMyQuestions", "NoteMyQuestions: ${value.errorMessage}")
                _noteMyQuestions.value = listOf()
            }
        }
    }
}