package com.mason.utils

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
     * @param filepath 文件夹路径
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun readDirs(filepath: String): List<String> {
      val fileList = ArrayList<String>()
      try {
        val dic = File(filepath)
        if (!dic.isDirectory) {
          println("输入的参数应该为[文件夹名]")
          println("filepath: " + dic.absolutePath)
        } else {
          val filelist = dic.list()
          for (i in filelist.indices) {
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
  }
}

fun main(args: Array<String>) {

}