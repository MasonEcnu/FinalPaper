package com.mason.finalpaper.spchs

import com.mason.finalpaper.models.KeyPair
import com.mason.finalpaper.models.Param
import com.mason.finalpaper.models.SpchsCipher
import com.mason.finalpaper.models.Structure
import com.mason.finalpaper.spchs.basic.BasicScheme
import com.mason.finalpaper.spchs.data.Word2Doc
import com.mason.utils.HashUtil
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import java.util.*

class SPCHS : BasicScheme {

  companion object {
    val pairing: Pairing = PairingFactory.getPairing("config/a.properties")

    init {
      PairingFactory.getInstance().isUsePBCWhenPossible = true
    }
  }

  override fun systemSetup(): Param {
    return Param(pairing)
  }

  /**
   * 密钥对生成
   */
  override fun keyGen(param: Param): KeyPair {
    val sk = param.Zr.newRandomElement()
    val pk = param.g.duplicate().powZn(sk)
    return KeyPair(sk, pk)
  }

  /**
   * 结构初始化
   */
  override fun structureInitialization(owner_sk: Element, param: Param): Structure {
    val pub = param.g.duplicate().powZn(owner_sk)
    val pri = mutableMapOf(owner_sk to mutableListOf<MutableMap<String, Element>>())
    return Structure(pri, pub)
  }

  /**
   * 结构加密
   */
  override fun structuredEncryption(param: Param, words: Map<String, List<String>>, owner: KeyPair, user_pk: Element, structure: Structure): Map<Element, Map<Long, SpchsCipher>> {
    val results = mutableMapOf<Long, SpchsCipher>()
    val priLists = structure.pri[owner.sk]
    if (priLists != null) {
      var flag = false
      words.keys.forEach {
        val r = param.Zr.newRandomElement()
        val word = it
        val wbs = word.toByteArray()
        val word_G1 = param.G1.newElementFromHash(wbs, 0, wbs.size)
        if (priLists.size == 0) flag = false
        var index = -1
        priLists.run {
          val lists = this
          indices.forEach {
            flag = lists[it].containsKey(word)
            if (flag) {
              index = it
              return@run
            }
          }
        }
        words[word]?.run {
          forEach {
            val doc = it
            val word_G1_pk = pairing.pairing(user_pk, word_G1)
            if (flag && index != -1) {
              // found
              val c1 = priLists[index][word] ?: param.GT.newRandomElement()
              val R = param.GT.newRandomElement()
              val c2 = param.g.duplicate().powZn(r)
              val c3 = word_G1_pk.duplicate().powZn(r).duplicate().mul(R)
              results.put(HashUtil.hash64(c1.toString()), SpchsCipher(c1, c2, c3, doc))
              priLists[index][word] = R
            } else {
              // not found
              val R = param.GT.newRandomElement()
              val c1 = word_G1_pk.duplicate().powZn(owner.sk)
              val c2 = param.g.duplicate().powZn(r)
              val c3 = word_G1_pk.duplicate().powZn(r).duplicate().mul(R)
              results.put(HashUtil.hash64(c1.toString()), SpchsCipher(c1, c2, c3, doc))
              val map = mutableMapOf(word to R)
              priLists.add(map)
              index = priLists.indexOf(map)
              flag = true
            }
          }
        }
      }
    }
    return mutableMapOf(owner.pk to results)
  }

  /**
   * 陷门生成
   */
  override fun trapdoor(user_sk: Element, word: String, param: Param): Element {
    val wbs = word.toByteArray()
    val ele = param.G1.newElementFromHash(wbs, 0, wbs.size)
    return ele.duplicate().powZn(user_sk)
  }

  /**
   * 检索
   */
  override fun structuredSearch(param: Param, user_pk: Element, owner_pk: Element, ciphers: Map<Element, Map<Long, SpchsCipher>>, tw: Element): List<String> {
    val results = mutableListOf<String>()
    var pt = pairing.pairing(owner_pk, tw)
    var pt_hash = HashUtil.hash64(pt.toString())
    val cipher_map = ciphers[owner_pk]
    if (cipher_map != null) {
      while (true) {
        if (cipher_map.containsKey(pt_hash)) {
          val cipher = cipher_map[pt_hash]
          if (cipher != null) {
            results.add(cipher.doc)
            pt = cipher.third.duplicate().div(pairing.pairing(cipher.second, tw))
            pt_hash = HashUtil.hash64(pt.toString())
          }
        } else {
          break
        }
      }
    }
    return results
  }
}

fun main(args: Array<String>) {
  var start: Long
  var end: Long
  println("开始！")
  // 关键字列表
  start = System.currentTimeMillis()
  val spchs = SPCHS()
  // 系统初始化
  start = System.currentTimeMillis()
  val param = spchs.systemSetup()
  end = System.currentTimeMillis()
  println("系统参数生成完毕： ${end - start}ms")
  // 发送者密钥对
  start = System.currentTimeMillis()
  val user = spchs.keyGen(param)
  // 接收者密钥对
  val owner = spchs.keyGen(param)
  end = System.currentTimeMillis()
  println("使用者密钥生成完毕： ${end - start}ms")
  // 结构初始化
  start = System.currentTimeMillis()
  val structure = spchs.structureInitialization(owner.sk, param)
  end = System.currentTimeMillis()
  println("结构初始化完毕： ${end - start}ms")
  // 索引加密
  start = System.currentTimeMillis()
  val ciphers = spchs.structuredEncryption(param, Word2Doc, owner, user.pk, structure)
  end = System.currentTimeMillis()
  println("加密完毕： ${end - start}ms")
  // 查询目标
  val scan = Scanner(System.`in`)
  var aim: String
  println("输入目标关键词: ")
  while (scan.hasNext()) {
    aim = scan.next()
    if ("exit" == aim) System.exit(0)
    // 陷门
    val tw = spchs.trapdoor(user.sk, aim, param)
    // 测试
    start = System.currentTimeMillis()
    val results = spchs.structuredSearch(param, user.pk, owner.pk, ciphers, tw)
    end = System.currentTimeMillis()
    println("检索完毕： ${end - start}ms")
    println("检索结果：")
    results.forEach {
      println(it)
    }
    println("输入目标关键词: ")
  }
}