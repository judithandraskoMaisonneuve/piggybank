package com.example.piggybank_projet3

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Classe utilisee pour obtenir les donnees de FireStore

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String?
        get() = auth.currentUser?.uid

    suspend fun getGoals(): List<Goal> {
        return try {
            userId?.let {
                firestore.collection("Users")
                    .document(it)
                    .collection("goals")
                    .get()
                    .await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(Goal::class.java)?.copy(id_goal = doc.id)
                    }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList() // Handle errors gracefully
        }
    }

    suspend fun getExpenses(): List<Expense> {
        return try {
            userId?.let {
                firestore.collection("Users")
                    .document(it)
                    .collection("expenses")
                    .get()
                    .await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(Expense::class.java)?.copy(id_expense = doc.id)
                    }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getIncomes(): List<Income> {
        return try {
            userId?.let {
                firestore.collection("Users")
                    .document(it)
                    .collection("incomes")
                    .get()
                    .await()
                    .documents.mapNotNull { doc ->
                        doc.toObject(Income::class.java)?.copy(id_income = doc.id)
                    }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
