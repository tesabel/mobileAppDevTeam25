
package com.example.doordonot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.R
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.DailyStatus
import com.example.doordonot.model.HabitType
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun HabitManagementPage(
    navController: NavController,
    habitViewModel: HabitViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val habits by habitViewModel.habits.collectAsState()
    val user by authViewModel.currentUser.collectAsState()

    user?.let { currentUser ->
        LaunchedEffect(currentUser.uid) {
            habitViewModel.loadHabits(currentUser.uid)
        }

        Scaffold(
            topBar = { TopBar(title = "습관 관리") },
            bottomBar = { BottomNavigationBar(navController) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("make_habit") },
                    containerColor = colorResource(id = R.color.blue),
                    contentColor = colorResource(id = R.color.white),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                }
            }
        ) { padding ->
            // 형성중인 습관
            val doList: List<com.example.doordonot.model.Habit> =
                habits.filter { it.type.name == "FORMING" }
            val donotList: List<com.example.doordonot.model.Habit> =
                habits.filter { it.type.name == "MAINTAIN" }

            // 오늘 날짜
            val today = LocalDate.now()
            LongPressDraggable(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                       // .background(colorResource(id = R.color.beige))
                        .padding(padding)
                        .padding(4.dp)
                ) {
                    Column {
                        // 날짜 표시
                        Text(
                            text = "${today}",
                            modifier = Modifier.padding(16.dp),
                            style = typography.headlineMedium.copy()
                        )

                        // 습관 리스트 표시
                        Row {
                            // Do list (형성 중인 습관)
                            DropTarget<com.example.doordonot.model.Habit>(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(start = 8.dp, end = 4.dp),
                                onDrop = { habit ->
                                    // 습관을 형성 중으로 변경
                                    habitViewModel.updateHabitType(habit.id, currentUser.uid, "MAINTAIN")
                                }
                            ) { isInBound, _ ->
                                List(
                                    modifier = Modifier.fillMaxSize(),
                                    title = "형성 중인 습관",
                                    uid = currentUser.uid,
                                    items = doList
                                )
                            }

                            // Donot list (유지 중인 습관)
                            DropTarget<com.example.doordonot.model.Habit>(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(start = 4.dp, end = 8.dp),
                                onDrop = { habit ->
                                    // 습관을 유지 중으로 변경
                                    habitViewModel.updateHabitType(habit.id, currentUser.uid, "FORMING")
                                }
                            ) { isInBound, _ ->
                                List(
                                    modifier = Modifier.fillMaxSize(),
                                    title = "유지 중인 습관",
                                    items = donotList,
                                    uid = currentUser.uid
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 해더 + 리스트 컬럼
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun List(
    modifier: Modifier = Modifier,
    uid: String,
    title: String,
    items: List<com.example.doordonot.model.Habit>
) {
    Column(modifier = modifier.padding(bottom = 48.dp)) {
        LazyColumn(modifier = modifier) {
            // 리스트 헤더
            stickyHeader {
                Text(
                    textAlign = TextAlign.Center,
                    text = title,
                    style = typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            if (title == "형성 중인 습관") colorResource(id = R.color.do_habit)
                            else colorResource(id = R.color.donot_habit)
                        )
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                )
            }
            // 리스트 내용
            items(items) { item ->
                DisplayingList(habit = item, userId = uid)
                Divider(modifier = Modifier.height(1.dp))
            }
        }
    }
}

// 리스트 표시
@Composable
fun DisplayingList(
    habit: com.example.doordonot.model.Habit,
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: HabitViewModel = viewModel()
) {
    val today = LocalDate.now().toString()
    var isClicked by rememberSaveable { mutableStateOf(false) }
    var isCheckedToday by rememberSaveable { mutableStateOf(habit.type == HabitType.MAINTAIN) }
    var dailyStatus by remember { mutableStateOf(DailyStatus(date = today, isChecked = isCheckedToday)) }

    LaunchedEffect(habit) {
        isCheckedToday = habit.type == HabitType.MAINTAIN
        dailyStatus = DailyStatus(date = today, isChecked = isCheckedToday)
    }

    DragTarget(modifier = Modifier, dataToDrop = habit) {
        ElevatedCard(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxSize(),
            colors = CardDefaults.cardColors(
              //  colorResource(id = R.color.beige)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = modifier
                        .padding(8.dp)
                        .weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 카테고리 표시
                        OutlinedCard(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            border = BorderStroke(
                                width = 2.dp,
                                color = when (habit.category) {
                                    "금지" -> Color(248, 84, 83)
                                    "운동" -> Color(0, 150, 136, 255)
                                    "공부" -> Color(13, 146, 244)
                                    else -> Color(255, 193, 7, 255)
                                }
                            ),
                            colors = CardDefaults.cardColors(
                                when (habit.category) {
                                    "금지" -> Color(248, 84, 83, 100)
                                    "운동" -> Color(0, 150, 136, 100)
                                    "공부" -> Color(13, 146, 244, 100)
                                    else -> Color(255, 193, 7, 100)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .padding(vertical = 2.dp),
                                text = habit.category,
                                style = typography.bodySmall
                            )
                        }

                        Text(
                            text = habit.name,
                            maxLines = if (isClicked) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { isClicked = !isClicked }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "연속 성공 : ${habit.streak}일",
                        style = typography.bodySmall
                    )
                }


                //-----------------------------체크박스 로직 -----------------------------------
                Checkbox(
                    checked = dailyStatus.isChecked,
                    onCheckedChange = { isChecked ->
                        isCheckedToday = isChecked
                        dailyStatus = DailyStatus(date = today, isChecked = isChecked)
                        //체크박스 선택 시 실행 함수
                        viewModel.updateDailyStatus(habit.id, userId, dailyStatus) // DailyStatus 전달
                    }
                )
//---------------------------------------------------------------------


                IconButton(onClick = {
                    // 삭제 로직 추가
                    viewModel.deleteHabit(habit.id, userId)
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Habit")
                }
            }
        }
    }
}

// ---------------------------- 드래그/드롭 함수 구현 -----------------------

// 드래그 타겟 상태 정보
internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
    var itemSize by mutableStateOf(IntSize.Zero)
}

// 드래그 가능한 뷰의 상태만 저장
internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

@Composable
fun <T> DragTarget(
    modifier: Modifier,
    dataToDrop: T,
    content: @Composable () -> Unit
) {
    var itemSize by remember { mutableStateOf(IntSize.Zero) }
    var initialPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragTargetInfo.current

    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                initialPosition = layoutCoordinates.localToRoot(Offset.Zero)
                itemSize = layoutCoordinates.size
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        currentState.dataToDrop = dataToDrop
                        currentState.isDragging = true
                        currentState.dragPosition = initialPosition
                        currentState.dragOffset = Offset.Zero
                        currentState.draggableComposable = content
                        currentState.itemSize = itemSize
                    },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        currentState.isDragging = false
                        currentState.dragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        currentState.isDragging = false
                        currentState.dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        content()
    }
}

// 드래그 개체 복사하여 드래그
@Composable
fun LongPressDraggable(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state
    ) {
        Box(
            modifier = modifier
        ) {
            content()
            if (state.isDragging) {
                // 드래그하는 동안 원래 항목 크기를 유지
                Box(
                    modifier = Modifier
                        .size(
                            width = with(LocalDensity.current) { state.itemSize.width.toDp() },
                            height = with(LocalDensity.current) { state.itemSize.height.toDp() }
                        )
                        .graphicsLayer {
                            translationX = state.dragPosition.x + state.dragOffset.x
                            translationY = state.dragPosition.y + state.dragOffset.y
                            scaleX = 1.2f
                            scaleY = 1.2f
                            alpha = if (state.itemSize == IntSize.Zero) 0f else .9f
                        }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
        }
    }
}

// ----------------------------- 드롭 --------------------
// 드롭 데이터 수신

@Composable
fun <T> DropTarget(
    modifier: Modifier = Modifier,
    onDrop: (T) -> Unit = {},
    content: @Composable BoxScope.(isInBound: Boolean, data: T?) -> Unit
) {
    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember { mutableStateOf(false) }
    var isPopupVisible by remember { mutableStateOf(false) } // 팝업 상태

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val rect = coordinates.boundsInWindow()
                isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
            }
    ) {
        val data = if (isCurrentDropTarget && !dragInfo.isDragging) dragInfo.dataToDrop as T? else null
        content(isCurrentDropTarget, data)

        // 팝업 표시
        if (isPopupVisible) {
            AlertDialog(
                onDismissRequest = { isPopupVisible = false },
                title = { Text(text = "습관 이동 확인") },
                text = { Text("이 습관을 이동하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        if (data != null)
                            onDrop(data)
                        dragInfo.dataToDrop = null
                        isPopupVisible = false
                    }) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isPopupVisible = false }) {
                        Text("취소")
                    }
                }
            )
        }

        // 드롭이 가능하고 드래그가 끝나면 팝업을 띄움
        LaunchedEffect(isCurrentDropTarget, dragInfo.isDragging) {
            if (isCurrentDropTarget && !dragInfo.isDragging && dragInfo.dataToDrop != null) {
                isPopupVisible = true
            }
        }
        LaunchedEffect(dragInfo.isDragging) {
            if (!dragInfo.isDragging) {
                dragInfo.dragOffset = Offset.Zero // dragOffset 리셋
            }
        }
    }
}
