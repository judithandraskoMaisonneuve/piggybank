package com.example.piggybank_projet3

data class Goal(
    val id_goal: String = "",
    val name: String = "",
    val deadline: String = "",
    val amountNeeded: Double = 0.0,
    val progress: Double = 0.0,
    var savedAmount: Double = 0.0
)
