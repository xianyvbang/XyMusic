//package cn.xybbz.ui.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.FlowRow
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Preview
//@Composable
//private fun BilibiliSearchScreenT() {
//    BilibiliSearchScreen(onBack = {}) {}
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BilibiliSearchScreen(
//    onBack: () -> Unit,
//    onSearchSubmit: (String) -> Unit
//) {
//    var query by remember { mutableStateOf("") }
//    val history = remember { mutableStateListOf("进击的巨人", "原神", "鬼灭之刃", "AI绘画") }
//    val hotSearch = listOf("2025新番", "LPL", "王者荣耀", "猛男舞蹈", "鬼畜区")
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        // 顶部搜索栏
//        TopAppBar(
//            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
//            title = {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color(0xFFF6F6F6), RoundedCornerShape(24.dp))
//                        .padding(horizontal = 12.dp, vertical = 4.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
//                    Spacer(Modifier.width(4.dp))
//                    TextField(
//                        value = query,
//                        onValueChange = { query = it },
//                        placeholder = { Text("搜索番剧 / UP主 / 视频") },
//                        singleLine = true,
//                        colors = TextFieldDefaults.colors(
//                            unfocusedContainerColor = Color.Transparent,
//                            focusedContainerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
//                            focusedLabelColor = Color.LightGray,
//                            unfocusedLabelColor = Color.LightGray,
//                            cursorColor = Color.White,
//                            disabledContainerColor = Color.Transparent
//                        ),
//                        modifier = Modifier.weight(1f)
//                    )
//                    if (query.isNotBlank()) {
//                        IconButton(onClick = { query = "" }) {
//                            Icon(
//                                Icons.Default.Clear,
//                                contentDescription = "清除",
//                                tint = Color.Gray
//                            )
//                        }
//                    }
//                }
//            },
//            navigationIcon = {
//                IconButton(onClick = onBack) {
//                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
//                }
//            }
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        if (query.isBlank()) {
//            // 搜索历史
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text("搜索历史", fontWeight = FontWeight.Bold)
//                Text(
//                    "清除",
//                    color = Color.Gray,
//                    modifier = Modifier.clickable { history.clear() }
//                )
//            }
//
//            FlowRow(
//                modifier = Modifier.padding(horizontal = 16.dp),
//            ) {
//                history.forEach { item ->
//                    Surface(
//                        shape = RoundedCornerShape(16.dp),
//                        color = Color(0xFFEDEDED),
//                        modifier = Modifier.clickable {
//                            query = item
//                            onSearchSubmit(item)
//                        }
//                    ) {
//                        Text(
//                            text = item,
//                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // 热门搜索
//            Text(
//                "热门搜索",
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//            )
//
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(2),
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp),
//                contentPadding = PaddingValues(bottom = 32.dp)
//            ) {
//                items(hotSearch) { item ->
//                    Text(
//                        text = "🔥 $item",
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .clickable {
//                                query = item
//                                onSearchSubmit(item)
//                            }
//                    )
//                }
//            }
//
//        } else {
//            // 实时展示搜索建议或跳转
//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                items(5) {
//                    ListItem(
//                        headlineContent = { Text("$query 相关结果 $it") },
//                        leadingContent = {
//                            Icon(Icons.Default.History, contentDescription = null)
//                        },
//                        modifier = Modifier.clickable {
//                            onSearchSubmit("$query 相关结果 $it")
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
