package com.mason.models

import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing

data class KeyPair(val sk: Element, val pk: Element)

data class PaeksCipher(val c1: Element, val c2: Element)

data class SpksCipher(val cm: Triple<Element, String, String>, val cwk: MutableList<String>, var crho: Element)

data class SpchsCipher(val first: Element, val second: Element, val third: Element, val doc: String)

data class Structure(val pri: MutableMap<Element, MutableList<MutableMap<String, Element>>>, val pub: Element)

data class KeywordNode(val keyword: String, val idf: Double)

/**
 * 方案2：部分解密+抵抗内部关键字攻击+正向索引
 */
data class SlowDocCipher(val u1: Element, val u2: String, val u3: String, var crho: Element)

data class SlowWordCipher(val first: Element, val second: Element)

data class SlowMsg2CSP(val cm: List<SlowDocCipher>, val wordCiphers: MutableList<SlowWordCipher>)

/**
 * 方案2：部分解密+抵抗内部关键字攻击+隐藏结构的倒排索引
 */
data class FastDocCipher(val cm: Triple<Element, String, String>, var crho: Element)

data class FastIndexCipher(val first: Element, val second: Element, val third: Element, val doc: String)

data class FastCipher2CSP(val docCiphers: MutableMap<String, List<FastDocCipher>>, val wordCiphers: Map<Element, MutableMap<Long, FastIndexCipher>>)

/**
 * 用于存储读取出的file实体
 */
data class MyFile(var name: String = "", var type: String = "", var content: MutableList<String> = mutableListOf())

class Param(pairing: Pairing) {
  val G1 = pairing.g1
  val GT = pairing.gt
  val Zr = pairing.zr
  val g = G1.newRandomElement()
}