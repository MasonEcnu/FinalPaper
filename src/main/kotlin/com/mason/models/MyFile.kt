package com.mason.models

/**
 * 用于存储读取出的file实体
 */
data class MyFile(var name: String = "", var type: String = "", var content: MutableList<String> = mutableListOf())