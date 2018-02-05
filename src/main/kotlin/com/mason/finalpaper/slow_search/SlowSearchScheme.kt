package com.mason.finalpaper.slow_search

import com.mason.constants.MAX_FILE_LENGTH
import com.mason.constants.STOP_CHARACTER
import com.mason.models.*
import com.mason.finalpaper.slow_search.basic.BasicScheme
import com.mason.finalpaper.slow_search.data.SlowData
import com.mason.utils.HashUtil
import com.mason.utils.MathUtil
import com.mason.utils.StringUtil
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import java.util.*

class SlowSearchScheme : BasicScheme {

  companion object {
    val pairing: Pairing = PairingFactory.getPairing("config/a.properties")
    // rho的长度
    val n_rho = MathUtil.log2(512)

    val Documents = SlowData.Documents
    val Doc2Word = SlowData.Doc2Word

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

  override fun msgEnc(doc_id: String, doc: String, pk_do: Element, pk_du: Element, pk_csp: Element, r: Element, param: Param): MutableList<SlowDocCipher> {
    val docs = StringUtil.padding(doc + STOP_CHARACTER)
    val results = mutableListOf<SlowDocCipher>()
    val rho = StringUtil.randomBinaryString(n_rho)
    docs.forEach {
      // 1. 计算u1
      val u1 = param.g.duplicate().powZn(r)
      // 2.计算u2
      val h5_in = pairing.pairing(pk_do, pk_csp).duplicate().powZn(r)
      val h5_hash = HashUtil.hash64(h5_in.toString()).toString()
      val h5 = StringUtil.randomBinaryString(h5_hash, n_rho)
      val u2 = MathUtil.xor(rho, h5)
      // 3.计算u3
      val h3_rho = param.G1.newElementFromHash(rho.toByteArray(), 0, n_rho)
      val h4_in = pairing.pairing(h3_rho, pk_du.duplicate().powZn(r))
      val h4_hash = HashUtil.hash64(h4_in.toString()).toString()
      val h4 = StringUtil.randomBinaryString(h4_hash, MAX_FILE_LENGTH)
      val u3 = MathUtil.xor(it, h4)
      // 4.计算cm
      results.add(SlowDocCipher(doc_id, u1, u2, u3, param.G1.newZeroElement()))
    }
    return results
  }

  override fun indexGen(word: String, sk_do: Element, pk_du: Element, r: Element, param: Param): SlowWordCipher {
    val wbs = word.toByteArray()
    val h1 = param.G1.newElementFromHash(wbs, 0, wbs.size)
    val gr = param.g.duplicate().powZn(r)
    val first = h1.duplicate().powZn(sk_do).mul(gr)
    val second = pk_du.duplicate().powZn(r)
    return SlowWordCipher(first, second)
  }

  override fun enc(sk_do: Element, pk_do: Element, pk_du: Element, pk_csp: Element, param: Param): List<SlowMsg2CSP> {
    val results = mutableListOf<SlowMsg2CSP>()
    Doc2Word.keys.forEach {
      val keywords = mutableListOf<SlowWordCipher>()
      // 关键字
      Doc2Word[it]?.forEach {
        val r_word = param.Zr.newRandomElement()
        keywords.add(indexGen(it, sk_do, pk_du, r_word, param))
      }
      val r_doc = param.Zr.newRandomElement()
      Collections.shuffle(keywords)
      results.add(SlowMsg2CSP(msgEnc(it, Documents[it] ?: "", pk_do, pk_du, pk_csp, r_doc, param), keywords))
    }
    return results
  }

  override fun trapdoor(word: String, sk_du: Element, pk_do: Element, param: Param): Element {
    val wbs = word.toByteArray()
    val h1_w = param.G1.newElementFromHash(wbs, 0, wbs.size)
    val first = h1_w.duplicate().powZn(sk_du)
    return pairing.pairing(first, pk_do)
  }

  override fun search(pk_du: Element, ciphers: List<SlowMsg2CSP>, tw: Element, param: Param): List<SlowMsg2CSP> {
    val results = mutableListOf<SlowMsg2CSP>()
    ciphers.indices.forEach {
      val cipher = ciphers[it]
      val wordCiphers = cipher.wordCiphers
      wordCiphers.indices.run {
        forEach {
          val left = pairing.pairing(wordCiphers[it].second, param.g)
          val right = pairing.pairing(wordCiphers[it].first, pk_du)
          val leftVar = left.duplicate().mul(tw)
          if (leftVar.isEqual(right)) {
            results.add(cipher)
            return@run
          }
        }
      }
    }
    return results
  }

  override fun preDec(pk_do: Element, sk_csp: Element, ciphers: List<SlowMsg2CSP>, param: Param): List<SlowMsg2CSP> {
    ciphers.forEach {
      it.cm.forEach {
        val h5_in = pairing.pairing(pk_do, it.u1).duplicate().powZn(sk_csp)
        val h5_hash = HashUtil.hash64(h5_in.toString()).toString()
        val h5 = StringUtil.randomBinaryString(h5_hash, n_rho)
        val rho = MathUtil.xor(it.u2, h5)
        val rho_bytes = rho.toByteArray()
        val h3_rho = param.G1.newElementFromHash(rho_bytes, 0, rho_bytes.size)
        it.crho = pairing.pairing(h3_rho, it.u1)
      }
    }
    return ciphers
  }

  override fun recovery(ciphers: List<SlowMsg2CSP>, sk_du: Element, param: Param): Map<String, String> {
    val results = mutableMapOf<String, String>()
    var doc_name = ""
    ciphers.forEach {
      val sb = StringBuilder()
      it.cm.forEach {
        doc_name = it.doc_id
        val h4_in = it.crho.duplicate().powZn(sk_du)
        val h4_hash = HashUtil.hash64(h4_in.toString()).toString()
        val h4 = StringUtil.randomBinaryString(h4_hash, it.u3.length)
        sb.append(MathUtil.xor(h4, it.u3))
      }
      results.put(doc_name, sb.toString())
    }
    return results
  }
}

fun main(args: Array<String>) {
  val slow = SlowSearchScheme()
  println("开始！")
  // 系统初始化
  var start = System.currentTimeMillis()
  val param = slow.setup()
  var end = System.currentTimeMillis()
  println("系统参数生成完毕： ${end - start}ms")
  // 密钥对生成
  start = System.currentTimeMillis()
  val owner = slow.keyGen(param)
  end = System.currentTimeMillis()
  println("数据拥有者密钥对生成完毕： ${end - start}ms")
  start = System.currentTimeMillis()
  val user = slow.keyGen(param)
  end = System.currentTimeMillis()
  println("数据使用者密钥对生成完毕： ${end - start}ms")
  start = System.currentTimeMillis()
  val csp = slow.keyGen(param)
  end = System.currentTimeMillis()
  println("服务器密钥对生成完毕： ${end - start}ms")
  // 加密明文和关键字
  start = System.currentTimeMillis()
  val ciphers = slow.enc(owner.sk, owner.pk, user.pk, csp.pk, param)
  end = System.currentTimeMillis()
  println("加密完毕： ${end - start}ms")
  // 查询目标
  val scan = Scanner(System.`in`)
  var aim: String
  println("输入目标关键词: ")
  while (scan.hasNext()) {
    aim = scan.next()
    if ("exit" == aim) System.exit(0)
    // 生成陷门
    start = System.currentTimeMillis()
    val tw = slow.trapdoor(aim, user.sk, owner.pk, param)
    end = System.currentTimeMillis()
    println("陷门生成完毕： ${end - start}ms")
    // 关键字检索
    start = System.currentTimeMillis()
    val search_results = slow.search(user.pk, ciphers, tw, param)
    end = System.currentTimeMillis()
    println("关键字检索完毕： ${end - start}ms")
    println("结果总数：${search_results.size}")
//    // 部分解密
//    start = System.currentTimeMillis()
//    val pre_results = slow.preDec(owner.pk, csp.sk, search_results, param)
//    end = System.currentTimeMillis()
//    println("部分解密完毕： ${end - start}ms")
//    // 完全解密
//    start = System.currentTimeMillis()
//    val results = slow.recovery(pre_results, user.sk, param)
//    end = System.currentTimeMillis()
//    println("完全解密完毕： ${end - start}ms")
//    println("解密结果：")
//    results.forEach { key, value ->
//      println("$key --> ${value.substring(0, value.indexOf(STOP_CHARACTER)).substring(0, 20)}")
//    }
    println("输入目标关键词: ")
  }
}