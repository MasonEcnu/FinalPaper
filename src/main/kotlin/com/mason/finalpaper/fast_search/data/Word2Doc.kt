package com.mason.finalpaper.fast_search.data

val Word2Doc = mapOf<String, List<String>>(
    "cat" to mutableListOf("doc1", "doc3", "doc5"),
    "hat" to mutableListOf("doc2", "doc4", "doc10"),
    "like" to mutableListOf("doc12", "doc4", "doc8"),
    "China" to mutableListOf("doc14", "doc15", "doc6"),
    "bar" to mutableListOf("doc11", "doc3", "doc6"),
    "book" to mutableListOf("doc10", "doc3", "doc5"),
    "love" to mutableListOf("doc9", "doc2", "doc4"),
    "shape" to mutableListOf("doc6", "doc4", "doc5"),
    "Japan" to mutableListOf("doc7", "doc5", "doc6"),
    "Earth" to mutableListOf("doc8", "doc3", "doc6"),
    "goods" to mutableListOf("doc9", "doc3", "doc5"),
    "food" to mutableListOf("doc10", "doc2", "doc4"),
    "soup" to mutableListOf("doc12", "doc4", "doc5"),
    "tea" to mutableListOf("doc14", "doc5", "doc6"),
    "name" to mutableListOf("doc11", "doc3", "doc6")
)

val docs = mapOf<String, String>(
    "doc1" to "I have a white cat.",
    "doc2" to "I have a black hat.",
    "doc3" to "I like eat fruit.",
    "doc4" to "I live in China.",
    "doc5" to "I just went to bar once.",
    "doc6" to "I read many books last year.",
    "doc7" to "I love money.",
    "doc8" to "The shape of the Earth is a ball.",
    "doc9" to "I like cartoon of Japan.",
    "doc10" to "The Earth is our home.",
    "doc11" to "There are all kinds of goods in SuperMarket.",
    "doc12" to "Food is necessary for us.",
    "doc13" to "I like to drink fish soup.",
    "doc14" to "I don't like to drink tea.",
    "doc15" to "My name is Mason."
)