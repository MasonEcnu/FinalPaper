package com.mason.basic.peks

import com.mason.basic.peks.basic.BasicScheme
import com.mason.basic.peks.data.PeksData
import com.mason.models.KeyPair
import com.mason.models.Param
import com.mason.models.PeksCipher
import com.mason.utils.HashUtil
import com.mason.utils.MathUtil
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import java.util.*

class PEKS : BasicScheme {

  companion object {
    val pairing: Pairing = PairingFactory.getPairing("config/a.properties")
    // H2哈希的长度
    val logp = MathUtil.log2(512)

    val Doc2Word = PeksData.Doc2Word

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

  override fun peks(pk: Element, param: Param): MutableList<PeksCipher> {
    val results = mutableListOf<PeksCipher>()
    Doc2Word.forEach { key, value ->
      val wordsCipher = mutableListOf<Pair<Element, String>>()
      value.forEach {
        val bs = it.toByteArray()
        val r = param.Zr.newRandomElement()
        val h1_ele = param.G1.newElementFromHash(bs, 0, bs.size)
        val t = pairing.pairing(h1_ele, pk.duplicate().powZn(r))
        val h2_t = HashUtil.hash64(t.toString()).toString()
        wordsCipher.add(Pair(param.g.duplicate().powZn(r), h2_t))
      }
      Collections.shuffle(wordsCipher)
      results.add(PeksCipher(key, wordsCipher))
    }
    return results
  }

  override fun trapdoor(word: String, sk: Element, param: Param): Element {
    val bs = word.toByteArray()
    val h1 = param.G1.newElementFromHash(bs, 0, bs.size)
    return h1.duplicate().powZn(sk)
  }

  override fun search(pk: Element, ciphers: List<PeksCipher>, tw: Element, param: Param): List<String> {
    val results = mutableListOf<String>()
    ciphers.forEach {
      val cipher = it
      cipher.cwk.run {
        forEach {
          val h2_in = pairing.pairing(tw, it.first)
          val h2_hash = HashUtil.hash64(h2_in.toString()).toString()
          if (it.second == h2_hash) {
            results.add(cipher.doc)
            return@run
          }
        }
      }
    }
    return results
  }
}

fun main(args: Array<String>) {
  val peks = PEKS()
  println("开始！")
  // 系统初始化
  var start = System.currentTimeMillis()
  val param = peks.setup()
  var end = System.currentTimeMillis()
  println("系统参数生成完毕： ${end - start}ms")
  // 密钥对生成
  start = System.currentTimeMillis()
  val user = peks.keyGen(param)
  end = System.currentTimeMillis()
  println("用户密钥对生成完毕： ${end - start}ms")
  // 加密明文和关键字
  start = System.currentTimeMillis()
  val ciphers = peks.peks(user.pk, param)
  end = System.currentTimeMillis()
  println("加密完毕： ${end - start}ms")
  val scan = Scanner(System.`in`)
  var aim: String
  println("输入目标关键词: ")
  while (scan.hasNext()) {
    aim = scan.next()
    if ("exit" == aim) System.exit(0)
    // 生成陷门
    start = System.currentTimeMillis()
    val tw = peks.trapdoor(aim, user.sk, param)
    end = System.currentTimeMillis()
    println("陷门生成完毕： ${end - start}ms")
    // 关键字检索
    start = System.currentTimeMillis()
    val search_results = peks.search(user.pk, ciphers, tw, param)
    end = System.currentTimeMillis()
    println("关键字检索完毕： ${end - start}ms")
    println("结果总数：${search_results.size}")
    println("输入目标关键词: ")
  }
}