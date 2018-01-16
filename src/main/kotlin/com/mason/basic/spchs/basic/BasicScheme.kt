package com.mason.basic.spchs.basic

import com.mason.models.*
import it.unisa.dia.gas.jpbc.Element

interface BasicScheme {

  fun systemSetup(): Param

  fun keyGen(param: Param): KeyPair

  fun structureInitialization(owner_sk: Element, param: Param): Structure

  fun structuredEncryption(param: Param, words: Map<String, List<String>>, owner: KeyPair, user_pk: Element, structure: Structure): Map<Element, Map<Long, SpchsCipher>>

  fun trapdoor(user_sk: Element, word: String, param: Param): Element

  fun structuredSearch(param: Param, user_pk: Element, owner_pk: Element, ciphers: Map<Element, Map<Long, SpchsCipher>>, tw: Element): List<String>
}