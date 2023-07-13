package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.MAX_NO_OF_WORDS
import com.example.unscramble.SCORE_INCREASE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    // StateFlowをGameUiStateから公開
    private var _uiState = MutableStateFlow(GameUiState(currentWord = Word("")))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    lateinit var allWords: MutableList<Word>

    // ユーザーが入力した単語を保存
    var userGuess by mutableStateOf("")
        private set

    /**
     * リストからランダムな単語を選択してシャッフルする
     */
    private fun pickRandomWordAndShuffle(): Word {
        val currentWord = allWords.random()
        return if (currentWord.isUsed) {
            pickRandomWordAndShuffle()
        } else {
            val index = allWords.indexOf(currentWord)
            allWords[index] = currentWord.copy(isUsed = true)
            allWords[index]
        }
    }

    /**
     * ゲームをリセットする
     */
    fun resetGame() {
        allWords = allWords.map { Word(answer = it.answer) }.toMutableList()
        _uiState.value = GameUiState(currentWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    /**
     * ユーザーの推測と正解を照合する
     */
    fun checkUserGuess() {
        if (userGuess.equals(uiState.value.currentWord.answer, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
            // 正解した時のみ入力した文字を消去する
            updateUserGuess("")
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = true
                )
            }
        }
    }

    /**
     * スコアを更新し、現在の単語カウントを増分する
     */
    private fun updateGameState(updatedScore: Int) {
        if (uiState.value.currentWordCount == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    currentWord = pickRandomWordAndShuffle(),
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }
    }

    /**
     * 単語をスキップする
     */
    fun skipWord() {
        // スコアは変えずに単語を変える
        updateGameState(_uiState.value.score)
        // 単語入力欄を空にする
        updateUserGuess("")
    }

    /**
     * isShowAnswerのフラグを更新
     */
    fun updateIsShowAnswer(isShow: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isShowAnswer = isShow
            )
        }
    }
}