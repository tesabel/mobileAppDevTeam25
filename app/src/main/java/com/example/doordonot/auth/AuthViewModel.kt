//auth/AuthViewModel

package com.example.doordonot.auth

import User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doordonot.model.AuthRepository
import com.example.doordonot.model.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val habitRepository: HabitRepository = HabitRepository()
) : ViewModel() {

    // UI 상태를 관리하는 StateFlow
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser


    private val _showDateAlert = MutableStateFlow<String?>(null)
    val showDateAlert: StateFlow<String?> = _showDateAlert

    init {
        loadCurrentUser()
    }

    // 이름 입력 업데이트
    fun onNameChange(newName: String) {
        _name.value = newName
    }

    // 이메일 입력 업데이트
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    // 비밀번호 입력 업데이트
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    // 비밀번호 확인 입력 업데이트
    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
    }

    // 회원가입 로직
    fun signUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when {
                _name.value.isBlank() -> {
                    _errorMessage.value = "이름을 입력해주세요."
                }
                !isValidEmail(_email.value) -> {
                    _errorMessage.value = "유효한 이메일 주소를 입력해주세요."
                }
                !isValidPassword(_password.value) -> {
                    _errorMessage.value = "비밀번호는 8자 이상이어야 합니다."
                }
                _password.value != _confirmPassword.value -> {
                    _errorMessage.value = "비밀번호가 일치하지 않습니다."
                }
                else -> {
                    authRepository.signUp(_name.value, _email.value, _password.value) { success ->
                        if (success) {
                            _errorMessage.value = ""
                            loadCurrentUser()
                            onSuccess() // 회원가입 성공 시 콜백 호출
                        } else {
                            _errorMessage.value = "회원가입에 실패했습니다."
                        }
                    }
                }
            }
        }
    }

    // 로그인 로직
    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _errorMessage.value = "이메일과 비밀번호를 모두 입력해주세요."
            } else {
                authRepository.signIn(_email.value, _password.value) { success ->
                    if (success) {
                        _errorMessage.value = ""
                        // 로그인 성공 후 User 정보를 로드한 뒤 날짜 처리
                        loadCurrentUser {
                            handleDateUpdate() // 날짜 관련 처리 함수 호출
                            onSuccess() // 화면 이동 등 처리
                        }
                    } else {
                        _errorMessage.value = "로그인에 실패했습니다."
                    }
                }
            }
        }
    }

    // 날짜 처리 함수
    private fun handleDateUpdate() {
        val user = _currentUser.value ?: return
        val currentDate = com.example.doordonot.Config.getCurrentDate()
        val lastUpdatedDate = user.lastUpdatedDate
        val mode = if (com.example.doordonot.Config.useTestDate) "테스트모드" else "현재날짜모드"

        // 반갑습니다! 알림 및 콘솔 출력
        println("반갑습니다! 모드: $mode, 오늘날짜: $currentDate")

        if (lastUpdatedDate.isBlank()) {
            // lastUpdatedDate가 없으면 현재 날짜로 설정
            updateUserLastUpdatedDate(currentDate)
            _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $currentDate"
        } else {
            if (lastUpdatedDate != currentDate) {
                // 날짜가 다르면 updateDate 실행
                updateDate(lastUpdatedDate, currentDate, mode)
            } else {
                // 날짜가 같으면 기본 환영메시지만
                _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $currentDate"
            }
        }
    }


    // 날짜 갱신 함수
// auth/AuthViewModel.kt

// updateDate 함수 내 사이 날짜 successDates 갱신 로직 추가
// handleDateUpdate와 updateDate 함수 내에서 마지막 부분에 추가

    private fun updateDate(oldDate: String, newDate: String, mode: String) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val old = dateFormat.parse(oldDate)
        val new = dateFormat.parse(newDate)
        val diff = ((new.time - old.time) / (1000 * 60 * 60 * 24)).toInt()

        if (diff > 0) {
            val alertMessage = "다시 돌아오셨군요! ${diff}일만에 접속하셨네요!"
            println(alertMessage)
            val calendar = java.util.Calendar.getInstance()
            calendar.time = old
            for (i in 1..diff) {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val logDate = dateFormat.format(calendar.time)
                println(logDate) // 각 날짜 콘솔로그로 출력
            }

            // 3. 모든 습관에 대해 successDates 업데이트
            val user = _currentUser.value ?: return
            viewModelScope.launch {
                // lastUpdatedDate 변경 전에 사이 날짜 success 갱신
                habitRepository.updateAllHabitsSuccessDates(user.uid, oldDate, newDate)

                // lastUpdatedDate를 newDate로 갱신
                updateUserLastUpdatedDate(newDate)
            }

            _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $newDate\n$alertMessage"
        } else {
            _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $newDate"
        }
    }

    // lastUpdatedDate DB 업데이트 함수
    private fun updateUserLastUpdatedDate(newDate: String) {
        val user = _currentUser.value ?: return
        authRepository.updateLastUpdatedDate(user.uid, newDate) { success ->
            if (success) {
                println("User lastUpdatedDate updated to $newDate")
            } else {
                println("Failed to update lastUpdatedDate")
            }
        }
    }

    // 알림 닫기 처리 함수 추가
    fun onDateAlertDismissed() {
        _showDateAlert.value = null
    }


    // 현재 사용자 정보 로드
    fun loadCurrentUser(onLoaded: (() -> Unit)? = null) {
        viewModelScope.launch {
            authRepository.getCurrentUser { user ->
                _currentUser.value = user
                onLoaded?.invoke()
            }
        }
    }

    // 이메일 유효성 검사
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return email.matches(emailRegex)
    }

    // 비밀번호 유효성 검사
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}