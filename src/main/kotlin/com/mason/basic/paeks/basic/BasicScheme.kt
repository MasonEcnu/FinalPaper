package com.mason.basic.paeks.basic

import com.mason.models.*
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {

  fun setup(): Param

  fun keyGen(param: Param): KeyPair

  fun peks(word: String, sk_s: Element, pk_r: Element, param: Param): PaeksCipher

  fun preprocess(ciphers: MutableList<PaeksCipher>, pk_r: Element, param: Param): List<Element>

  fun trapdoor(word: String, pk_s: Element, sk_r: Element, param: Param): Element

  fun test(Tw: Element, cipher: PaeksCipher, pk_r: Element, param: Param): Int

}