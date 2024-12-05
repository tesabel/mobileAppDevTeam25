package com.example.doordonot.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class HabitRepository {
    private val db = FirebaseFirestore.getInstance()

    // 습관 추가
    fun addHabit(habit: Habit, userId: String, onComplete: (Boolean) -> Unit) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document() // 고유한 문서 ID 자동 생성

        val habitWithId = habit.copy(id = habitRef.id) // 생성된 ID를 Habit 객체에 설정

        habitRef.set(habitWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 습관 목록 가져오기
    fun getHabits(userId: String, onResult: (List<Habit>) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .addOnSuccessListener { snapshot ->
                val habits = snapshot.toObjects(Habit::class.java)
                onResult(habits)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // 습관 상태 업데이트
    fun updateDailyStatus(
        habitId: String,
        userId: String,
        dailyStatus: DailyStatus,
        onComplete: (Boolean) -> Unit
    ) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        habitRef.collection("dailyStatus")
            .document(dailyStatus.date)
            .set(dailyStatus)
            .addOnSuccessListener {
                habitRef.collection("dailyStatus")
                    .orderBy("date")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val streak = calculateStreak(snapshot)
                        habitRef.update("streak", streak)
                            .addOnSuccessListener { onComplete(true) }
                            .addOnFailureListener { onComplete(false) }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    // 연속 성공 계산
    private fun calculateStreak(snapshot: QuerySnapshot): Int {
        val sortedStatuses = snapshot.toObjects(DailyStatus::class.java)
            .sortedByDescending { it.date }
        var streak = 0
        for (status in sortedStatuses) {
            if (status.isChecked) streak++ else break
        }
        return streak
    }
}