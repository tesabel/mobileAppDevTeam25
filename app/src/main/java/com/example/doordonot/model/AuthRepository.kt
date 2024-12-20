//model/AuthRepository

package com.example.doordonot.model

import User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 회원가입
    fun signUp(name: String, email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentDate = com.example.doordonot.Config.getCurrentDate()
                val user = User(auth.currentUser!!.uid, name, lastUpdatedDate = currentDate)
                db.collection("users").document(user.uid).set(user).addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    // lastUpdatedDate를 업데이트하는 함수
    fun updateLastUpdatedDate(userId: String, newDate: String, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(userId)
            .update("lastUpdatedDate", newDate)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 로그인
    fun signIn(email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    // 현재 사용자 정보 가져오기
    fun getCurrentUser(onResult: (User?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    onResult(document.toObject(User::class.java))
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } else {
            onResult(null)
        }
    }
}