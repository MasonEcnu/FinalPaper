package com.mason.finalpaper.fast_search

import com.mason.constants.MAX_FILE_LENGTH
import com.mason.constants.PRIME_LENGTH
import com.mason.constants.STOP_CHARACTER
import com.mason.finalpaper.fast_search.basic.BasicScheme
import com.mason.finalpaper.fast_search.data.*
import com.mason.models.*
import com.mason.utils.HashUtil
import com.mason.utils.MathUtil
import com.mason.utils.StringUtil
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import java.util.*

class FastSearchScheme : BasicScheme {

  companion object {
    val pairing: Pairing = PairingFactory.getPairing("config/a.properties")
    // rho的长度
    val n_rho = MathUtil.log2(PRIME_LENGTH)

    val Documents = FastData.Documents
    val Word2Doc = FastData.Word2Doc

    var indexLists = listOf<FastIndexCipher>()

    init {
      PairingFactory.getInstance().isUsePBCWhenPossible = true
//      Word2Doc.forEach { key, value ->
//        println("$key --> ${value.size}")
//      }
    }
  }

  // 初始化系统参数
  override fun setup(): Param = Param(pairing)

  // 生成密钥对
  override fun keyGen(param: Param): KeyPair {
    val sk = param.Zr.newRandomElement()
    val pk = param.g.duplicate().powZn(sk)
    return KeyPair(sk, pk)
  }

  // 文档加密
  override fun docEnc(docs: Map<String, String>, pk_do: Element, pk_du: Element, pk_csp: Element, param: Param): MutableMap<String, List<FastDocCipher>> {
    val results = mutableMapOf<String, List<FastDocCipher>>()
    docs.keys.forEach {
      val doc_id = it
      val content = docs[it]
      if (content != null) {
        val docCiphers = mutableListOf<FastDocCipher>()
        val docLists = StringUtil.padding(content + STOP_CHARACTER)
        docLists.forEach {
          val r = param.Zr.newRandomElement()
          // 1. 计算u1
          val u1 = param.g.duplicate().powZn(r)
          // 2.计算u2
          val rho = StringUtil.genRandomString(n_rho)
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
          docCiphers.add(FastDocCipher(Triple(u1, u2, u3), param.G1.newZeroElement()))
        }
        results.put(doc_id, docCiphers)
      }
    }
    return results
  }

  // 结构初始化
  override fun strucInit(sk_do: Element, param: Param): Structure {
    val pub = param.g.duplicate().powZn(sk_do)
    val pri = mutableMapOf(sk_do to mutableListOf<MutableMap<String, Element>>())
    return Structure(pri, pub)
  }

