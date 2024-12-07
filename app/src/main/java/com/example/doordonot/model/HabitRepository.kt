package com.example.doordonot.model

import com.google.android.gms.tasks.Tasks
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
            .addOnSuccessListener {
                println("HabitRepository: Habit added successfully with id ${habitWithId.id}")
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to add habit: ${exception.message}")
                onComplete(false)
            }
    }

    // 습관 목록 가져오기
    fun getHabits(userId: String, onResult: (List<Habit>) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .addOnSuccessListener { snapshot ->
                val habits = snapshot.documents.mapNotNull { doc ->
                    val habit = doc.toObject(Habit::class.java)
                    habit?.copy(id = doc.id) // doc.id를 habit.id에 설정
                }
                println("HabitRepository: Loaded habits: $habits")
                onResult(habits)
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to get habits: ${exception.message}")
                onResult(emptyList())
            }
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
                println("HabitRepository: DailyStatus updated for date ${dailyStatus.date}")
                habitRef.collection("dailyStatus")
                    .orderBy("date")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val streak = calculateStreak(snapshot)
                        habitRef.update("streak", streak)
                            .addOnSuccessListener {
                                println("HabitRepository: Streak updated to $streak")
                                onComplete(true)
                            }
                            .addOnFailureListener { exception ->
                                println("HabitRepository: Failed to update streak: ${exception.message}")
                                onComplete(false)
                            }
                    }
                    .addOnFailureListener { exception ->
                        println("HabitRepository: Failed to get daily statuses for streak calculation: ${exception.message}")
                        onComplete(false)
                    }
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to update DailyStatus: ${exception.message}")
                onComplete(false)
            }
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
                println("HabitRepository: Loaded dailyStatuses: $dailyStatuses")
                onResult(dailyStatuses)
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to get dailyStatuses: ${exception.message}")
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
                    println("HabitRepository: Loaded existing DailyStatus: $status")
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
                                println("HabitRepository: Initializing DailyStatus: $initialStatus")
                                dailyStatusRef.set(initialStatus)
                                    .addOnSuccessListener {
                                        println("HabitRepository: Initialized DailyStatus successfully")
                                        onResult(initialStatus)
                                    }
                                    .addOnFailureListener { exception ->
                                        println("HabitRepository: Failed to initialize DailyStatus: ${exception.message}")
                                        onResult(null)
                                    }
                            } else {
                                println("HabitRepository: Failed to get Habit for initializing DailyStatus")
                                onResult(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("HabitRepository: Failed to get Habit for initializing DailyStatus: ${exception.message}")
                            onResult(null)
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to get DailyStatus: ${exception.message}")
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
                                    println("HabitRepository: Batch set DailyStatus for habitId: ${habit.id}, date: $todayDate")
                                }
                            }
                            .addOnFailureListener { exception ->
                                println("HabitRepository: Failed to get DailyStatus for habitId: ${habit.id}, date: $todayDate: ${exception.message}")
                            }
                        tasks.add(task)
                    }
                }

                // 모든 get 작업이 완료된 후 batch commit
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        batch.commit()
                            .addOnSuccessListener {
                                println("HabitRepository: Batch commit succeeded for initializeTodayDailyStatuses")
                                onComplete(true)
                            }
                            .addOnFailureListener { exception ->
                                println("HabitRepository: Batch commit failed for initializeTodayDailyStatuses: ${exception.message}")
                                onComplete(false)
                            }
                    }
                    .addOnFailureListener { exception ->
                        println("HabitRepository: WhenAllComplete failed for initializeTodayDailyStatuses: ${exception.message}")
                        onComplete(false)
                    }
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to get habits for initializeTodayDailyStatuses: ${exception.message}")
                onComplete(false)
            }
    }

    // Helper 함수: 현재 날짜 가져오기
    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    // 특정 날짜의 DailyStatus isChecked 값을 변경하는 함수
    fun toggleDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        onComplete: (Boolean) -> Unit
    ) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        val dailyStatusRef = habitRef.collection("dailyStatus").document(date)

        dailyStatusRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // 기존 DailyStatus 객체 가져오기
                    val currentStatus = snapshot.toObject(DailyStatus::class.java)
                    println("HabitRepository: CurrentDailyStatus: $currentStatus")
                    if (currentStatus != null) {
                        // isChecked 값 반전
                        val updatedStatus = currentStatus.copy(isChecked = !currentStatus.isChecked)
                        println("HabitRepository: UpdatedDailyStatus: $updatedStatus")

                        // Firestore에 업데이트
                        dailyStatusRef.set(updatedStatus)
                            .addOnSuccessListener {
                                println("HabitRepository: toggleDailyStatus succeeded")
                                onComplete(true)
                            }
                            .addOnFailureListener { exception ->
                                println("HabitRepository: toggleDailyStatus failed: ${exception.message}")
                                onComplete(false)
                            }
                    } else {
                        println("HabitRepository: toggleDailyStatus failed: currentStatus is null")
                        onComplete(false)
                    }
                } else {
                    // 상태가 없으면 초기화 후 저장
                    val initialStatus = DailyStatus(date = date, isChecked = true)
                    println("HabitRepository: DailyStatus does not exist, initializing with $initialStatus")
                    dailyStatusRef.set(initialStatus)
                        .addOnSuccessListener {
                            println("HabitRepository: Initialized DailyStatus successfully")
                            onComplete(true)
                        }
                        .addOnFailureListener { exception ->
                            println("HabitRepository: Failed to initialize DailyStatus: ${exception.message}")
                            onComplete(false)
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("HabitRepository: Failed to get DailyStatus: ${exception.message}")
                onComplete(false)
            }

    }

    // 연속 성공 계산
    private fun calculateStreak(snapshot: QuerySnapshot): Int {
        val sortedStatuses = snapshot.toObjects(DailyStatus::class.java)
            .sortedByDescending { it.date }
        var streak = 0
        for (status in sortedStatuses) {
            if (status.isChecked) streak++ else break
        }
        println("HabitRepository: Calculated streak: $streak")
        return streak
    }
}