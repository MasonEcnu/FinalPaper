package com.mason.utils

import com.mason.constants.*
import com.mason.models.MyFile
import java.io.File
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密解密工具类
 */
class CryptoUtil {

  companion object {


    private val KEY_BYTES = "Mason.Wu".toByteArray()
    private val IV_BYTES = ".ECNUer.MasonWu.".toByteArray()

    // 初始化密钥文件
    init {
      File(AES_KEY_PATH).apply {
        if (!(exists() && 0L != length())) {
          // 生成AES密钥
          val kg = KeyGenerator.getInstance(KEY_ALGORITHM)
          kg.init(KEY_SIZE, SecureRandom(KEY_BYTES))
          val sk = kg.generateKey()
          val content = String(Base64.getEncoder().encode(sk.encoded))
          FileUtil.writeFile(AES_KEY_PATH, MyFile(content = mutableListOf(content)), FolderType.KEY)
        }
      }
    }

    /**
     * 恢复AES密钥
     */
    private fun recoverAESKey(): SecretKey {
      val keyStr = FileUtil.readFolderOrFile(AES_KEY_PATH, FolderType.KEY).first().content.first()
      val keyBytes = Base64.getDecoder().decode(keyStr)
      return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }

    /**
     * 加密sourceFolder文件夹中的文件，并将加密结果写入到aimFolder文件夹中
     */
    fun encrypt(sourceFolder: String, aimFolder: String) {
      val sk = recoverAESKey()
      val cipher = Cipher.getInstance(AES_PADDING)
      cipher.init(Cipher.ENCRYPT_MODE, sk, IvParameterSpec(IV_BYTES))
      val plaintextFiles = mutableListOf<MyFile>()
      FileUtil.readFolderOrFile(sourceFolder, FolderType.ENCRYPTED).forEach {
        val contents = it.content
        contents.indices.forEach {
          contents[it] = String(Base64.getEncoder().encode(cipher.doFinal(contents[it].toByteArray())))
        }
        plaintextFiles.add(it)
      }
      FileUtil.writeFiles(aimFolder, plaintextFiles, FolderType.ENCRYPTED)
    }

    /**
     * 解密sourceFolder文件夹中的文件，并将解密结果写入到aimFolder文件夹中
     */
    fun decrypt(sourceFolder: String, aimFolder: String) {
      val sk = recoverAESKey()
      val cipher = Cipher.getInstance(AES_PADDING)
      cipher.init(Cipher.DECRYPT_MODE, sk, IvParameterSpec(IV_BYTES))
      val encryptedFiles = mutableListOf<MyFile>()
      FileUtil.readFolderOrFile(sourceFolder, FolderType.DECRYPTED).forEach {
        val contents = it.content
        contents.indices.forEach {
          contents[it] = String((cipher.doFinal(Base64.getDecoder().decode(contents[it]))))
        }
        encryptedFiles.add(it)
      }
      FileUtil.writeFiles(aimFolder, encryptedFiles, FolderType.DECRYPTED)
    }
  }
}

fun main(args: Array<String>) {
  CryptoUtil.encrypt(PROCESSED_FILES_DIC_PATH, ENCRYPTED_FILES_DIC_PATH)
  CryptoUtil.decrypt(ENCRYPTED_FILES_DIC_PATH, DECRYPTED_FILES_DIC_PATH)
}