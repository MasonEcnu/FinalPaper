package com.mason.finalpaper.slow_search.data

import com.mason.constants.ORIGINAL_FILES_DIC_PATH
import com.mason.keyword.KeywordsExtractor
import com.mason.utils.FileUtil

class SlowData {
  companion object {
    val Documents = FileUtil.readDirFiles(ORIGINAL_FILES_DIC_PATH)
    val Doc2Word = KeywordsExtractor.forwardIndexByTextRank(ORIGINAL_FILES_DIC_PATH)
  }
}