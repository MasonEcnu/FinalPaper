package com.mason.basic.spks

import com.mason.models.*
import com.mason.basic.spks.basic.BasicScheme
import com.mason.basic.spks.data.Msg2Word
import com.mason.proxy.TimeCountProxyHandle
import com.mason.utils.HashUtil
import com.mason.utils.MathUtil
import com.mason.utils.StringUtil
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import java.lang.reflect.Proxy

class SPKS : BasicScheme {

  companion object {
    val pairing: Pairing = PairingFactory.getPairing("config/a.properties")
    // rho的长度
    val n_rho = MathUtil.log2(512)

    init {
      PairingFactory.getInstance().isUsePBCWhenPossible = true
    }
  }

  override fun setup(): Param = Param(pairing)

  override fun keyGen(param: Param): KeyPair {
    val sk = param.Zr.newRandomElement()
    val pk = param.g.duplicate().powZn(sk)
    return KeyPair(sk, pk)
  }

  override fun enc(docs: Map<String, List<String>>, pk_u: Element, pk_c: Element, param: Param): List<SpksCipher> {
    val results = mutableListOf<SpksCipher>()
    docs.keys.forEach {
      val msg = it
      val msg_binary = StringUtil.hex2Binary(StringUtil.str2Hex(msg))
      val words = docs[it]
      // 二进制数的长度
      val n = msg.length * 8
      // 随机元素r
      val r = param.Zr.newRandomElement()
      // 1. 计算u1
      val u1 = param.g.duplicate().powZn(r)
      // 2.计算u2
      val rho = StringUtil.randomBinaryString(n_rho)
      val h5_in = pairing.pairing(pk_u, pk_c).duplicate().powZn(r)
      val h5_hash = HashUtil.toBinaryString(HashUtil.hash32(h5_in.toString()))
      val h5 = StringUtil.randomBinaryString(h5_hash, n_rho)
      val u2 = MathUtil.xor(rho, h5)
      // 3.计算u3
      val h4_in = pairing.pairing(param.G1.newElementFromHash(rho.toByteArray(), 0, n_rho), pk_u.duplicate().powZn(r))
      val h4_hash = HashUtil.toBinaryString(HashUtil.hash32(h4_in.toString()))
      val h4 = StringUtil.randomBinaryString(h4_hash, n)
      val u3 = MathUtil.xor(msg_binary, h4)
      // 4.计算cm
      val cm = Triple(u1, u2, u3)
      // 5.依次计算cw
      val cwk = mutableListOf<String>()
      words?.forEach {
        val wbs = it.toByteArray()
        val h1 = param.G1.newElementFromHash(wbs, 0, wbs.size)
        val h2_in = pairing.pairing(pk_u, h1.duplicate().powZn(r))
        val h2_hash = HashUtil.toBinaryString(HashUtil.hash32(h2_in.toString()))
        val h2 = StringUtil.randomBinaryString(h2_hash, n_rho)
        cwk.add(h2)
      }
      results.add(SpksCipher(cm, cwk, param.G1.newOneElement()))
    }
    return results
  }

  override fun tCompute(word: String, sk_u: Element, param: Param): Element {
    val wbs = word.toByteArray()
    val h1 = param.G1.newElementFromHash(wbs, 0, wbs.size)
    return h1.duplicate().powZn(sk_u)
  }

  override fun kwTest(pk_u: Element, ciphers: List<SpksCipher>, tw: Element, param: Param): List<SpksCipher> {
    val results = mutableListOf<SpksCipher>()
    ciphers.forEach {
      val cipher = it
      val h2_in = pairing.pairing(cipher.cm.first, tw)
      val h2_hash = HashUtil.toBinaryString(HashUtil.hash32(h2_in.toString()))
      val h2 = StringUtil.randomBinaryString(h2_hash, n_rho)
      cipher.cwk.forEach {
        if (h2 == it) results.add(cipher)
      }
    }
    return results
  }

