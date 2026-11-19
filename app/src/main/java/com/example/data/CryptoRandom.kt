package com.example.data

import java.security.SecureRandom

object CryptoRandom {
    private val secureRandom = SecureRandom()

    fun <T> shuffled(items: List<T>): List<T> {
        if (items.size <= 1) return items.toList()
        val copy = items.toMutableList()
        for (i in copy.lastIndex downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val tmp = copy[i]
            copy[i] = copy[j]
            copy[j] = tmp
        }
        return copy
    }

    fun <T> pickOne(items: List<T>): T? {
        if (items.isEmpty()) return null
        return items[secureRandom.nextInt(items.size)]
    }
}
