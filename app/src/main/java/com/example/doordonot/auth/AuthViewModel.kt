package com.example.doordonot.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doordonot.model.AuthRepository
import com.example.doordonot.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
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
                        loadCurrentUser()
                        onSuccess() // 로그인 성공 시 콜백 호출
                    } else {
                        _errorMessage.value = "로그인에 실패했습니다."
                    }
                }
            }
        }
    }

    // 현재 사용자 정보 로드
    fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser { user ->
                _currentUser.value = user
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