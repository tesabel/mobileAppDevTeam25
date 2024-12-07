package com.example.doordonot.model

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

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

    // 날짜별 습관 조회
    fun getDailyStatuses(
        habitId: String,
        userId: String,
        onResult: (List<DailyStatus>) -> Unit
    ) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        habitRef.collection("dailyStatus")
            .orderBy("date") // 날짜별 정렬
            .get()
            .addOnSuccessListener { snapshot ->
                val dailyStatuses = snapshot.toObjects(DailyStatus::class.java)
                onResult(dailyStatuses)
            }
            .addOnFailureListener {
                onResult(emptyList()) // 실패 시 빈 리스트 반환
            }
    }

    // 특정 날짜의 DailyStatus를 가져오거나 초기화하는 함수
    fun getOrInitializeDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        onResult: (DailyStatus?) -> Unit
    ) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        val dailyStatusRef = habitRef.collection("dailyStatus").document(date)

        dailyStatusRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val status = snapshot.toObject(DailyStatus::class.java)
                    onResult(status)
                } else {
                    // 습관 타입에 따라 초기 상태 설정
                    habitRef.get()
                        .addOnSuccessListener { habitSnapshot ->
                            val habit = habitSnapshot.toObject(Habit::class.java)
                            if (habit != null) {
                                val initialStatus = if (habit.type == HabitType.MAINTAIN) {
                                    DailyStatus(date = date, isChecked = true)
                                } else { // FORMING
                                    DailyStatus(date = date, isChecked = false)
                                }
                                dailyStatusRef.set(initialStatus)
                                    .addOnSuccessListener {
                                        onResult(initialStatus)
                                    }
                                    .addOnFailureListener {
                                        onResult(null)
                                    }
                            } else {
                                onResult(null)
                            }
                        }
                        .addOnFailureListener {
                            onResult(null)
                        }
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // 오늘 날짜의 DailyStatus를 초기화하는 함수
    fun initializeTodayDailyStatuses(userId: String, onComplete: (Boolean) -> Unit) {
        val todayDate = getCurrentDate()
        val userHabitsRef = db.collection("users").document(userId).collection("habits")

        userHabitsRef.get()
            .addOnSuccessListener { habitsSnapshot ->
                val batch = db.batch()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                habitsSnapshot.documents.forEach { habitDoc ->
                    val habit = habitDoc.toObject(Habit::class.java)
                    if (habit != null) {
                        val dailyStatusRef = habitDoc.reference.collection("dailyStatus").document(todayDate)
                        // DailyStatus가 존재하지 않으면 초기화
                        val task = dailyStatusRef.get()
                            .addOnSuccessListener { statusSnapshot ->
                                if (!statusSnapshot.exists()) {
                                    val initialStatus = if (habit.type == HabitType.MAINTAIN) {
                                        DailyStatus(date = todayDate, isChecked = true)
                                    } else { // FORMING
                                        DailyStatus(date = todayDate, isChecked = false)
                                    }
                                    batch.set(dailyStatusRef, initialStatus)
                                }
                            }
                            .addOnFailureListener {
                                // 개별 습관에 대한 실패 처리 (옵션)
                            }
                        tasks.add(task)
                    }
                }

                // 모든 get 작업이 완료된 후 batch commit
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        batch.commit()
                            .addOnSuccessListener {
                                onComplete(true)
                            }
                            .addOnFailureListener {
                                onComplete(false)
                            }
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    // Helper 함수: 현재 날짜 가져오기
    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
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

    // 드롭
    suspend fun updateHabitType(habitId: String, newType: String, callback: (Boolean) -> Unit) {
        try {
            // 타입 업데이트
            val habitRef = db.collection("habits").document(habitId)
            habitRef.update("type", newType).await()
            callback(true)
        } catch (e: Exception) {
            callback(false)
        }
    }

}