  // 结构加密
  override fun strucEnc(words: Map<String, List<String>>, sk_do: Element, pk_du: Element, structure: Structure, param: Param): MutableMap<Long, FastIndexCipher> {
    val results = mutableMapOf<Long, FastIndexCipher>()
    val priLists = structure.pri[sk_do]
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
        words[word]?.forEach {
          val word_G1_sk = word_G1.duplicate().powZn(sk_do)
          if (flag && index != -1) {
            // found
            val c1 = priLists[index][word] ?: param.GT.newRandomElement()
            val R = param.GT.newRandomElement()
            val c2 = pk_du.duplicate().powZn(r)
            val c3 = pairing.pairing(word_G1_sk.duplicate().mul(param.g.duplicate().powZn(r)), pk_du).duplicate().mul(R)
            results.put(HashUtil.hash64(c1.toString()), FastIndexCipher(c1, c2, c3, it))
            priLists[index][word] = R
          } else {
            // not found
            val R = param.GT.newRandomElement()
            val c1 = pairing.pairing(word_G1_sk.duplicate().mul(structure.pub), pk_du)
            val c2 = pk_du.duplicate().powZn(r)
            val c3 = pairing.pairing(word_G1_sk.duplicate().mul(param.g.duplicate().powZn(r)), pk_du).duplicate().mul(R)
            results.put(HashUtil.hash64(c1.toString()), FastIndexCipher(c1, c2, c3, it))
            val map = mutableMapOf(word to R)
            priLists.add(map)
            index = priLists.indexOf(map)
            flag = true
          }
        }
      }
    }
    return results
  }

  // 加密
  override fun enc(sk_do: Element, pk_do: Element, pk_du: Element, pk_csp: Element, structure: Structure, param: Param): FastCipher2CSP {
    val docCiphers = docEnc(Documents, pk_do, pk_du, pk_csp, param)
    val wordsCiphers = strucEnc(Word2Doc, sk_do, pk_du, structure, param)
    indexLists = wordsCiphers.values.toList()
    Collections.shuffle(indexLists)
    return FastCipher2CSP(docCiphers, mapOf(structure.pub to wordsCiphers))
  }

  // 生成陷门
  override fun trapdoor(word: String, sk_du: Element, pk_do: Element, param: Param): Element {
    val wbs = word.toByteArray()
    val h1_w = param.G1.newElementFromHash(wbs, 0, wbs.size)
    val left = h1_w.duplicate().powZn(sk_du)
    return pairing.pairing(left, pk_do)
  }

  // 检索
  override fun search(pk_du: Element, pk_st: Element, ciphers: FastCipher2CSP, tw: Element, param: Param): Map<String, List<FastDocCipher>> {
//    val results = mutableMapOf<String, List<FastDocCipher>>()
//    var pt = pairing.pairing(pk_st, pk_du).duplicate().mul(tw)
//    var pt_hash = HashUtil.hash64(pt.toString())
//    val index_map = ciphers.wordCiphers[pk_st]
//    val cipher_map = ciphers.docCiphers
//    if (index_map != null) {
//      while (index_map.containsKey(pt_hash)) {
//        val index = index_map[pt_hash]
//        if (index != null) {
//          val cipher = cipher_map[index.doc]
//          if (cipher != null) {
//            results.put(index.doc, cipher)
//            pt = index.third.duplicate().div(pairing.pairing(index.second, param.g).duplicate().mul(tw))
//            pt_hash = HashUtil.hash64(pt.toString())
//          }
//        }
//      }
//    }
//    return results

    val results = mutableMapOf<String, List<FastDocCipher>>()
    var pt = pairing.pairing(pk_st, pk_du).duplicate().mul(tw)
    val cipher_map = ciphers.docCiphers
    var flag = true
    while (flag) {
      indexLists.indices.run {
        forEach {
          val record = indexLists[it]
          if (pt.isEqual(record.first)) {
            cipher_map[record.doc]?.let {
              results.put(record.doc, it)
              pt = record.third.duplicate().div(pairing.pairing(record.second, param.g).duplicate().mul(tw))
              return@run
            }
          } else if (it == indexLists.size - 1) {
            flag = false
            return@run
          }
        }
      }
    }
    return results
  }

  // 部分解密
  override fun preDec(pk_do: Element, sk_csp: Element, ciphers: Map<String, List<FastDocCipher>>, param: Param): Map<String, List<FastDocCipher>> {
    ciphers.keys.forEach {
      ciphers[it]?.forEach {
        val h5_in = pairing.pairing(pk_do, it.cm.first).duplicate().powZn(sk_csp)
        val h5_hash = HashUtil.hash64(h5_in.toString()).toString()
        val h5 = StringUtil.randomBinaryString(h5_hash, n_rho)
        val rho = MathUtil.xor(it.cm.second, h5)
        val rho_bytes = rho.toByteArray()
        val h3_rho = param.G1.newElementFromHash(rho_bytes, 0, rho_bytes.size)
        it.crho = pairing.pairing(h3_rho, it.cm.first)
      }
    }
    return ciphers
  }

  // 恢复明文
  override fun recovery(ciphers: Map<String, List<FastDocCipher>>, sk_du: Element, param: Param): Map<String, String> {
    val results = mutableMapOf<String, String>()
    ciphers.keys.forEach {
      val sb = StringBuilder()
      ciphers[it]?.forEach {
        val h4_in = it.crho.duplicate().powZn(sk_du)
        val h4_hash = HashUtil.hash64(h4_in.toString()).toString()
        val h4 = StringUtil.randomBinaryString(h4_hash, it.cm.third.length)
        sb.append(MathUtil.xor(h4, it.cm.third))
      }
      results.put(it, sb.toString())
    }
    return results
  }

}

fun main(args: Array<String>) {
  val fast = FastSearchScheme()
  println("开始！")
  // 系统初始化
  var start = System.currentTimeMillis()
  val param = fast.setup()
  var end = System.currentTimeMillis()
  println("系统参数生成完毕： ${end - start}ms")
  // 密钥对生成
  start = System.currentTimeMillis()
  val owner = fast.keyGen(param)
  end = System.currentTimeMillis()
  println("数据拥有者密钥对生成完毕： ${end - start}ms")
  start = System.currentTimeMillis()
  val user = fast.keyGen(param)
  end = System.currentTimeMillis()
  println("数据使用者密钥对生成完毕： ${end - start}ms")
  start = System.currentTimeMillis()
  val csp = fast.keyGen(param)
  end = System.currentTimeMillis()
  println("服务器密钥对生成完毕： ${end - start}ms")
  // 结构初始化
  start = System.currentTimeMillis()
  val structure = fast.strucInit(owner.sk, param)
  end = System.currentTimeMillis()
  println("结构初始化完毕： ${end - start}ms")
  // 加密明文和关键字
  start = System.currentTimeMillis()
  val ciphers = fast.enc(owner.sk, owner.pk, user.pk, csp.pk, structure, param)
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
    val tw = fast.trapdoor(aim, user.sk, owner.pk, param)
    end = System.currentTimeMillis()
    println("陷门生成完毕： ${end - start}ms")
    // 关键字检索
    start = System.currentTimeMillis()
    val search_results = fast.search(user.pk, structure.pub, ciphers, tw, param)
    end = System.currentTimeMillis()
    println("关键字检索完毕： ${end - start}ms")
    println("结果总数： ${search_results.size}")
//    // 部分解密
//    start = System.currentTimeMillis()
//    val pre_results = fast.preDec(owner.pk, csp.sk, search_results, param)
//    end = System.currentTimeMillis()
//    println("部分解密完毕： ${end - start}ms")
//    // 完全解密
//    start = System.currentTimeMillis()
//    val results = fast.recovery(pre_results, user.sk, param)
//    end = System.currentTimeMillis()
//    println("完全解密完毕： ${end - start}ms")
//    println("解密结果：")
//    results.forEach { key, value ->
//      println("$key --> ${value.substring(0, value.indexOf(STOP_CHARACTER)).substring(0, 20)}")
//    }
//    println("结果总数： ${results.size}")
    println("输入目标关键词: ")
  }
}