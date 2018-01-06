package com.mason.keyword

import com.mason.models.KeywordNode
import com.mason.utils.FileUtil
import java.io.IOException

object Main {

  private val path = "F:/CP-VABKS/files"
  @Throws(IOException::class)
  @JvmStatic
  fun main(args: Array<String>) {
    val normal = FileUtil.NormalTFOfAll(path)
    for (filename in normal.keys) {
      println("fileName " + filename)
      println("TF " + normal[filename].toString())
    }
    println("—————————————–")
    val notNarmal = FileUtil.tfOfAll(path)
    for (filename in notNarmal.keys) {
      println("fileName " + filename)
      println("TF " + notNarmal[filename].toString())
    }
    println("—————————————–")
    val idf = FileUtil.idf(path)
    // 创建一个关键字节点List
    // List<KeywordNode> nodesList = new ArrayList<>();
    // 创建一个KeywordNode数组
    val nodesArray = arrayOfNulls<KeywordNode>(idf.size)
    // 数组下标
    for ((index, word) in idf.keys.withIndex()) {
      // 将每一个关键字与idf值作为KeywordNode对象添加到List中
      // nodesList.add(new KeywordNode(word, idf.get(word)));
      // 将每一个关键字与idf值作为KeywordNode对象存到数组中
      nodesArray[index] = KeywordNode(word, idf[word]?.toDouble() ?: 0.0)
      println("keyword :" + word + " idf:  " + idf[word])
    }
    // 对nodesArray进行排序
    println("—————————————–")
    val tfidf = FileUtil.tfidf(path)
    for (filename in tfidf.keys) {
      println("fileName " + filename)
      println(tfidf[filename])
    }
  }
}
