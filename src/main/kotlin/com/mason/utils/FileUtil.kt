package com.mason.utils

import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import java.io.*
import java.nio.charset.Charset
import java.util.ArrayList


/**
 * 文件读取和写入工具类
 */
class FileUtil {

  companion object {

    /**
     * 读取文件夹下的所有文件的绝对路径
     * @param path 文件夹路径
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun readDirs(path: String): List<String> {
      val fileList = ArrayList<String>()
      try {
        val dic = File(path)
        if (!dic.isDirectory) {
          println("输入的参数应该为[文件夹名]")
          println("filepath: " + dic.absolutePath)
        } else {
          val filelist = dic.list()
          for (i in filelist.indices) {
            val readfile = File(path + "\\" + filelist[i])
            if (!readfile.isDirectory) {
              fileList.add(readfile.absolutePath)
            } else if (readfile.isDirectory) {
              readDirs(path + "\\" + filelist[i])
            }
          }
        }
      } catch (e: FileNotFoundException) {
        println(e.message)
      }
      return fileList
    }

    /**
     * 读取单个文件
     * @param path 文件路径
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun readFiles(path: String): String {
      val sb = StringBuffer()
      val `is` = InputStreamReader(FileInputStream(path), Charset.forName("UTF-8"))
      val br = BufferedReader(`is`)
      var line: String? = br.readLine()
      while (line != null) {
        sb.append(line).append("\r\n")
        line = br.readLine()
      }
      br.close()
      return sb.toString()
    }

    /**
     * 读取单个文件
     * @param path 文件路径
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun readDirFiles(path: String): Map<String, String> {
      val dirs = readDirs(path)
      val results = mutableMapOf<String, String>()
      dirs.forEach {
        results.put(getFileName(it), readFiles(it))
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
  }
}

fun main(args: Array<String>) {
  FileUtil.readDirFiles(ORIGINAL_FILES_DIC_PATH).forEach { key, value ->
    println("$key --> ${value.substring(0, 100)}")
  }
}