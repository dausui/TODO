package com.daustodo.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> LazyLoadingList(
    items: List<T>,
    isLoading: Boolean,
    hasMoreItems: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T) -> Unit,
    loadingContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    },
    emptyContent: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    // Auto-load more items when reaching the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItem = visibleItems.last()
                    val totalItems = listState.layoutInfo.totalItemsCount
                    
                    if (lastVisibleItem.index >= totalItems - 3 && hasMoreItems && !isLoading) {
                        scope.launch {
                            delay(100) // Small delay to prevent rapid loading
                            onLoadMore()
                        }
                    }
                }
            }
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = items,
            key = { it.hashCode() }
        ) { item ->
            itemContent(item)
        }
        
        // Loading indicator at the bottom
        if (isLoading && hasMoreItems) {
            item {
                loadingContent()
            }
        }
        
        // Empty state
        if (items.isEmpty() && !isLoading) {
            item {
                emptyContent()
            }
        }
    }
}

@Composable
fun <T> OptimizedLazyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T) -> Unit,
    emptyContent: @Composable () -> Unit = {}
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = items,
            key = { it.hashCode() }
        ) { item ->
            itemContent(item)
        }
        
        if (items.isEmpty()) {
            item {
                emptyContent()
            }
        }
    }
}