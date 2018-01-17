package com.mason.keyword

import com.mason.constants.MAX_KEYWORDS_COUNT
import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import com.mason.utils.StringUtil
import com.mason.utils.TfidfUtil
import java.io.IOException
import java.util.*
import java.util.HashMap
import com.hankcs.hanlp.HanLP
import com.mason.utils.FileUtil


class ExtractKeywords {
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
          val fileName = getFileName(it)
          if (temp.size >= MAX_KEYWORDS_COUNT) results.put(fileName, temp.subList(0, MAX_KEYWORDS_COUNT))
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
      if (map_filter.size <= MAX_KEYWORDS_COUNT) return map_filter.keys.toList()
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

    // 获取不带文件夹和后缀的文件名
    fun getFileName(filename: String): String {
      if (filename.indexOf("\\") == -1) return ""
      if (filename.indexOf(".") == -1) return ""
      val start = filename.lastIndexOf("\\")
      val end = filename.lastIndexOf(".")
      return filename.substring(start + 1, end)
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
        val fileName = getFileName(it)
        val keywords = HanLP.extractKeyword(content, MAX_KEYWORDS_COUNT)
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
  }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
  val path = ORIGINAL_FILES_DIC_PATH
  val results = ExtractKeywords.invertedIndexByTextRank(path)
  results.forEach { key, value ->
    println("$key --> $value")
  }
}