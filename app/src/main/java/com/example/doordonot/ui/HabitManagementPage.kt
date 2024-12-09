package com.example.doordonot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.doordonot.Config.getCurrentDate
import com.example.doordonot.R
import com.example.doordonot.auth.AuthViewModel
import com.example.doordonot.model.DailyStatus
import com.example.doordonot.model.Habit
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HabitManagementPage(
    navController: NavController,
    habitViewModel: HabitViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val habits by habitViewModel.habits.collectAsState()
    val user by authViewModel.currentUser.collectAsState()

    // 로딩 상태 및 완료 메시지 상태
    var isLoading by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

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
            },
            snackbarHost = {
                if (showMessage) {
                    SnackbarHost(
                        hostState = androidx.compose.material3.SnackbarHostState()
                    ) {
                        Snackbar(
                            actionOnNewLine = false,
                            content = {
                                Text("수정 완료되었습니다.")
                            }
                        )
                    }
                }
            }
        ) { padding ->
            val doList: List<Habit> = habits.filter { it.type.name == "FORMING" }
            val donotList: List<Habit> = habits.filter { it.type.name == "MAINTAIN" }

            val today = getCurrentDate()

            LongPressDraggable(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(4.dp)
                ) {
                    Column {
                        Text(
                            text = today,
                            modifier = Modifier.padding(16.dp),
                            style = typography.headlineMedium.copy()
                        )
                        Row {
                            // 왼쪽 리스트(형성 중)
                            DropTarget<Habit>(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(start = 8.dp, end = 4.dp),
                                onDrop = { habit ->
                                    habitViewModel.updateHabitType(habit.id, currentUser.uid, "MAINTAIN")
                                }
                            ) { _, _ ->
                                List(
                                    modifier = Modifier.fillMaxSize(),
                                    title = "형성 중인 습관",
                                    uid = currentUser.uid,
                                    items = doList,
                                    habitViewModel = habitViewModel,
                                    onStartLoading = { isLoading = true },
                                    onFinish = { success ->
                                        coroutineScope.launch {
                                            if (success) {
                                                // 1.5초 로딩 후
                                                delay(1500)
                                                isLoading = false
                                                showMessage = true
                                                // 리스트 갱신
                                                habitViewModel.loadHabits(currentUser.uid)
                                                // 메시지 2초 후 사라짐
                                                delay(2000)
                                                showMessage = false
                                            } else {
                                                isLoading = false
                                                // 실패 시 별도 처리 원하면 여기에 추가
                                            }
                                        }
                                    }
                                )
                            }

                            // 오른쪽 리스트(유지 중)
                            DropTarget<Habit>(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(start = 4.dp, end = 8.dp),
                                onDrop = { habit ->
                                    habitViewModel.updateHabitType(habit.id, currentUser.uid, "FORMING")
                                }
                            ) { _, _ ->
                                List(
                                    modifier = Modifier.fillMaxSize(),
                                    title = "유지 중인 습관",
                                    uid = currentUser.uid,
                                    items = donotList,
                                    habitViewModel = habitViewModel,
                                    onStartLoading = { isLoading = true },
                                    onFinish = { success ->
                                        coroutineScope.launch {
                                            if (success) {
                                                delay(1500)
                                                isLoading = false
                                                showMessage = true
                                                habitViewModel.loadHabits(currentUser.uid)
                                                delay(2000)
                                                showMessage = false
                                            } else {
                                                isLoading = false
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 로딩 화면 표시
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x99000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun List(
    modifier: Modifier = Modifier,
    uid: String,
    title: String,
    items: List<Habit>,
    habitViewModel: HabitViewModel,
    onStartLoading: () -> Unit,
    onFinish: (Boolean) -> Unit
) {
    Column(modifier = modifier.padding(bottom = 48.dp)) {
        LazyColumn(modifier = modifier) {
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
            items(items) { item ->
                DisplayingList(
                    habit = item,
                    userId = uid,
                    habitViewModel = habitViewModel,
                    onStartLoading = onStartLoading,
                    onFinish = onFinish
                )
                Divider(modifier = Modifier.height(1.dp))
            }
        }
    }
}

@Composable
fun DisplayingList(
    habit: Habit,
    userId: String,
    habitViewModel: HabitViewModel,
    modifier: Modifier = Modifier,
    onStartLoading: () -> Unit,
    onFinish: (Boolean) -> Unit
) {
    val today = getCurrentDate()
    // 초기화: habit.successDates에 오늘 날짜가 있으면 체크 상태로 시작
    var isCheckedToday by rememberSaveable { mutableStateOf(habit.successDates.contains(today)) }
    var dailyStatus by remember { mutableStateOf(DailyStatus(date = today, isChecked = isCheckedToday)) }

    // 만약 habit이 갱신되어 successDates가 바뀌었다면, DB 실제 상태를 반영
    LaunchedEffect(habit) {
        val currentlyChecked = habit.successDates.contains(today)
        // 현재 UI 상태와 DB 상태가 다를 경우에만 업데이트
        if (currentlyChecked != isCheckedToday) {
            isCheckedToday = currentlyChecked
            dailyStatus = dailyStatus.copy(isChecked = currentlyChecked)
        }
    }

    DragTarget(modifier = Modifier, dataToDrop = habit) {
        ElevatedCard(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxSize(),
            colors = CardDefaults.cardColors()
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
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                text = habit.category,
                                style = typography.bodySmall
                            )
                        }

                        Text(
                            text = habit.name,
                            maxLines = if (false) 2 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "연속 성공 : ${habit.streak}일",
                        style = typography.bodySmall
                    )
                }

                Column {
                    Checkbox(
                        checked = dailyStatus.isChecked,
                        onCheckedChange = { isChecked ->
                            isCheckedToday = isChecked
                            dailyStatus = dailyStatus.copy(isChecked = isChecked)
                            onStartLoading()
                            // onComplete 콜백을 활용해 UI 반영
                            habitViewModel.updateDailyStatus(habit.id, userId, dailyStatus) { success ->
                                onFinish(success)
                            }
                        }
                    )

                    IconButton(onClick = {
                        habitViewModel.deleteHabit(habit.id, userId)
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Habit")
                    }
                }
            }
        }
    }
}

// ---------------------------- 드래그/드롭 관련 코드 -----------------------

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
    var itemSize by mutableStateOf(IntSize.Zero)
}

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

@Composable
fun LongPressDraggable(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(LocalDragTargetInfo provides state) {
        Box(modifier = modifier) {
            content()
            if (state.isDragging) {
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
    var isPopupVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val rect = coordinates.boundsInWindow()
                isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
            }
    ) {
        val data = if (isCurrentDropTarget && !dragInfo.isDragging) dragInfo.dataToDrop as T? else null
        content(isCurrentDropTarget, data)

        if (isPopupVisible) {
            AlertDialog(
                onDismissRequest = { isPopupVisible = false },
                title = { Text(text = "습관 이동 확인") },
                text = { Text("이 습관을 이동하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        if (data != null) onDrop(data)
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

        LaunchedEffect(isCurrentDropTarget, dragInfo.isDragging) {
            if (isCurrentDropTarget && !dragInfo.isDragging && dragInfo.dataToDrop != null) {
                isPopupVisible = true
            }
        }
        LaunchedEffect(dragInfo.isDragging) {
            if (!dragInfo.isDragging) {
                dragInfo.dragOffset = Offset.Zero
            }
        }
    }
}
