package com.discarded

import com.mason.constants.DECRYPTED_FILES_DIC_PATH
import com.mason.constants.FolderType
import com.mason.constants.SPLICED_FILES_DIC_PATH
import com.mason.models.MyFile
import com.mason.utils.StringUtil
import java.io.*

/**
 * 文件读取和写入工具类
 */
class DiscardedFileUtil {

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
      val sources = readFolderOrFile(from, folderType)
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
  }
}

fun main(args: Array<String>) {
  // 文件拆分
//  FileUtil.readFolderOrFile(ORIGINAL_FILES_DIC_PATH, FolderType.FILE).forEach {
//    FileUtil.writeFile(PROCESSED_FILES_DIC_PATH, it, FolderType.FILE)
//  }
  // 文件拼接
  DiscardedFileUtil.splice(DECRYPTED_FILES_DIC_PATH, SPLICED_FILES_DIC_PATH, FolderType.SPLICED)
  DiscardedFileUtil.release()
}