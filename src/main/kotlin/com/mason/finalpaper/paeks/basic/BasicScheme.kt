package com.mason.finalpaper.paeks.basic

import com.mason.finalpaper.models.KeyPair
import com.mason.finalpaper.models.PaeksCipher
import com.mason.finalpaper.models.Param
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {

  fun setup(): Param

  fun keyGen(param: Param): KeyPair

  fun peks(word: String, sk_s: Element, pk_r: Element, param: Param): PaeksCipher

  fun preprocess(ciphers: MutableList<PaeksCipher>, pk_r: Element, param: Param): List<Element>

  fun trapdoor(word: String, pk_s: Element, sk_r: Element, param: Param): Element

  fun test(Tw: Element, cipher: PaeksCipher, pk_r: Element, param: Param): Int

}