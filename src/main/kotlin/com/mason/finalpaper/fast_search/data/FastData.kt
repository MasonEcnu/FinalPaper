package com.mason.finalpaper.fast_search.data

import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import com.mason.keyword.KeywordsExtractor
import com.mason.utils.FileUtil

class FastData {
  companion object {
    val Documents = FileUtil.readDirFiles(ORIGINAL_FILES_DIC_PATH)
    val Word2Doc = KeywordsExtractor.invertedIndexByTextRank(ORIGINAL_FILES_DIC_PATH)
  }
}