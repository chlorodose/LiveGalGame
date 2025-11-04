package com.example.livegg1.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun KeywordDialog(
	primaryOptionLabel: String,
	secondaryOptionLabel: String,
	onPrimarySelected: () -> Unit,
	onSecondarySelected: () -> Unit,
	onDismiss: () -> Unit,
	onSelectBgm: (String) -> Unit
) {
	val dialogShape = RoundedCornerShape(16.dp)
	val dialogPink = Color(0xCCFFC0CB)
	val borderColor = Color(0xFFFF80AB)

	BasicAlertDialog(onDismissRequest = onDismiss) {
		Card(
			modifier = Modifier.padding(24.dp),
			shape = dialogShape,
			colors = CardDefaults.cardColors(containerColor = dialogPink),
			border = BorderStroke(2.dp, borderColor)
		) {
			Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
				Text(
					text = "该做出选择了",
					style = MaterialTheme.typography.titleLarge,
					color = Color.White,
					fontWeight = FontWeight.Bold
				)
				Text(
					modifier = Modifier.padding(top = 12.dp),
					text = "。。。",
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White
				)
				Column(
					modifier = Modifier
						.padding(top = 24.dp)
						.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					OutlinedButton(
						modifier = Modifier.fillMaxWidth(),
						onClick = {
							onPrimarySelected()
							onSelectBgm("casual.mp3")
						},
						shape = dialogShape,
						border = BorderStroke(2.dp, borderColor),
						colors = ButtonDefaults.outlinedButtonColors(
							containerColor = Color.White,
							contentColor = borderColor
						)
					) {
						Text(primaryOptionLabel)
					}
					OutlinedButton(
						modifier = Modifier.fillMaxWidth(),
						onClick = {
							onSecondarySelected()
							onSelectBgm("Ah.mp3")
						},
						shape = dialogShape,
						border = BorderStroke(2.dp, borderColor),
						colors = ButtonDefaults.outlinedButtonColors(
							containerColor = Color.White,
							contentColor = borderColor
						)
					) {
						Text(secondaryOptionLabel)
					}
				}
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
private fun KeywordDialogPreview() {
	KeywordDialog(
		primaryOptionLabel = "好啊好啊",
		secondaryOptionLabel = "不了",
		onPrimarySelected = {},
		onSecondarySelected = {},
		onDismiss = {},
		onSelectBgm = {}
	)
}
