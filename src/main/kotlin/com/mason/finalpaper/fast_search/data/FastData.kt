package com.mason.finalpaper.fast_search.data

import com.mason.constants.INVERTED_INDEX_PATH
import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import com.mason.constants.TEST_FILES_DIC_PATH
import com.mason.keyword.KeywordsExtractor
import com.mason.utils.FileUtil

class FastData {
  companion object {
    val Documents = FileUtil.readDirFiles(ORIGINAL_FILES_DIC_PATH)
    //    val Word2Doc = KeywordsExtractor.invertedIndexByGivenCount(TEST_FILES_DIC_PATH, 20)
    val Word2Doc = FileUtil.readObjectFromFile(INVERTED_INDEX_PATH).let {
      it as Map<*, *>
      val results = mutableMapOf<String, List<String>>()
      it.forEach { key, value ->
        val docList = mutableListOf<String>()
        key as String
        value as List<*>
        value.forEach {
          it as String
          docList.add(it)
        }
        results.put(key.trim(), docList)
      }
      results
    }
  }
}

fun main(args: Array<String>) {
//  FastData.Documents.forEach { key, value ->
//    println("$key --> ${value.substring(0, 20)}")
//  }
  FastData.Word2Doc.forEach { key, value ->
    println("$key --> ${value.size}")
  }
}