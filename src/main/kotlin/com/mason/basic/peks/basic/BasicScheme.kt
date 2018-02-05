package com.mason.basic.peks.basic

import com.mason.models.*
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {
  fun setup(): Param

  fun keyGen(param: Param): KeyPair

  fun peks(pk: Element, param: Param): MutableList<PeksCipher>

  fun trapdoor(word: String, sk: Element, param: Param): Element

  fun search(pk: Element, ciphers: List<PeksCipher>, tw: Element, param: Param): List<String>
}
