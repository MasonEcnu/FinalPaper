package com.mason.utils

import com.mason.constants.FORWARD_INDEX_PATH
import java.io.*
import java.nio.charset.Charset
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.FileInputStream


/**
 * 文件读取和写入工具类
 */
class FileUtil {

  companion object {

    private val filesList = mutableListOf<String>()

    /**
     * 读取文件夹下的所有文件的绝对路径
     * @param path 文件夹路径
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun readDirs(path: String): List<String> {
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
              filesList.add(readfile.absolutePath)
            } else if (readfile.isDirectory) {
              readDirs(path + "\\" + filelist[i])
            }
          }
        }
      } catch (e: FileNotFoundException) {
        println(e.message)
      }
      return filesList
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
     * 读取单个文件--生成文件名与内容对应的map
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

    /**
     * 将一个对象写入到文件中
     * @param obj 待写入的对象
     * @param path 写入路径--文件
     */
    fun writeObject2File(obj: Any, path: String) {
      val file = File(path)
      if (!file.exists()) file.createNewFile()
      val out: FileOutputStream
      try {
        out = FileOutputStream(file)
        val objOut = ObjectOutputStream(out)
        objOut.writeObject(obj)
        objOut.flush()
        objOut.close()
        println("write object success!")
      } catch (e: IOException) {
        println("write object failed")
        e.printStackTrace()
      }
    }

    /**
     * 将一个对象写入到文件中
     * @param path 写入路径--文件
     */
    fun readObjectFromFile(path: String): Any? {
      var temp: Any? = null
      val file = File(path)
      val fin: FileInputStream
      try {
        fin = FileInputStream(file)
        val objIn = ObjectInputStream(fin)
        temp = objIn.readObject()
        objIn.close()
        println("read object success!")
      } catch (e: IOException) {
        println("read object failed")
        e.printStackTrace()
      } catch (e: ClassNotFoundException) {
        e.printStackTrace()
      }
      return temp
    }
  }
}

fun main(args: Array<String>) {
  val list = listOf<String>("123", "456", "789")
  val map = mapOf("1" to list)
  FileUtil.writeObject2File(map, FORWARD_INDEX_PATH)
  val obj = FileUtil.readObjectFromFile(FORWARD_INDEX_PATH) as Map<*, *>
  obj.forEach {
    println(it.toString())
  }
}