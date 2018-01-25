package com.mason.finalpaper.slow_search.data

import com.mason.constants.FORWARD_INDEX_PATH
import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import com.mason.constants.TEST_FILES_DIC_PATH
import com.mason.keyword.KeywordsExtractor
import com.mason.utils.FileUtil

class SlowData {
  companion object {
    val Documents = FileUtil.readDirFiles(ORIGINAL_FILES_DIC_PATH)
//    val Doc2Word = KeywordsExtractor.forwardIndexByGivenCount(TEST_FILES_DIC_PATH, 20)
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
  }
}

fun main(args: Array<String>) {
  SlowData.Doc2Word.forEach { key, value ->
    println("$key --> ${value.size}")
  }
}