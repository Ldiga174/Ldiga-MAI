/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.R
import com.google.ai.edge.gallery.data.BuiltInTaskId
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.ui.common.tos.AppTosDialog
import com.google.ai.edge.gallery.ui.common.tos.TosViewModel
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel

@Composable
fun HomeScreen(
  modelManagerViewModel: ModelManagerViewModel,
  tosViewModel: TosViewModel,
  navigateToTaskScreen: (Task) -> Unit,
  onModelsClicked: () -> Unit,
  onNotificationsClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by modelManagerViewModel.uiState.collectAsState()
  var showSettings by rememberSaveable { mutableStateOf(false) }
  var showTerms by rememberSaveable { mutableStateOf(!tosViewModel.getIsTosAccepted()) }
  val chat = state.tasks.firstOrNull { it.id == BuiltInTaskId.LLM_AGENT_CHAT }
  val hasLocalModel = modelManagerViewModel.getAllDownloadedModels().isNotEmpty()
  val foreground = Color(0xFFF2F7F5)
  val muted = Color(0xFFADC1BA)
  val accent = Color(0xFFA8F5CF)

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(Color(0xFF101716))
        .safeDrawingPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Text(
        stringResource(R.string.app_name),
        color = foreground,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = onNotificationsClicked, enabled = !showTerms) {
        Icon(Icons.Rounded.Notifications, stringResource(R.string.ldiga_notifications), tint = muted)
      }
      IconButton(onClick = { showSettings = true }, enabled = !showTerms) {
        Icon(Icons.Rounded.Settings, stringResource(R.string.ldiga_settings), tint = muted)
      }
    }

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            Brush.linearGradient(listOf(Color(0xFF203A30), Color(0xFF192623))),
            RoundedCornerShape(28.dp),
          )
          .padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Box(Modifier.size(width = 38.dp, height = 4.dp).background(accent, RoundedCornerShape(4.dp)))
      Text(
        stringResource(R.string.ldiga_home_title),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = foreground,
      )
      Text(stringResource(R.string.ldiga_home_description), color = muted)
      Button(
        onClick = { chat?.let(navigateToTaskScreen) },
        enabled = !showTerms && chat != null,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF10231A)),
      ) {
        Text(stringResource(R.string.ldiga_open_chat))
      }
    }

    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2523)),
      shape = RoundedCornerShape(22.dp),
    ) {
      Column(
        Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          stringResource(R.string.ldiga_local_models),
          style = MaterialTheme.typography.titleLarge,
          color = foreground,
        )
        Text(
          stringResource(
            when {
              hasLocalModel -> R.string.ldiga_model_available
              state.loadingModelAllowlist -> R.string.ldiga_loading_models
              else -> R.string.ldiga_import_hint
            }
          ),
          color = muted,
        )
        OutlinedButton(onClick = onModelsClicked, enabled = !showTerms) {
          Text(stringResource(R.string.ldiga_manage_models), color = accent)
        }
        if (state.loadingModelAllowlistError.isNotEmpty()) {
          Text(stringResource(R.string.ldiga_catalog_unavailable), color = muted)
          TextButton(onClick = modelManagerViewModel::loadModelAllowlist, enabled = !showTerms) {
            Text(stringResource(R.string.retry), color = accent)
          }
        }
      }
    }
  }

  if (showTerms) {
    AppTosDialog(onTosAccepted = {
      tosViewModel.acceptTos()
      showTerms = false
    })
  }
  if (showSettings) {
    SettingsDialog(
      curThemeOverride = modelManagerViewModel.readThemeOverride(),
      curFirebaseAnalytics = modelManagerViewModel.readFirebaseAnalytics(),
      modelManagerViewModel = modelManagerViewModel,
      onDismissed = { showSettings = false },
    )
  }
}
