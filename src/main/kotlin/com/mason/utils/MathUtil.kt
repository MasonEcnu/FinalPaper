package com.mason.utils

import kotlin.experimental.xor

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
     * 计算两字符串异或
     */
    fun xor(s1: String, s2: String): String {
      if (s1.length != s2.length) return ""
      val cs1 = s1.toCharArray()
      val cs2 = s2.toCharArray()
      val len = cs1.size
      val cs = CharArray(len)
      (0 until len).forEach {
        cs[it] = (cs1[it].toInt() xor cs2[it].toInt()).toChar()
      }
      return String(cs)
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
  val s1 = "hell,1"
  val s2 = "world2"
  val cs = MathUtil.xor(s1, s2)
  val result = MathUtil.xor(cs, s2)
  println(result)
}