package com.mason.utils

import com.mason.constants.DECRYPTED_FILES_DIC_PATH
import com.mason.constants.FolderType
import com.mason.constants.SPLICED_FILES_DIC_PATH
import com.mason.models.MyFile
import jeasy.analysis.MMAnalyzer
import java.io.*
import java.util.ArrayList
import java.util.HashMap


/**
 * 文件读取和写入工具类
 */
class FileUtil {

  companion object {

    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    /**
     * 读取文件夹
     * 返回读取到的文件列表
     */
    fun readFolderOrFile(path: String, folderType: FolderType): MutableList<MyFile> {
      val result = mutableListOf<MyFile>()
      val dic = File(path)
      if (dic.exists()) {
        if (dic.isDirectory) {
          dic.listFiles().forEach {
            val name = it.name.substring(0, it.name.lastIndexOf("."))
            result.add(MyFile(name = name, type = it.extension, content = readFile(it, folderType)))
          }
        }
        if (dic.isFile) {
          val name = dic.name.substring(0, dic.name.lastIndexOf("."))
          result.add(MyFile(name = name, type = dic.extension, content = readFile(dic, folderType)))
        }
      }
      return result
    }

    /**
     * 读取文件
     */
    private fun readFile(file: File, folderType: FolderType): MutableList<String> {
      val sb = StringBuilder()
      try {
        reader = BufferedReader(FileReader(file))
        reader?.forEachLine {
          sb.append(it).append("\n")
        }
      } catch (e: IOException) {
        println(e.message)
      }
      val temp = sb.toString().substring(0, sb.toString().lastIndexOf("\n"))
      return if (folderType == FolderType.FILE) StringUtil.padding(temp) else mutableListOf(temp)
    }

    /**
     * 写单个文件
     */
    fun writeFile(path: String, mf: MyFile, folderType: FolderType) {
      writeFiles(path, mutableListOf(mf), folderType)
    }

    /**
     * 向文件夹中写多个文件
     */
    fun writeFiles(path: String, mfs: MutableList<MyFile>, folderType: FolderType) {
      val dic = File(path)
      if (dic.exists()) {
        try {
          mfs.forEach {
            if (dic.isDirectory) {
              val contents = it.content
              val name = it.name
              val type = it.type
              contents.indices.forEach {
                val t_name = if (folderType == FolderType.FILE) "${name}_$it" else name
                val file = File(dic.path + File.separator + t_name + ".$type")
                writer = BufferedWriter(FileWriter(file.apply {
                  if (exists()) delete()
                  createNewFile()
                }))
                writer?.write(contents[it])
                writer?.flush()
              }
            }
            if (dic.isFile) {
              writer = BufferedWriter(FileWriter(dic.apply {
                if (exists()) delete()
                createNewFile()
              }))
              writer?.write(it.content.first())
              writer?.flush()
            }
          }
        } catch (e: IOException) {
          println(e.message)
        }
      }
    }

    fun splice(from: String, to: String, folderType: FolderType) {
      val sources = FileUtil.readFolderOrFile(from, folderType)
      val map = mutableMapOf<String, MyFile>()
      sources.forEach<MyFile> {
        it.name = it.name.substring(0, it.name.indexOfFirst { c -> c == '_' })
        val sb = StringBuilder()
        if (map.containsKey(it.name)) {
          if (map[it.name] != null) {
            sb.append(map[it.name]?.content?.first())
            sb.append(it.content.first())
            map[it.name]?.content = mutableListOf(sb.toString())
          }
        } else {
          map.put(it.name, it)
        }
      }
      writeFiles(to, map.values.toMutableList(), folderType)
    }

    /**
     * 释放资源
     */
    @Throws(IOException::class)
    fun release() {
      if (reader != null) reader?.close()
      if (writer != null) writer?.close()
    }

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

    @Throws(FileNotFoundException::class, IOException::class)
    fun readFiles(file: String): String {
      val sb = StringBuffer()
      val `is` = InputStreamReader(FileInputStream(file), "gbk")
      val br = BufferedReader(`is`)
      var line: String? = br.readLine()
      while (line != null) {
        sb.append(line).append("\r\n")
        line = br.readLine()
      }
      br.close()
      return sb.toString()
    }

    @Throws(IOException::class)
    fun cutWord(file: String): Array<String> {
      val cutWordResult: Array<String>?
      val text = readFiles(file)
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

fun main(args: Array<String>) {
  // 文件拆分
//  FileUtil.readFolderOrFile(ORIGINAL_FILES_DIC_PATH, FolderType.FILE).forEach {
//    FileUtil.writeFile(PROCESSED_FILES_DIC_PATH, it, FolderType.FILE)
//  }
  // 文件拼接
  FileUtil.splice(DECRYPTED_FILES_DIC_PATH, SPLICED_FILES_DIC_PATH, FolderType.SPLICED)
  FileUtil.release()
}