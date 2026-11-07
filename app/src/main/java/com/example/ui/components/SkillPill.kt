package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.AtsRed
import com.example.ui.theme.SecondaryCyan

enum class SkillType {
    MATCHED, MISSING, ADDITIONAL
}

@Composable
fun SkillPill(
    skillName: String,
    type: SkillType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (type) {
        SkillType.MATCHED -> Triple(
            AtsGreen.copy(alpha = 0.15f),
            AtsGreen,
            Icons.Default.Check
        )
        SkillType.MISSING -> Triple(
            AtsRed.copy(alpha = 0.15f),
            AtsRed,
            Icons.Default.Close
        )
        SkillType.ADDITIONAL -> Triple(
            SecondaryCyan.copy(alpha = 0.15f),
            SecondaryCyan,
            Icons.Default.Add
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = skillName,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
