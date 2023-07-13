package com.example.unscramble.ui

/**
 * ゲームの表示に影響する状態を保持
 */
data class GameUiState(
    val currentWord: Word,
    val isGuessedWordWrong: Boolean = false,
    val score: Int = 0,
    val currentWordCount: Int = 1,
    val isGameOver: Boolean = false,
    val isShowAnswer:Boolean = false
)


/**
 * 単語の状態を保持
 */
data class Word(
    val answer: String,
    val shuffleWords: String = answer.shuffle(),
    var isUsed: Boolean = false
)

fun String.shuffle(): String {
    val resultValue = this.toCharArray()
    resultValue.shuffle()
    return String(resultValue)
}
