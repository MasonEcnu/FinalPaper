package com.mason.finalpaper.slow_search.basic

import com.mason.models.*
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {
  fun setup(): Param

  fun keyGen(param: Param): KeyPair

  fun msgEnc(doc: String, pk_do: Element, pk_du: Element, pk_csp: Element, r: Element, param: Param): SlowDocCipher

  fun indexGen(word: String, sk_do: Element, pk_du: Element, r: Element, param: Param): SlowWordCipher

  // 此方法调用msgEnc和indexGen以生成最后的密文
  fun enc(docs: Map<String, List<String>>, sk_do: Element, pk_do: Element, pk_du: Element, pk_csp: Element, param: Param): List<SlowMsg2CSP>

  fun trapdoor(word: String, sk_du: Element, pk_do: Element, param: Param): Element

  fun search(pk_du: Element, ciphers: List<SlowMsg2CSP>, tw: Element, param: Param): List<SlowMsg2CSP>

  fun preDec(pk_do: Element, sk_csp: Element, ciphers: List<SlowMsg2CSP>, param: Param): List<SlowMsg2CSP>

  fun recovery(ciphers: List<SlowMsg2CSP>, sk_du: Element, param: Param): List<String>
}
