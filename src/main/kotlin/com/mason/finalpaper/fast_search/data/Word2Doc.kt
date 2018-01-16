package com.mason.finalpaper.fast_search.data

val Word2Doc = mapOf<String, List<String>>(
    "cat" to mutableListOf("doc1", "doc3", "doc5"),
    "hat" to mutableListOf("doc2", "doc4", "doc10"),
    "book" to mutableListOf("doc1", "doc4", "doc8"),
    "car" to mutableListOf("doc3", "doc5", "doc6"),
    "hat" to mutableListOf("doc1", "doc3", "doc6"),
    "bamboo" to mutableListOf("doc10", "doc3", "doc5"),
    "like" to mutableListOf("doc9", "doc2", "doc4"),
    "computer" to mutableListOf("doc6", "doc4", "doc5"),
    "hammer" to mutableListOf("doc7", "doc5", "doc6"),
    "letter" to mutableListOf("doc8", "doc3", "doc6"),
    "goods" to mutableListOf("doc9", "doc3", "doc5"),
    "hug" to mutableListOf("doc10", "doc2", "doc4"),
    "truth" to mutableListOf("doc2", "doc4", "doc5"),
    "banana" to mutableListOf("doc4", "doc5", "doc6")
)

val Documents = mapOf<String, String>(
    "doc1" to "I have a white cat.",
    "doc2" to "I have a black hat.",
    "doc3" to "I like eat fruit.",
    "doc4" to "I live in China.",
    "doc5" to "I just went to bar once.",
    "doc6" to "I read many books last year.",
    "doc7" to "I love money.",
    "doc8" to "The shape of the Earth is a ball.",
    "doc9" to "I like cartoon of Japan.",
    "doc10" to "The Earth is our home."
)