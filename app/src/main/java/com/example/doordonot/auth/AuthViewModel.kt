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

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
    }

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
                            onSuccess()
                        } else {
                            _errorMessage.value = "회원가입에 실패했습니다."
                        }
                    }
                }
            }
        }
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _errorMessage.value = "이메일과 비밀번호를 모두 입력해주세요."
            } else {
                authRepository.signIn(_email.value, _password.value) { success ->
                    if (success) {
                        _errorMessage.value = ""
                        loadCurrentUser {
                            handleDateUpdate()
                            onSuccess()
                        }
                    } else {
                        _errorMessage.value = "로그인에 실패했습니다."
                    }
                }
            }
        }
    }

    private fun handleDateUpdate() {
        val user = _currentUser.value ?: return
        val currentDate = com.example.doordonot.Config.getCurrentDate()
        val lastUpdatedDate = user.lastUpdatedDate
        val mode = if (com.example.doordonot.Config.useTestDate) "테스트모드" else "현재날짜모드"

        println("반갑습니다! 모드: $mode, 오늘날짜: $currentDate")

        if (lastUpdatedDate.isBlank()) {
            updateUserLastUpdatedDate(currentDate)
            _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $currentDate"
        } else {
            if (lastUpdatedDate != currentDate) {
                updateDate(lastUpdatedDate, currentDate, mode)
            } else {
                _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $currentDate"
            }
        }
    }

    private fun updateDate(oldDate: String, newDate: String, mode: String) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val old = dateFormat.parse(oldDate)
        val new = dateFormat.parse(newDate)
        val diff = ((new.time - old.time) / (1000 * 60 * 60 * 24)).toInt()

        // 현재 사용자 정보가 없으면 중단
        val user = _currentUser.value ?: return

        // 날짜 차이에 상관없이 항상 오늘 날짜까지 갱신
        viewModelScope.launch {
            habitRepository.updateAllHabitsSuccessDates(user.uid, oldDate, newDate)
            updateUserLastUpdatedDate(newDate)
        }

        if (diff > 0) {
            val alertMessage = "다시 돌아오셨군요! ${diff}일만에 접속하셨네요!"
            println(alertMessage)
            val calendar = java.util.Calendar.getInstance()
            calendar.time = old
            for (i in 1..diff) {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val logDate = dateFormat.format(calendar.time)
                println(logDate)
            }

            _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $newDate\n$alertMessage"
        } else {
            _showDateAlert.value = "반갑습니다!\n모드: $mode\n오늘날짜: $newDate"
        }
    }


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

    fun onDateAlertDismissed() {
        _showDateAlert.value = null
    }

    fun loadCurrentUser(onLoaded: (() -> Unit)? = null) {
        viewModelScope.launch {
            authRepository.getCurrentUser { user ->
                _currentUser.value = user
                onLoaded?.invoke()
                // 여기서 이미 로그인된 사용자가 있을 경우 handleDateUpdate 호출
                if (user != null) {
                    handleDateUpdate()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}
