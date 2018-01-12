package com.mason.utils

import com.mason.hash.MurmurHash3
import java.util.*

class HashUtil {
  companion object {
    fun hash32(value: Int): Int {
      val result = MurmurHash3.fmix32(value)
      if (result > 0) return -result
      return result
    }

    fun hash64(value: Long): Long {
      val result = MurmurHash3.fmix64(value)
      if (result > 0) return -result
      return result
    }

    fun hash32(input: String): Int {
      val bytes = input.toByteArray()
      val result = MurmurHash3.murmurhash3_x86_32(bytes, 0, bytes.size, Arrays.hashCode(bytes))
      if (result > 0) return -result
      return result
    }

    fun hash32(input: CharSequence): Int {
      val result = MurmurHash3.murmurhash3_x86_32(input, 0, input.length, input.hashCode())
      if (result > 0) return -result
      return result
    }

    fun hash64(input: String): Long {
      val bytes = input.toByteArray()
      val result = MurmurHash3.murmurhash3_x64_128(bytes, 0, bytes.size, Arrays.hashCode(bytes)).val1
      if (result > 0) return -result
      return result
    }

    fun hash128(input: String): MurmurHash3.LongPair {
      val bytes = input.toByteArray()
      val result = MurmurHash3.murmurhash3_x64_128(bytes, 0, bytes.size, Arrays.hashCode(bytes))
      if (result.val1 > 0) result.val1 = -result.val1
      if (result.val2 > 0) result.val2 = -result.val2
      return result
    }

    fun toBinaryString(value: Int): String {
      if (value > 0) return Integer.toBinaryString(-value)
      return Integer.toBinaryString(value)
    }

    fun toBinaryString(value: Long): String {
      if (value > 0) return java.lang.Long.toBinaryString(-value)
      return java.lang.Long.toBinaryString(value)
    }

    fun toBinaryString(value: MurmurHash3.LongPair): String {
      if (value.val1 > 0) value.val1 = -value.val1
      if (value.val2 > 0) value.val2 = -value.val2
      return java.lang.Long.toBinaryString(value.val1) + java.lang.Long.toBinaryString(value.val2)
    }
  }
}

fun main(args: Array<String>) {
//  val msg = "Hello World!"
//  val len = StringUtil.hex2Binary(StringUtil.str2Hex(msg)).length.let { println(it) }
  println((-2) * (-10))
}
