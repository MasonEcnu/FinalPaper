package com.mason.fnlp

import org.fnlp.nlp.cn.CNFactory


fun main(args: Array<String>) {
  val start = System.currentTimeMillis()
  // 创建中文处理工厂对象，并使用“models”目录下的模型文件初始化
  val factory = CNFactory.getInstance("models")
  // 使用分词器对中文句子进行分词，得到分词结果
  val words = factory.seg("关注自然语言处理、语音识别、深度学习等方向的前沿技术和业界动态。")
  // 打印分词结果
  words.forEach {
    print("$it ")
  }
  println()

  // 中文词性标注
  val result = factory.tag2String("关注自然语言处理、语音识别、深度学习等方向的前沿技术和业界动态。")
  println(result)

  // 实体名识别
  val map = CNFactory.ner("詹姆斯·默多克和丽贝卡·布鲁克斯 鲁珀特·默多克旗下的美国小报《纽约邮报》的职员被公司律师告知，保存任何也许与电话窃听及贿赂有关的文件。")
  println(map.toString())
  val end = System.currentTimeMillis()
  println("cost = ${end - start}")
}