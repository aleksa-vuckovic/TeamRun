package com.example.runpal.activities.account

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.runpal.Capitalize
import com.example.runpal.DoubleInput
import com.example.runpal.DoubleInputWithUnit
import com.example.runpal.ImageSelector
import com.example.runpal.Units
import com.example.runpal.borderBottom
import com.example.runpal.models.User
import com.example.runpal.ui.theme.StandardButton
import com.example.runpal.ui.theme.StandardSpinner
import com.example.runpal.ui.theme.StandardTextField
import com.example.runpal.R
import kotlin.reflect.KProperty

@Composable
fun EditScreen(init: User,
               onUpdate: (String, String, Double, Uri?) -> Unit,
               errorMessage: String,
               preferredUnits: Units = Units.METRIC,
               modifier: Modifier = Modifier) {

    var name by rememberSaveable {
        mutableStateOf(init.name)
    }
    var last by rememberSaveable {
        mutableStateOf(init.last)
    }
    var units by rememberSaveable {
        mutableStateOf(preferredUnits)
    }
    var weight by rememberSaveable {
        mutableStateOf(Units.METRIC.weight.convert(init.weight, Units.IMPERIAL))
    }
    var profile by rememberSaveable {
        mutableStateOf<Uri?>(init.profileUri)
    }

    @Composable
    fun RowScope.Label(text: String) = Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .padding(end = 30.dp)
            .fillMaxWidth(0.3f),
        textAlign = TextAlign.End
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val style = MaterialTheme.typography.labelMedium

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Label(stringResource(id = R.string.first_name))
            StandardTextField(value = name, onChange = {name = it}, modifier = Modifier.width(200.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Label(stringResource(id = R.string.last_name))
            StandardTextField(value = last, onChange = {last = it}, modifier = Modifier.width(200.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Label(stringResource(id = R.string.weight))
            DoubleInputWithUnit(
                value = weight,
                onChange ={weight = it} ,
                unit = units.weight.primary,
                onChangeUnit = { units = units.next },
                enabled = true,
                modifier = Modifier.height(60.dp).width(200.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Label(stringResource(id = R.string.profile_photo))
            ImageSelector(input = profile, onSelect = {profile = it}, Modifier.size(200.dp))
        }

        StandardButton(onClick = { onUpdate(name, last, units.weight.convert(weight, Units.METRIC), profile)})
        {
            Text(stringResource(id = R.string.update))
        }
        Text(text = errorMessage, style = style.copy(color = Color.Red))
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AccountScreen(user: User,
                  onEdit: () -> Unit,
                  units: Units,
                  onSelectUnits: (Units) -> Unit,
                  modifier: Modifier) {

    Box(modifier = modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {}
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = rememberImagePainter(data = user.profileUri),
                        contentDescription = stringResource(id = R.string.profile_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(shape = CircleShape)
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    Text(text = "${user.name} ${user.last}", style = MaterialTheme.typography.titleMedium)
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(text = stringResource(id = R.string.weight) + ": %.2f%s".format(Units.METRIC.weight.convert(user.weight, units), units.weight.primary), style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.preferred_units) +": ", style = MaterialTheme.typography.bodyLarge)
                StandardSpinner(values = Units.values().toList(), selected = units, onSelect = {onSelectUnits(it)}, display = { it.name.Capitalize() })
            }

        }
        FloatingActionButton(onClick = onEdit,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)) {
                Icon(imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.edit))
        }
    }
}
