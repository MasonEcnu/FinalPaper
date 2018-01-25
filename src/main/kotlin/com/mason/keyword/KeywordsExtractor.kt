package com.mason.keyword

import com.mason.utils.StringUtil
import com.mason.utils.TfidfUtil
import java.io.IOException
import java.util.*
import java.util.HashMap
import com.hankcs.hanlp.HanLP
import com.mason.constants.*
import com.mason.utils.FileUtil


class KeywordsExtractor {
  companion object {

    /**
     * 构建正向索引--采用tfidf的方式提取关键字
     * @param path 文档所在的文件夹路径
     */
    fun forwardIndexByTFIDF(path: String): Map<String, List<String>> {
      val results = mutableMapOf<String, List<String>>()
      TfidfUtil.NormalTFOfAll(path)
      TfidfUtil.tfOfAll(path)
      TfidfUtil.idf(path)
      val tfidf = TfidfUtil.tfidf(path)
      tfidf.keys.forEach {
        val doc = tfidf[it]
        if (doc != null) {
          val temp = mapSort(doc)
          val fileName = FileUtil.getFileName(it)
          if (temp.size >= MAX_KEYWORDS_COUNT_SINGLE) results.put(fileName, temp.subList(0, MAX_KEYWORDS_COUNT_SINGLE))
          else results.put(fileName, temp)
        }
      }
      return results
    }

    // 将map中的entries对value排序,并输出value最大的前MAX_KEYWORDS_COUNT个
    private fun mapSort(map: HashMap<String, Float>): List<String> {
      val map_filter = map.filter {
        !StringUtil.isNumeric(it.key)
      }
      if (map_filter.size <= MAX_KEYWORDS_COUNT_SINGLE) return map_filter.keys.toList()
      val results = mutableListOf<String>()
      val infoIds = map_filter.entries.toList()
      Collections.sort(infoIds, { o1, o2 ->
        o1.toString().compareTo(o2.toString())
      })
      infoIds.forEach {
        results.add(it.key)
      }
      return results
    }

    /**
     * 构建反向索引--采用tfidf的方式提取关键字
     * @param path 文档所在的文件夹路径
     */
    fun invertedIndexByTFIDF(path: String): Map<String, List<String>> {
      val forwardIndex = forwardIndexByTFIDF(path)
      val results = mutableMapOf<String, MutableList<String>>()
      forwardIndex.forEach { key, value ->
        value.forEach {
          if (results.containsKey(it)) {
            results[it]?.add(key)
          } else {
            results.put(it, mutableListOf(key))
          }
        }
      }
      return results
    }

    /**
     * 构建正向索引--采用TextRank的方式提取关键字
     * @param path 文档所在的文件夹路径
     */
    fun forwardIndexByTextRank(path: String): Map<String, List<String>> {
      val results = mutableMapOf<String, List<String>>()
      val files = FileUtil.readDirs(path)
      files.forEach {
        val content = FileUtil.readFiles(it)
        val fileName = FileUtil.getFileName(it)
        val keywords = HanLP.extractKeyword(content, MAX_KEYWORDS_COUNT_SINGLE)
        results.put(fileName, keywords)
      }
      return results
    }

    /**
     * 构建反向索引--采用TextRank的方式提取关键字
     * @param path 文档所在的文件夹路径
     */
    fun invertedIndexByTextRank(path: String): Map<String, List<String>> {
      val forwardIndex = forwardIndexByTextRank(path)
      val results = mutableMapOf<String, MutableList<String>>()
      forwardIndex.forEach { key, value ->
        value.forEach {
          if (results.containsKey(it)) {
            results[it]?.add(key)
          } else {
            results.put(it, mutableListOf(key))
          }
        }
      }
      return results
    }

    /**
     * 根据需要的个数选定关键字
     * 实际上还是构建了一个倒排索引
     * @param path 文件夹路径
     * @param count 需要的关键字个数
     */
    fun invertedIndexByGivenCount(path: String, count: Int): Map<String, List<String>> {
      val map = invertedIndexByTextRank(path)
      val map_filter = map.filter {
        !StringUtil.isNumeric(it.key)
      }
      if (map_filter.size <= count) return map_filter
      val results = mutableMapOf<String, List<String>>()
      val infoIds = map_filter.entries.toList()
      Collections.sort(infoIds, { o1, o2 ->
        o2.value.size.compareTo(o1.value.size)
      })
      infoIds.subList(0, count).forEach {
        results.put(it.key, it.value)
      }
      return results
    }

    /**
     * 根据指定的个数构建正向索引
     * @param path 文件夹路径
     * @param count 需要的关键字个数
     */
    fun forwardIndexByGivenCount(path: String, count: Int): Map<String, List<String>> {
      val results = mutableMapOf<String, MutableList<String>>()
      val invertedIndex = invertedIndexByGivenCount(path, count)
      invertedIndex.forEach { key, value ->
        value.forEach {
          if (results.containsKey(it)) {
            results[it]?.add(key)
          } else {
            results.put(it, mutableListOf(key))
          }
        }
      }
      return results
    }
  }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
//  val start = System.currentTimeMillis()
//  val results = KeywordsExtractor.invertedIndexByGivenCount(path, MAX_KEYWORDS_COUNT_TOTAL)
//  val end = System.currentTimeMillis()
//  println("time cost: ${end - start}")
//  println(results.size)
//  results.forEach { key, value ->
//    println("$key --> ${value.size} --> $value")
//  }
//  FileUtil.writeObject2File(KeywordsExtractor.invertedIndexByGivenCount(ORIGINAL_FILES_DIC_PATH, MAX_KEYWORDS_COUNT_TOTAL), INVERTED_INDEX_PATH)
//  FileUtil.writeObject2File(KeywordsExtractor.forwardIndexByGivenCount(ORIGINAL_FILES_DIC_PATH, MAX_KEYWORDS_COUNT_TOTAL), FORWARD_INDEX_PATH)
  val start: Long
  val end: Long

//  start = System.currentTimeMillis()
//  val forward = FileUtil.readObjectFromFile(FORWARD_INDEX_PATH) as Map<*, *>
//  end = System.currentTimeMillis()
//  println(forward.size)
//  println("time cost: ${end - start}")
//  forward.forEach { key, value ->
//    value as List<*>
//    println("$key --> ${value.size} --> $value")
//  }
  start = System.currentTimeMillis()
  val inverted = FileUtil.readObjectFromFile(INVERTED_INDEX_PATH) as Map<*, *>
  end = System.currentTimeMillis()
  println("time cost: ${end - start}")
  inverted.forEach { key, value ->
    value as List<*>
    println("$key --> ${value.size} --> $value")
  }
}