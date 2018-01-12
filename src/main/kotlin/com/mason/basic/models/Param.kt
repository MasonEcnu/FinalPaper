package com.mason.basic.models

import it.unisa.dia.gas.jpbc.Pairing

class Param(pairing: Pairing) {
  val G1 = pairing.g1
  val GT = pairing.gt
  val Zr = pairing.zr
  val g = G1.newRandomElement()
}