package com.joyproxy.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JoyProxyLogoMark(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.24f))
                .background(JoyProxyColors.BrandGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "J",
            color = JoyProxyColors.OnGradient,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.52f).sp,
        )
    }
}

@Composable
fun JoyProxyHeader(
    onTitleClick: () -> Unit,
    subtitle: String = "企业级网络代理方案",
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(JoyProxyColors.BrandGradient)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                JoyProxyLogoMark(
                    modifier = Modifier.clickable(onClick = onTitleClick),
                )
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text =
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = JoyProxyColors.OnGradient, fontWeight = FontWeight.Bold)) {
                                    append("Joy")
                                }
                                withStyle(SpanStyle(color = Color(0xFFE0E7FF), fontWeight = FontWeight.Bold)) {
                                    append("Proxy")
                                }
                            },
                        fontSize = 26.sp,
                        modifier = Modifier.clickable(onClick = onTitleClick),
                    )
                    Text(
                        text = subtitle,
                        color = JoyProxyColors.OnGradient.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
