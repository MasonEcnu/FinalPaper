package com.mason.basic

import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory

import java.math.BigInteger
import java.util.concurrent.ConcurrentSkipListMap

class KotlinPairingDemo {

  var pairing: Pairing
    private set

  init {
    pairing = PairingFactory.getPairing("config/a.properties")
    PairingFactory.getInstance().isUsePBCWhenPossible = true
  }
}

fun main(args: Array<String>) {
  val pairing = KotlinPairingDemo().pairing
  val Zr = pairing.zr
  val G1 = pairing.g1
  val G2 = pairing.g2
  val GT = pairing.gt

  val a = G1.newRandomElement()
  val b = G1.newRandomElement()
  val c = pairing.pairing(a, b)
}
