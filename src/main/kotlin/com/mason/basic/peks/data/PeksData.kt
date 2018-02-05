package com.mason.basic.peks.data

import com.mason.constants.FORWARD_INDEX_PATH
import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import com.mason.constants.TEST_FILES_DIC_PATH
import com.mason.keyword.KeywordsExtractor
import com.mason.utils.FileUtil

class PeksData {
  companion object {
    val Doc2Word = FileUtil.readObjectFromFile(FORWARD_INDEX_PATH).let {
      it as Map<*, *>
      val results = mutableMapOf<String, List<String>>()
      it.forEach { key, value ->
        val wordList = mutableListOf<String>()
        key as String
        value as List<*>
        value.forEach {
          it as String
          wordList.add(it)
        }
        results.put(key, wordList)
      }
      results
    }

    val Msg2Word = mapOf<String, List<String>>(
        "Message of document 1" to mutableListOf("cat", "hat", "mat"),
        "Message of document 2" to mutableListOf("you", "bar", "mar"),
        "Message of document 3" to mutableListOf("like", "love", "hehe"),
        "Message of document 4" to mutableListOf("cat", "I", "he"),
        "Message of document 5" to mutableListOf("China", "Japan", "America")
    )
  }
}

fun main(args: Array<String>) {
  PeksData.Doc2Word.forEach { key, value ->
    println("$key --> ${value.size}")
  }
}