package org.avium.systemuitools.compose

import android.content.Context
import android.graphics.Bitmap
import org.avium.systemuitools.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingPickerUI(
    images: State<List<Bitmap>>,
    selectedImages: State<List<Bitmap>>,
    isLoading: State<Boolean>,
    onImageClick: (Bitmap) -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onClose: () -> Unit,
    isVisible: Boolean = true
) {
    val context = LocalContext.current
    val view = LocalView.current
    val rootInsets = ViewCompat.getRootWindowInsets(view)
    val sysInsets = rootInsets?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.of(0,0,0,0)
    val density = LocalDensity.current
    val statusBarDp = with(density) { sysInsets.top.toDp() }
    val navBarDp = with(density) { sysInsets.bottom.toDp() }
    val scrim = Color.Black.copy(alpha = 0.36f)
    val interactionSource = remember { MutableInteractionSource() }
    val corner = 32.dp

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarDp)
//                    .background(scrim)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(scrim)
                    .clickable(
                        onClick = onClose,
                        indication = null,
                        interactionSource = interactionSource
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = navBarDp + 6.dp)
                    .align(Alignment.BottomCenter)
                    .clickable(onClick = { }, indication = null, interactionSource = interactionSource)
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(corner),
                        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = context.getString(R.string.select_images),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF374151)
                                )
                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                        containerColor = Color(0xFF374151).copy(alpha = 0.06f)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = context.getString(R.string.close),
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF374151)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF7F8F9),
                                tonalElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isLoading.value -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(48.dp),
                                                strokeWidth = 4.dp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                        images.value.isEmpty() -> {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = Color(0xFF9CA3AF)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = context.getString(R.string.no_images_found),
                                                    color = Color(0xFF6B7280),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                        else -> {
                                            LazyRow(
                                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxHeight(),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                items(images.value) { bitmap ->
                                                    ImageItem(
                                                        bitmap = bitmap,
                                                        isSelected = selectedImages.value.contains(bitmap),
                                                        onClick = { onImageClick(bitmap) },
                                                        context = context
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val haptic = LocalHapticFeedback.current
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onSaveClick()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    enabled = selectedImages.value.isNotEmpty(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF111827),
                                        disabledContainerColor = Color(0xFFF1F5F9),
                                        disabledContentColor = Color(0xFF9CA3AF)
                                    ),
                                ) {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = context.getString(R.string.save),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = context.getString(R.string.save_with_count, selectedImages.value.size),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Button(
                                    onClick = {
                                        onShareClick()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    enabled = selectedImages.value.isNotEmpty(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFeef2f7),
                                        contentColor = Color(0xFF0f172a),
                                        disabledContainerColor = Color(0xFFF1F5F9),
                                        disabledContentColor = Color(0xFF9CA3AF)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = context.getString(R.string.share),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = context.getString(R.string.share),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageItem(
    bitmap: Bitmap,
    isSelected: Boolean,
    onClick: () -> Unit,
    context: Context
) {
    val corner = 16.dp
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 120.dp)
            .clip(RoundedCornerShape(corner))
            .clickable {
                onClick()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(corner)),
            contentScale = ContentScale.Crop
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111827).copy(alpha = 0.18f))
            )

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111827)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = context.getString(R.string.selected),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
