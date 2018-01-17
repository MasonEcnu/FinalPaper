package com.mason.utils

import jeasy.analysis.MMAnalyzer
import java.io.*
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.HashMap

// 关键字处理相关类
class TfidfUtil {
  companion object {
    // 关键字提取
    private val fileList = ArrayList<String>()

    private val allTheTf = HashMap<String, HashMap<String, Float>>()

    private val allTheNormalTF = HashMap<String, HashMap<String, Int>>()

    @Throws(FileNotFoundException::class, IOException::class)
    fun readDirs(filepath: String): List<String> {
      try {
        val file = File(filepath)
        if (!file.isDirectory) {
          println("输入的参数应该为[文件夹名]")
          println("filepath: " + file.absolutePath)
        } else if (file.isDirectory) {
          val filelist = file.list()
          for (i in filelist!!.indices) {
            val readfile = File(filepath + "\\" + filelist[i])
            if (!readfile.isDirectory) {
              fileList.add(readfile.absolutePath)
            } else if (readfile.isDirectory) {
              readDirs(filepath + "\\" + filelist[i])
            }
          }
        }
      } catch (e: FileNotFoundException) {
        println(e.message)
      }
      return fileList
    }



    @Throws(IOException::class)
    fun cutWord(file: String): Array<String> {
      val cutWordResult: Array<String>?
      val text = FileUtil.readFiles(file)
      val analyzer = MMAnalyzer()
      val tempCutWordResult = analyzer.segment(text, " ")
      cutWordResult = tempCutWordResult.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      return cutWordResult
    }

    fun tf(cutWordResult: Array<String>): HashMap<String, Float> {
      val tf = HashMap<String, Float>()
      val wordNum = cutWordResult.size
      var wordtf: Int
      for (i in 0 until wordNum) {
        wordtf = 0
        for (j in 0 until wordNum) {
          if (cutWordResult[i] !== " " && i != j) {
            if (cutWordResult[i] == cutWordResult[j]) {
              cutWordResult[j] = " "
              wordtf++
            }
          }
        }
        if (cutWordResult[i] !== " ") {
          tf.put(cutWordResult[i], (++wordtf).toFloat().div(wordNum))
          cutWordResult[i] = " "
        }
      }
      return tf
    }

    fun normalTF(cutWordResult: Array<String>): HashMap<String, Int> {
      val tfNormal = HashMap<String, Int>()
      val wordNum = cutWordResult.size
      var wordtf: Int
      for (i in 0 until wordNum) {
        wordtf = 0
        if (cutWordResult[i] !== " ") {
          for (j in 0 until wordNum) {
            if (i != j) {
              if (cutWordResult[i] == cutWordResult[j]) {
                cutWordResult[j] = " "
                wordtf++
              }
            }
          }
          tfNormal.put(cutWordResult[i], ++wordtf)
          cutWordResult[i] = " "
        }
      }
      return tfNormal
    }

    @Throws(IOException::class)
    fun tfOfAll(dir: String): Map<String, HashMap<String, Float>> {
      val fileList = readDirs(dir)
      for (file in fileList) {
        allTheTf.put(file, tf(cutWord(file)))
      }
      return allTheTf
    }

    @Throws(IOException::class)
    fun NormalTFOfAll(dir: String): Map<String, HashMap<String, Int>> {
      val fileList = readDirs(dir)
      for (i in fileList.indices) {
        allTheNormalTF.put(fileList[i], normalTF(cutWord(fileList[i])))
      }
      return allTheNormalTF
    }

    @Throws(FileNotFoundException::class, UnsupportedEncodingException::class, IOException::class)
    fun idf(dir: String): Map<String, Float> {
      println(dir)
      val idf = HashMap<String, Float>()
      val located = ArrayList<String>()
      var Dt: Float
      val D = allTheNormalTF.size.toFloat()
      val key = fileList
      val tfInIdf = allTheNormalTF
      var i = 0
      while (i < D) {
        val temp = tfInIdf[key[i]]
        if (temp != null) {
          for (word in temp.keys) {
            Dt = 1f
            if (!located.contains(word)) {
              var k = 0
              while (k < D) {
                if (k != i) {
                  val temp2 = tfInIdf[key[k]]
                  if (temp2 != null) {
                    if (temp2.keys.contains(word)) {
                      located.add(word)
                      Dt += 1
                      k++
                      continue
                    }
                  }
                }
                k++
              }
              idf.put(word, MathUtil.log((1 + D) / Dt, 10f))
            }
          }
        }
        i++
      }
      return idf
    }

    @Throws(IOException::class)
    fun tfidf(dir: String): Map<String, HashMap<String, Float>> {
      val idf = idf(dir)
      val tf = tfOfAll(dir)
      for (file in tf.keys) {
        val singelFile = tf[file]
        if (singelFile != null) {
          for (word in singelFile.keys) {
            val front = idf[word] ?: 0.0f
            val back = singelFile[word] ?: 0.0f
            singelFile.put(word, front.times(back))
          }
        }
      }
      return tf
    }
  }
}