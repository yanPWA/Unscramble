package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    // StateFlowをGameUiStateから公開
    private var _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // 現在のスクランブルされた単語を保存
    private lateinit var currentWord: String

    // ゲームで使用済みの単語を保存
    private var usedWords: MutableSet<String> = mutableSetOf()

    // ユーザーが入力した単語を保存
    var userGuess by mutableStateOf("")
        private set

    init {
        resetGame()
    }

    /**
     * リストからランダムな単語を選択してシャッフルする
     */
    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        return if (usedWords.contains(currentWord)) {
            pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    /**
     * 単語をシャッフルする
     */
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        // シャッフル下結果が元々の単語と違う単語になるまでシャッフルする
        while (String(tempWord) == word) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    /**
     * ゲームをリセットする
     */
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    /**
     * ユーザーの推測と正解を照合する
     */
    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = true
                )
            }
        }
        updateUserGuess("")
    }

    /**
     * スコアを更新し、現在の単語カウントを増分する
     */
    private fun updateGameState(updatedScore: Int) {
        // もしくはusedWords.size == MAX_NO_OF_WORDS
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
                    currentScrambledWord = pickRandomWordAndShuffle(),
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
}