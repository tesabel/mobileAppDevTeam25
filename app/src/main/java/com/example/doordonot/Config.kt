// doordonot/Config.kt (신규 파일 추가)
package com.example.doordonot

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Config {
    // 테스트 모드 여부를 결정하는 변수
    var useTestDate: Boolean = true

    // 테스트 모드에서 사용할 날짜
    var testDate: String = "2024-12-09"
    // 현재 날짜를 반환하는 함수
    fun getCurrentDate(): String {
        return if (useTestDate) {
            testDate
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }
    }
}
