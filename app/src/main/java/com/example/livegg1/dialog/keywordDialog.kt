package com.example.livegg1.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun KeywordDialog(
	onAccept: () -> Unit,
	onReject: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("检测到关键词") },
		text = { Text("语音中出现了“吗”，请选择下一步操作。") },
		confirmButton = {
			TextButton(onClick = onAccept) {
				Text("接受")
			}
		},
		dismissButton = {
			TextButton(onClick = onReject) {
				Text("拒绝")
			}
		}
	)
}
