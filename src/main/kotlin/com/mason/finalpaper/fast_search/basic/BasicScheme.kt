package com.mason.finalpaper.fast_search.basic

import com.mason.models.*
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {
  fun setup(): Param

  fun keyGen(param: Param): KeyPair

  fun docEnc(docs: Map<String, String>, pk_do: Element, pk_du: Element, pk_csp: Element, param: Param): MutableMap<String, List<FastDocCipher>>

  fun strucInit(sk_do: Element, param: Param): Structure

  fun strucEnc(words: Map<String, List<String>>, sk_do: Element, pk_du: Element, structure: Structure, param: Param): MutableMap<Long, FastIndexCipher>

  fun enc(docs: Map<String, String>, words: Map<String, List<String>>, sk_do: Element, pk_do: Element, pk_du: Element, pk_csp: Element,structure: Structure, param: Param): FastCipher2CSP

  fun trapdoor(word: String, sk_du: Element, pk_do: Element, param: Param): Element

  fun search(pk_du: Element, pk_do: Element, pk_st: Element, ciphers: FastCipher2CSP, tw: Element, param: Param): Map<String, List<FastDocCipher>>

  fun preDec(pk_do: Element, sk_csp: Element, ciphers: Map<String, List<FastDocCipher>>, param: Param): Map<String, List<FastDocCipher>>

  fun recovery(ciphers: Map<String, List<FastDocCipher>>, sk_du: Element, param: Param): Map<String, String>
}
