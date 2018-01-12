package com.mason.basic.spks.basic

import com.mason.basic.models.*
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {

  fun setup(): Param

  fun keyGen(param: Param): KeyPair

  fun enc(docs: Map<String, List<String>>, pk_u: Element, pk_c: Element, param: Param): List<SpksCipher>

  fun tCompute(word: String, sk_u: Element, param: Param): Element

  fun kwTest(pk_u: Element, ciphers: List<SpksCipher>, tw: Element, param: Param): List<SpksCipher>

  fun pDecrypt(pk_u: Element, sk_c: Element, ciphers: List<SpksCipher>, param: Param): List<SpksCipher>

  fun recovery(ciphers: List<SpksCipher>, sk_u: Element, param: Param): List<String>

}

