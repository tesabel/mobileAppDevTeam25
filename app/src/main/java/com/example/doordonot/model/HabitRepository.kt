//HabitRepository

package com.example.doordonot.model

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    fun getHabits(userId: String,
                  onResult: (List<Habit>) -> Unit) {
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
    // 모든 습관을 가져와 해당 날짜의 DailyStatus를 확인하고 HabitDisplay 리스트로 반환하도록 변경
    fun getHabitsForDate(
        userId: String,
        selectedDate: String,
        onResult: (List<HabitDisplay>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userRef = db.collection("users").document(userId).collection("habits")

        userRef.get()
            .addOnSuccessListener { habitsSnapshot ->
                val habitDisplays = mutableListOf<HabitDisplay>()

                // 모든 습관 문서 가져오기
                for (habitDoc in habitsSnapshot.documents) {
                    val habit = habitDoc.toObject(Habit::class.java)
                    if (habit != null) {
                        // 선택된 날짜가 successDates에 포함되어 있는지 확인
                        val isChecked = habit.successDates.contains(selectedDate)
                        // HabitDisplay에 추가
                        habitDisplays.add(HabitDisplay(habit = habit, isChecked = isChecked))
                    }
                }

                // 결과 반환
                onResult(habitDisplays)
            }
            .addOnFailureListener { e ->
                // 에러 처리
                println("HabitRepository: Failed to fetch habits for user: ${e.message}")
                onError("Failed to fetch habits for date $selectedDate: ${e.message}")
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

        // Step 1: update dailyStatus
        habitRef.collection("dailyStatus")
            .document(dailyStatus.date)
            .set(dailyStatus)
            .addOnSuccessListener {
                println("HabitRepository: DailyStatus updated for date ${dailyStatus.date}")

                // Step 2: Get current habit document to access successDates
                habitRef.get()
                    .addOnSuccessListener { habitSnapshot ->
                        if (!habitSnapshot.exists()) {
                            println("HabitRepository: Habit document does not exist")
                            onComplete(false)
                            return@addOnSuccessListener
                        }

                        // 현재 successDates 리스트를 가져옵니다.
                        val currentSuccessDates = habitSnapshot.get("successDates") as? List<String> ?: emptyList()

                        // dailyStatus.isChecked에 따라 successDates 수정
                        val updatedSuccessDates = currentSuccessDates.toMutableList()
                        if (dailyStatus.isChecked) {
                            // 체크되었으면 날짜 추가 (중복 추가 방지)
                            if (!updatedSuccessDates.contains(dailyStatus.date)) {
                                updatedSuccessDates.add(dailyStatus.date)
                            }
                        } else {
                            // 체크해제되었으면 날짜 제거
                            updatedSuccessDates.remove(dailyStatus.date)
                        }

                        // Step 3: updateHabitSuccessInfo 호출
                        // suspend 함수이므로 코루틴 사용 필요
                        // 여기서는 예시로 코루틴 컨텍스트(scope)를 가지고 있다고 가정
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = updateHabitSuccessInfo(userId, habitId, updatedSuccessDates)
                            withContext(Dispatchers.Main) {
                                onComplete(result)
                            }
                        }
                    }
                    .addOnFailureListener {
                        println("HabitRepository: Failed to get habit document. Error: ${it.message}")
                        onComplete(false)
                    }

            }
            .addOnFailureListener { e ->
                println("HabitRepository: Failed to update DailyStatus. Error: ${e.message}")
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
        val todayDate = com.example.doordonot.Config.getCurrentDate()
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

    // 2. successDates와 totalSuccessCount, streak를 업데이트하는 함수 추가
    suspend fun updateHabitSuccessInfo(
        userId: String,
        habitId: String,
        newSuccessDates: List<String>
    ): Boolean {
        return try {
            // successDates를 기반으로 새로운 streak 계산
            val newStreak = calculateStreakFromSuccessDates(newSuccessDates)
            val newTotalSuccess = newSuccessDates.size

            val habitRef = db.collection("users")
                .document(userId)
                .collection("habits")
                .document(habitId)

            val updates = mapOf(
                "successDates" to newSuccessDates,
                "totalSuccessCount" to newTotalSuccess,
                "streak" to newStreak
            )

            habitRef.update(updates).await()
            println("HabitRepository: updateHabitSuccessInfo succeeded. 수정된 streak: $newStreak, totalSuccessCount: $newTotalSuccess")
            true
        } catch (e: Exception) {
            println("HabitRepository: updateHabitSuccessInfo failed: ${e.message}")
            false
        }
    }

    private fun calculateStreakFromSuccessDates(successDates: List<String>): Int {
        if (successDates.isEmpty()) return 0

        val sorted = successDates.sorted()
        val currentDate = com.example.doordonot.Config.getCurrentDate()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val current = dateFormat.parse(currentDate)!!
        val calendar = java.util.Calendar.getInstance()
        calendar.time = current

        // 오늘 날짜 포함 여부
        val includesToday = sorted.contains(currentDate)
        // 오늘 포함 시 streakCount 시작값은 1, 아닐 시 0
        var streakCount = if (includesToday) 1 else 0

        // 어제로 이동
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)

        // 어제부터 거슬러 올라가며 연속 성공일수 계산
        while (true) {
            val checkDate = dateFormat.format(calendar.time)
            if (sorted.contains(checkDate)) {
                streakCount++
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streakCount
    }

    // 2. updateDate 로직에서 호출할 함수: lastUpdatedDate와 currentDate 사이 날짜 처리
    // 유지중(MAINTAIN) 습관은 사이 날짜 모두 successDates에 추가 (없는 경우만)
    // 형성중(FORMING) 습관은 사이 날짜 성공 없음
    suspend fun updateAllHabitsSuccessDates(userId: String, lastUpdatedDate: String, currentDate: String) {
        val habits = getHabitsSuspend(userId)
        if (habits.isEmpty()) return

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val old = dateFormat.parse(lastUpdatedDate)!!
        val new = dateFormat.parse(currentDate)!!

        val diff = ((new.time - old.time) / (1000 * 60 * 60 * 24)).toInt()

        // diff일만큼 사이 날짜 존재
        // oldDate와 newDate 사이의 날짜: oldDate 다음날부터 newDate 전날까지
        // 예: old: 2024-11-01, new: 2024-11-05
        // 사이 날짜: 11-02, 11-03, 11-04

        for (habit in habits) {
            val updatedSuccessDates = habit.successDates.toMutableList()
            if (diff > 1) {
                // old와 new 사이 날짜 처리
                val calendar = java.util.Calendar.getInstance()
                calendar.time = old
                for (i in 1 until diff) {
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    val intermediateDate = dateFormat.format(calendar.time)

                    // MAINTAIN 습관이라면 사이 날짜 모두 성공 처리(이미 있지 않다면 추가)
                    if (habit.type == HabitType.MAINTAIN) {
                        if (!updatedSuccessDates.contains(intermediateDate)) {
                            updatedSuccessDates.add(intermediateDate)
                        }
                    }
                    // FORMING 습관은 사이 날짜 추가 없음(실패로 간주)
                }
            }
            // 업데이트된 successDates로 DB 갱신 및 streak 재계산
            updateHabitSuccessInfo(userId, habit.id, updatedSuccessDates)
        }
    }

    // suspend version of getHabits for convenience
    private suspend fun getHabitsSuspend(userId: String): List<Habit> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("habits")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val habit = doc.toObject(Habit::class.java)
                habit?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    // 특정 날짜의 DailyStatus isChecked 값을 변경하는 함수 (트랜잭션 사용)
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

        db.runTransaction { transaction ->
            val snapshot = transaction.get(dailyStatusRef)
            if (snapshot.exists()) {
                val currentStatus = snapshot.toObject(DailyStatus::class.java)
                println("HabitRepository: CurrentStatus before toggle: $currentStatus")
                if (currentStatus != null) {
                    val updatedStatus = currentStatus.copy(isChecked = !currentStatus.isChecked)
                    transaction.set(dailyStatusRef, updatedStatus)
                    println("HabitRepository: UpdatedStatus after toggle: $updatedStatus")
                } else {
                    throw Exception("HabitRepository: CurrentStatus is null")
                }
            } else {
                // 상태가 없으면 초기화 (isChecked = true)
                val initialStatus = DailyStatus(date = date, isChecked = true)
                transaction.set(dailyStatusRef, initialStatus)
                println("HabitRepository: toggleDailyStatus initialized with isChecked=true")
            }
            true
        }.addOnSuccessListener {
            println("HabitRepository: Transaction succeeded")
            onComplete(true)
        }.addOnFailureListener { e ->
            println("HabitRepository: Transaction failed: ${e.message}")
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

    // 실시간 업데이트 리스너 함수 추가
    fun observeDailyStatuses(
        habitId: String,
        userId: String,
        onResult: (List<DailyStatus>) -> Unit
    ) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        habitRef.collection("dailyStatus")
            .orderBy("date")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    println("HabitRepository: Failed to observe dailyStatuses: ${exception.message}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val dailyStatuses = snapshot.toObjects(DailyStatus::class.java)
                    println("HabitRepository: Observed dailyStatuses: $dailyStatuses")
                    onResult(dailyStatuses)
                }
            }
    }

    // 특정 날짜의 DailyStatus isChecked 값을 설정하는 함수
    fun setDailyStatus(
        habitId: String,
        userId: String,
        date: String,
        isChecked: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        val habitRef = db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        val dailyStatusRef = habitRef.collection("dailyStatus").document(date)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(dailyStatusRef)
            if (snapshot.exists()) {
                val currentStatus = snapshot.toObject(DailyStatus::class.java)
                println("HabitRepository: CurrentStatus before set: $currentStatus")
                if (currentStatus != null) {
                    val updatedStatus = currentStatus.copy(isChecked = isChecked)
                    transaction.set(dailyStatusRef, updatedStatus)
                    println("HabitRepository: UpdatedStatus after set: $updatedStatus")
                } else {
                    throw Exception("HabitRepository: CurrentStatus is null")
                }
            } else {
                // 상태가 없으면 초기화
                val initialStatus = DailyStatus(date = date, isChecked = isChecked)
                transaction.set(dailyStatusRef, initialStatus)
                println("HabitRepository: setDailyStatus initialized with isChecked=$isChecked")
            }
            true
        }.addOnSuccessListener {
            println("HabitRepository: setDailyStatus Transaction succeeded")
            onComplete(true)
        }.addOnFailureListener { e ->
            println("HabitRepository: setDailyStatus Transaction failed: ${e.message}")
            onComplete(false)
        }
    }

    //드롭-상태 업데이트
    suspend fun updateHabitType(
        habitId: String,
        userId: String,
        newType: String
    ): Boolean {
        return try {
            // 예시: Firestore 업데이트
            val habitRef = db.collection("users")
                .document(userId)
                .collection("habits")
                .document(habitId)

            habitRef.update("type", newType).await()
            true
        } catch (e: Exception) {
            Log.e("HabitRepository", "Error updating habit type", e)
            false
        }
    }

    fun observeHabits(userId: String, onResult: (List<Habit>) -> Unit) {
        val habitsRef = FirebaseFirestore.getInstance().collection("users")
            .document(userId).collection("habits")

        habitsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("HabitRepository", "Failed to observe habits", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val habits = snapshot.toObjects(Habit::class.java)
                onResult(habits)
            }
        }
    }


    // 습관 삭제 함수
    suspend fun deleteHabit(habitId: String, userId: String): Boolean {
        val habitRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("habits")
            .document(habitId)

        return try {
            habitRef.delete().await() // suspend 함수로 변경하여 작업 완료 대기
            Log.d("HabitRepository", "습관 삭제 성공")
            true
        } catch (e: Exception) {
            Log.e("HabitRepository", "습관 삭제 실패", e)
            false
        }
    }

}