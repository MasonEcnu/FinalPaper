package com.mason.basic.paeks

import com.mason.basic.paeks.basic.BasicScheme
import com.mason.models.*
import com.mason.utils.StringUtil
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import java.util.*

class PAEKS : BasicScheme {

  var isPreprocess = false

  companion object {
    val pairing: Pairing = PairingFactory.getPairing("config/a.properties")

    init {
      PairingFactory.getInstance().isUsePBCWhenPossible = true
    }
  }

  /**
   * 系统初始化
   */
  override fun setup(): Param = Param(pairing)

  /**
   * 密钥对生成
   */
  override fun keyGen(param: Param): KeyPair {
    val sk = param.Zr.newRandomElement()
    val pk = param.g.duplicate().powZn(sk)
    return KeyPair(sk, pk)
  }

  /**
   * 索引项生成
   */
  override fun peks(word: String, sk_s: Element, pk_r: Element, param: Param): PaeksCipher {
    val r = param.Zr.newRandomElement()
    val bytes = word.toByteArray()
    val c1_1 = param.G1.newElementFromHash(bytes, 0, bytes.size).duplicate().powZn(sk_s)
    val c1_2 = param.g.duplicate().powZn(r)
    val c1 = c1_1.duplicate().mul(c1_2)
    val c2 = pk_r.duplicate().powZn(r)
    return PaeksCipher(c1, c2)
  }

  /**
   * 陷门生成
   */
  override fun trapdoor(word: String, pk_s: Element, sk_r: Element, param: Param): Element {
    val bytes = word.toByteArray()
    val p1 = param.G1.newElementFromHash(bytes, 0, bytes.size).duplicate().powZn(sk_r)
    val p2 = pk_s
    return pairing.pairing(p1, p2)
  }

  /**
   * 服务器预处理索引项
   */
  override fun preprocess(ciphers: MutableList<PaeksCipher>, pk_r: Element, param: Param): List<Element> {
    isPreprocess = true
    val result = mutableListOf<Element>()
    ciphers.forEach {
      val down = pairing.pairing(it.c2, param.g)
      val up = pairing.pairing(it.c1, pk_r)
      result.add(up.duplicate().div(down))
    }
    return result
  }

  /**
   * 测试索引项
   */
  override fun test(Tw: Element, cipher: PaeksCipher, pk_r: Element, param: Param): Int {
    val left_left = Tw
    val left_right = pairing.pairing(cipher.c2, param.g)
    val left = left_left.duplicate().mul(left_right)
    val right = pairing.pairing(cipher.c1, pk_r)
    return if (left.isEqual(right)) 1 else 0
  }

}

fun main(args: Array<String>) {
  var start: Long
  var end: Long
  println("开始！")
  // 关键字列表
  start = System.currentTimeMillis()
  val words = mutableListOf<String>()
  (0 until 100).forEach {
    words.add(StringUtil.genRandomString(5))
    if (it % 10 == 0) println(words[it])
  }
  end = System.currentTimeMillis()
  println("关键字初始化完毕： ${end - start}ms")
  val paeks = PAEKS()
  // 系统初始化
  start = System.currentTimeMillis()
  val param = paeks.setup()
  end = System.currentTimeMillis()
  println("系统参数生成完毕： ${end - start}ms")
  // 发送者密钥对
  start = System.currentTimeMillis()
  val sender = paeks.keyGen(param)
  // 接收者密钥对
  val receiver = paeks.keyGen(param)
  end = System.currentTimeMillis()
  println("使用者密钥生成完毕： ${end - start}ms")
  // 索引list
  val ciphers = mutableListOf<PaeksCipher>()
  start = System.currentTimeMillis()
  words.forEach {
    ciphers.add(paeks.peks(it, sender.sk, receiver.pk, param))
  }
  end = System.currentTimeMillis()
  println("索引项生成完毕： ${end - start}ms")
  // 服务器预处理
  start = System.currentTimeMillis()
  val preprocessed = paeks.preprocess(ciphers, receiver.pk, param)
  end = System.currentTimeMillis()
  println("服务器预处理完毕： ${end - start}ms")
  // 查询目标
  val scan = Scanner(System.`in`)
  var aim: String
  println("是否应用预处理？Y/N: ")
  while (scan.hasNext()) {
    aim = scan.next()
    paeks.isPreprocess = "Y" == aim.toUpperCase()
    println("输入目标关键词: ")
    aim = scan.next()
    if ("exit" == aim) System.exit(0)
    // 陷门
    val Tw = paeks.trapdoor(aim, sender.pk, receiver.sk, param)
    // 测试
    start = System.currentTimeMillis()
    if (paeks.isPreprocess) {
      preprocessed.let {
        it.forEach {
          if (Tw.isEqual(it)) {
            println("Bingo!")
            return@let
          }
        }
      }
    } else {
      ciphers.let {
        it.forEach {
          if (paeks.test(Tw, it, receiver.pk, param) == 1) {
            println("Bingo!")
            return@let
          }
        }
      }
    }
    end = System.currentTimeMillis()
    println("检索完毕： ${end - start}ms")
    println("是否应用预处理？Y/N: ")
  }
}