  override fun pDecrypt(pk_u: Element, sk_c: Element, ciphers: List<SpksCipher>, param: Param): List<SpksCipher> {
    return ciphers.apply {
      forEach {
        val h5_in = pairing.pairing(pk_u, it.cm.first).duplicate().powZn(sk_c)
        val h5_hash = HashUtil.toBinaryString(HashUtil.hash32(h5_in.toString()))
        val h5 = StringUtil.randomBinaryString(h5_hash, n_rho)
        val rho = MathUtil.xor(it.cm.second, h5)
        val rho_bytes = rho.toByteArray()
        val crho = pairing.pairing(param.G1.newElementFromHash(rho_bytes, 0, rho_bytes.size), it.cm.first)
        it.crho = crho
      }
    }
  }

  override fun recovery(ciphers: List<SpksCipher>, sk_u: Element, param: Param): List<String> {
    val results = mutableListOf<String>()
    ciphers.forEach {
      val h4_in = it.crho.duplicate().powZn(sk_u)
      val h4_hash = HashUtil.toBinaryString(HashUtil.hash32(h4_in.toString()))
      val h4 = StringUtil.randomBinaryString(h4_hash, it.cm.third.length)
      results.add(MathUtil.xor(h4, it.cm.third))
    }
    return results
  }
}

fun main(args: Array<String>) {
//  val spks = SPKS()
//  println("开始！")
//  // 系统初始化
//  var start = System.currentTimeMillis()
//  val param = spks.setup()
//  var end = System.currentTimeMillis()
//  println("系统参数生成完毕： ${end - start}ms")
//  // 密钥对生成
//  start = System.currentTimeMillis()
//  val user = spks.keyGen(param)
//  end = System.currentTimeMillis()
//  println("用户密钥对生成完毕： ${end - start}ms")
//  start = System.currentTimeMillis()
//  val csp = spks.keyGen(param)
//  end = System.currentTimeMillis()
//  println("服务器密钥对生成完毕： ${end - start}ms")
//  // 加密明文和关键字
//  start = System.currentTimeMillis()
//  val ciphers = spks.enc(Msg2Word, user.pk, csp.pk, param)
//  end = System.currentTimeMillis()
//  println("加密完毕： ${end - start}ms")
//  // 生成陷门
//  start = System.currentTimeMillis()
//  val tw = spks.tCompute("cat", user.sk, param)
//  end = System.currentTimeMillis()
//  println("陷门生成完毕： ${end - start}ms")
//  // 关键字检索
//  start = System.currentTimeMillis()
//  val search_results = spks.kwTest(user.pk, ciphers, tw, param)
//  end = System.currentTimeMillis()
//  println("关键字检索完毕： ${end - start}ms")
//  // 部分解密
//  start = System.currentTimeMillis()
//  val p_results = spks.pDecrypt(user.pk, csp.sk, search_results, param)
//  end = System.currentTimeMillis()
//  println("部分解密完毕： ${end - start}ms")
//  // 完全解密
//  start = System.currentTimeMillis()
//  val results = spks.recovery(p_results, user.sk, param)
//  end = System.currentTimeMillis()
//  println("完全解密完毕： ${end - start}ms")
//  println("解密结果：")
//  results.forEach {
//    println(StringUtil.hex2Str(StringUtil.binary2Hex(it)))
//  }

  val spks = SPKS()
  // 动态代理，统计各个方法耗时
  val spksProxy = Proxy.newProxyInstance(
      spks::class.java.classLoader,
      spks::class.java.interfaces, TimeCountProxyHandle(spks)) as BasicScheme

  val param = spksProxy.setup()
  val user = spksProxy.keyGen(param)
  val csp = spksProxy.keyGen(param)
  val ciphers = spksProxy.enc(Msg2Word, user.pk, csp.pk, param)
  val tw = spksProxy.tCompute("cat", user.sk, param)
  val search_results = spksProxy.kwTest(user.pk, ciphers, tw, param)
  val p_results = spksProxy.pDecrypt(user.pk, csp.sk, search_results, param)
  val results = spksProxy.recovery(p_results, user.sk, param)
  println("解密结果：")
  results.forEach {
    println(StringUtil.hex2Str(StringUtil.binary2Hex(it)))
  }
}