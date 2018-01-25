package com.mason.constants

// 文件存储位置
const val ORIGINAL_FILES_DIC_PATH: String = "data\\original"
const val PROCESSED_FILES_DIC_PATH: String = "data\\processed"
const val ENCRYPTED_FILES_DIC_PATH: String = "data\\encrypted"
const val DECRYPTED_FILES_DIC_PATH: String = "data\\decrypted"
const val SPLICED_FILES_DIC_PATH: String = "data\\spliced"
const val WORDS_DIC_PATH: String = "data\\words"
// AES加密解密
const val AES_KEY_PATH: String = "keys\\AESKey.txt"
const val KEY_ALGORITHM = "AES"
const val KEY_SIZE = 128
const val AES_PADDING = "AES/CBC/PKCS5Padding"
// 用于生成随机字符串
const val CHARS = " !\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
const val CHARS_LENGTH = CHARS.length
// 最大文件长度
const val MAX_FILE_LENGTH = 2500

// 素数p的位数
const val PRIME_LENGTH = 512

// 读取文件方式枚举
enum class FolderType {
  FILE, ENCRYPTED, DECRYPTED, KEY, SPLICED, INDEX
}

// 分隔符
const val SEPARATOR = " "

// 终止符
const val STOP_CHARACTER = "#.#"

// 总关键字个数
const val MAX_KEYWORDS_COUNT_TOTAL = 1000
// 单个文件提取的关键字个数
const val MAX_KEYWORDS_COUNT_SINGLE = 5
// 正向索引路径
const val FORWARD_INDEX_PATH = "data\\index\\forward.txt"
// 倒排索引路径
const val INVERTED_INDEX_PATH = "data\\index\\inverted.txt"
// 测试文件
const val TEST_FILES_DIC_PATH: String = "data\\temp"