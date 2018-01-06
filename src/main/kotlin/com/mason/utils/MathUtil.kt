package com.mason.utils

class MathUtil {
  companion object {
    /**
     * 根据进制计算需要多少位二进制表示
     */
    fun calDigit(radix: Int): Int {
      var digit = Math.log(radix.toDouble()) / Math.log(2.toDouble())
      digit = if (digit.rem(1) == 0.toDouble()) digit else (digit.toInt() + 1).toDouble()
      return digit.toInt()
    }

    /**
     * 计算给定两个二进制字符串的亦或结果
     */
    fun xor(s1: String, s2: String): String {
      if (s1.length != s2.length) return ""
      val len = s1.length
      val sb = StringBuilder()
      (0 until len).forEach {
        sb.append(s1[it].toInt() xor s2[it].toInt())
      }
      return sb.toString()
    }

    /**
     * 计算log以2为底的对数
     */
    fun log2(n: Int): Int = (Math.log(n.toDouble()) / Math.log(2.toDouble())).toInt()

    /**
     * 计算任意底的对数值
     */
    fun log(value: Float, base: Float): Float = (Math.log(value.toDouble()) / Math.log(base.toDouble())).toFloat()
  }
}

fun main(args: Array<String>) {
  println("010011010110010101110011011100110110000101100111011001010010000001101111011001100010000001100100011011110110001101110101011011010110010101101110011101000010000000110001".length)
}