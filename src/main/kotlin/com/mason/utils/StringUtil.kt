package com.mason.utils

import com.mason.constants.CHARS
import com.mason.constants.CHARS_LENGTH
import com.mason.constants.MAX_FILE_LENGTH
import com.mason.constants.SEPARATOR
import java.util.*

class StringUtil {
  companion object {

    private val RANDOM = Random()

    /**
     * 填充
     */
    fun padding(source: String): MutableList<String> {
      val len = if (source.isBlank()) 0 else source.length
      return when {
        len == MAX_FILE_LENGTH -> mutableListOf(source)
        len > MAX_FILE_LENGTH -> split(source, MAX_FILE_LENGTH)
        else -> // len < MAX_FILE_LENGTH
          mutableListOf(source + genRandomString(MAX_FILE_LENGTH - len))
      }
    }

    /**
     * 拆分
     */
    private fun split(source: String, maxLength: Int): MutableList<String> {
      val len = source.length
      val n = if (len % maxLength == 0) len / maxLength else len / maxLength + 1
      val result = mutableListOf<String>()
      (0 until n).forEach {
        val start = it * maxLength
        val end = start + maxLength
        var str = source.substring(start, if (end > len) len else end)
        if (str.length < maxLength) str += genRandomString(maxLength - str.length)
        result.add(str)
      }
      return result
    }

    /**
     * 找到可打印的所有字符
     */
    private fun getPrintChars(): String {
      return StringBuilder().apply {
        (0..127).forEach {
          if (it in 32..126) {
            append(it.toChar())
          }
        }
      }.toString()
    }

    /**
     * 生成指定长度的随机字符串
     */
    fun genRandomString(n: Int): String {
      val ran = Random()
      return StringBuilder().apply {
        (0 until n).forEach {
          append(CHARS[ran.nextInt(CHARS_LENGTH)])
        }
      }.toString()
    }

    /**
     * 转化字符串为十六进制编码
     * @param str
     * @return
     */
    fun str2Hex(str: String): String {
      val sb = StringBuilder()
      (0 until str.length)
          .map { str[it].toInt() }
          .forEach {
            val temp = Integer.toHexString(it)
            if (temp.length == 1) sb.append("0")
            sb.append(temp)
          }
      return sb.toString()
    }

    /**
     * 转化十六进制串为字符串
     * @param hex
     * @return
     */
    fun hex2Str(hex: String): String {
      val sb = StringBuilder()
      for (i in 0 until hex.length / 2) {
        sb.append(Integer.valueOf(hex.substring(i * 2, i * 2 + 2),
            16).toChar())
      }
      return sb.toString()
    }

    private val binaryArray = arrayOf("0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111")
    private val hexStr = "0123456789abcdef"

    /**
     * 转化十六进制串为二进制
     * @param hex
     * @return
     */
    fun hex2Binary(hex: String): String {
      val result = StringBuilder()
      hex.forEach {
        result.append(binaryArray[hexStr.indexOf(it)])
      }
      return result.toString()
    }

    /**
     * 二进制转十六进制
     */
    fun binary2Hex(binary: String): String {
      val digit = 8
      val len = binary.length
      var _binary = binary
      if (len % digit != 0) _binary = StringBuilder().apply {
        (0 until (len / digit + 1) * digit - len).forEach {
          append(0)
        }
      }.append(binary).toString()
      val strList = split(_binary, 4)
      val result = StringBuilder()
      (0 until strList.size step 2).forEach {
        // 高位
        result.append(hexStr[binaryArray.indexOf(strList[it])])
        // 低位
        result.append(hexStr[binaryArray.indexOf(strList[it + 1])])
      }
      return result.toString()
    }

    /**
     * 将source转为二进制串（通用中英文及标点和特殊符号）
     */
    fun c_toBinary(source: String): String {
      val bytes = source.toByteArray()
      val sb = StringBuilder()
      bytes.forEach {
        if (it == bytes.last()) {
          sb.append(Integer.toBinaryString(it.toInt()))
        } else {
          sb.append(Integer.toBinaryString(it.toInt())).append(SEPARATOR)
        }
      }
      return sb.toString()
    }

    /**
     * source为二进制串
     * 转为一般字符串
     */
    fun c_toString(source: String): String {
      val binaryArray = source.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      val byteArray = ByteArray(binaryArray.size)
      for (i in byteArray.indices) {
        byteArray[i] = parse(binaryArray[i]).toByte()
      }
      return String(byteArray)
    }

    private fun parse(str: String): Int {
      var source = str
      //32位 为负数
      if (32 == str.length) {
        source = "-" + source.substring(1)
        return -(Integer.parseInt(source, 2) + Integer.MAX_VALUE + 1)
      }
      return Integer.parseInt(str, 2)
    }

    /**
     * 随机生成指定位数的0-1字符串
     */
    fun randomBinaryString(n: Int): String {
      val sb = StringBuilder()
      (0 until n).forEach {
        sb.append(if (RANDOM.nextBoolean()) "1" else "0")
      }
      return sb.toString()
    }

    /**
     * 根据已有字符串生成指定长度的字符串
     */
    fun randomBinaryString(source: String, n: Int): String {
      val len = source.length
      return when {
        n == len -> source
        n < len -> source.substring(len - n, len)
        else -> {
          val sb = StringBuilder()
          (0 until len % n + 1).forEach {
            sb.append(source)
          }
          sb.toString().substring(0, n)
        }
      }
    }
  }
}

fun main(args: Array<String>) {
  println(StringUtil.randomBinaryString("10", 5))

//  val start = System.currentTimeMillis()
//  val msg = "Life doesn't always give us the joys we want. We don't always get our hopes and dreams, and we don't always get our own way. But do not give up hope, because you can make a difference one situation and one person at a time."
//  // 计算msg的二进制字符串
//  val msg_binary = StringUtil.hex2Binary(StringUtil.str2Hex(msg))
//  val h4 = StringUtil.randomBinaryString(msg_binary.length)
//  val u3 = MathUtil.xor(msg_binary, h4)
//  println("msg: $msg")
//  println("msg_binary: $msg_binary")
//  println("h4: $h4")
//  println("u3: $u3")
//  val _msg_binary = MathUtil.xor(u3, h4)
//  println("_msg_binary: $_msg_binary")
//  val _msg = StringUtil.hex2Str(StringUtil.binary2Hex(_msg_binary))
//  println("_msg: $_msg")
//  val end = System.currentTimeMillis()
//  println("cost: ${end - start}ms")
}