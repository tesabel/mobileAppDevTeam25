// HabitManagementPage.kt

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.doordonot.ui.components.BottomNavigationBar
import com.example.doordonot.ui.components.TopBar
import com.example.doordonot.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun HabitManagementPage(navController: NavController, viewModel: HabitViewModel = viewModel()) {
    Scaffold(
        topBar = { TopBar(title = "습관 관리") },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("make_habit") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "add")
            }
        }
    ) { padding ->
        val habits by viewModel.habits.collectAsState()

        //형성중인 습관
        val doList: List<Habit> = habits.filter { it.isMaintained}
        //유지중인 습관
        val donotList: List<Habit> = habits.filter { !it.isMaintained}
        val today = LocalDate.now()
        LongPressDraggable(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(4.dp)
            ) {
                Column {
                    //날짜
                    Text(
                        text = "${today}",
                        modifier = Modifier.padding(16.dp),
                        style = typography.headlineMedium.copy()
                    )
                    //습관 리스트
                    Row {
                        // Do list
                        DropTarget<Habit>(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 8.dp, end = 4.dp),
                            onDrop = { habit ->
                                // isMaintained 상태 반전
                                viewModel.updateHabit(habit.copy(isMaintained = false))
                            }
                        ) { isInBound, _ ->
                            List(
                                modifier = Modifier.fillMaxSize(),
                                title = "형성 중인 습관",
                                items = doList,
                            )
                        }

                        // Donot list
                        DropTarget<Habit>(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 4.dp, end = 8.dp),
                            onDrop = { habit ->
                                // isMaintained 상태 반전
                                viewModel.updateHabit(habit.copy(isMaintained = true))
                            }
                        ) { isInBound, _ ->
                            List(
                                modifier = Modifier.fillMaxSize(),
                                title = "유지 중인 습관",
                                items = donotList,
                            )
                        }
                    }
                }
            }
        }
    }
}


//해더+리스트 컬럼
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun List(
    modifier: Modifier = Modifier,
    title: String,
    items: List<Habit>,
) {

    Column(
        modifier = modifier.padding(bottom = 48.dp)
    ) {
        LazyColumn(modifier = modifier) {
            //리스트 헤더
            stickyHeader(
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = title,
                    style = typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            if (title == "형성 중인 습관") Color(248, 84, 83, 255)
                            else Color(13,146,244)
                        )
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                )
            }
            //리스트 내용
            items(items) { h ->
                DisplayingList(habit = h)
                Divider(modifier = Modifier.height(1.dp))
            }
        }
    }
}

//리스트 표시
@Composable
fun DisplayingList(
    habit: Habit,
    modifier: Modifier = Modifier,
    viewModel: HabitViewModel = viewModel()
) {
    // 체크박스 체크 여부
    val today = LocalDate.now()
    var isCheckedToday = viewModel.isHabitCheckedOnDate(habit, today)
    //이름 클릭 시 전체보기
    var isClicked by rememberSaveable { mutableStateOf(false) }

    DragTarget(modifier = Modifier, dataToDrop = habit) {
        ElevatedCard(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(vertical = 8.dp).fillMaxSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = modifier.padding(8.dp).weight(1f) // 컨텐츠 공간 확보
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 카테고리
                        OutlinedCard(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            border = BorderStroke(
                                width = 2.dp,
                                color = when (habit.categories.joinToString(", ")) {
                                    "금지" -> Color(248, 84, 83)
                                    "운동" -> Color(0, 150, 136, 255)
                                    "공부" -> Color(13, 146, 244)
                                    else -> Color(255, 193, 7, 255)
                                }
                            ),
                            colors = CardDefaults.cardColors(
                                when (habit.categories.joinToString(", ")) {
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
                                text = habit.categories.joinToString(", "),
                                style = typography.bodySmall
                            )
                        }
                        // 습관명
                        Text(
                            text = habit.name,
                            maxLines = if (isClicked) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { isClicked = !isClicked }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 총 성공일수
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "총 성공 : ${viewModel.getTotalDays(habit)}일",
                        style = typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 연속 성공일수
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "연속 성공 : ${viewModel.getConsecutiveDays(habit)}일",
                        style = typography.bodySmall
                    )
                    Text(
                        text = "${habit.isMaintained}"
                    )
                }
                // 체크박스 - 오른쪽 끝에 고정
                Checkbox(
                    checked = isCheckedToday,
                    onCheckedChange = {viewModel.setHabitCheck(habit, today, it)}
                )
            }
        }
    }
}


//------------드래그/드롭 함수 구현-----------------------
//드래그 타겟 상태 정보
internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
    var itemSize by mutableStateOf(IntSize.Zero)
}

//드래그 가능한 뷰의 상태만 저장
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
                // 드래그 항목의 시작 위치와 크기를 저장
                initialPosition = layoutCoordinates.localToRoot(Offset.Zero)
                itemSize = layoutCoordinates.size
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        currentState.dataToDrop = dataToDrop
                        currentState.isDragging = true
                        currentState.dragPosition = initialPosition // 시작 위치 설정
                        currentState.dragOffset = Offset.Zero // 초기 드래그 오프셋
                        currentState.draggableComposable = content
                        currentState.itemSize = itemSize // 드래그 항목 크기 저장
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

//드래그 개체 복사하여 드래그
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


//-----------------------------드롭--------------------
//드롭 데이터 수신
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

        // 드롭이 가능하고 드래그가 끝나면 팝업을 띄움
        LaunchedEffect(isCurrentDropTarget, dragInfo.isDragging) {
            if (isCurrentDropTarget && !dragInfo.isDragging && dragInfo.dataToDrop != null) {
                isPopupVisible = true
            }
        }
    }
}
