package com.mason.finalpaper.models

import it.unisa.dia.gas.jpbc.Element


data class KeyPair(val sk: Element, val pk: Element)

data class PaeksCipher(val c1: Element, val c2: Element)

data class SpksCipher(val cm: Triple<Element, String, String>, val cwk: MutableList<String>, var crho: Element)

data class SpchsCipher(val first: Element, val second: Element, val third: Element, val doc: String)

data class Structure(val pri: MutableMap<Element, MutableList<MutableMap<String, Element>>>, val pub: Element)