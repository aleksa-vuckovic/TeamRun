package com.example.runpal.activities.login

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runpal.DoubleInput
import com.example.runpal.DoubleInputWithUnit
import com.example.runpal.ImageSelector
import com.example.runpal.Units
import com.example.runpal.borderBottom
import com.example.runpal.ui.theme.StandardButton
import com.example.runpal.ui.theme.StandardTextField
import com.example.runpal.R

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, errorMessage: String, modifier: Modifier = Modifier) {

    var email by rememberSaveable {
        mutableStateOf("")
    }
    var password by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val style = MaterialTheme.typography.labelMedium

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(id = R.string.email), style = style, modifier = Modifier.weight(0.3f))
            StandardTextField(value = email, onChange = {email = it}, modifier = Modifier.weight(0.7f))
        }
        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(id = R.string.password), style = style, modifier = Modifier.weight(0.3f))
            StandardTextField(value = password,
                onChange = {password = it},
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(0.7f))
        }
        Spacer(modifier = Modifier.height(30.dp))

        StandardButton(onClick = { onLogin(email, password)}) {
            Text(stringResource(id = R.string.login))
        }
        Spacer(modifier = Modifier.height(30.dp))

        Text(text = errorMessage, style = style.copy(color = Color.Red))
    }
}


@Composable
fun RegisterScreen(onRegister: (String, String, String, String, Double, Uri?) -> Unit, errorMessage: String, modifier: Modifier = Modifier) {

    var email by rememberSaveable {
        mutableStateOf("")
    }
    var password by rememberSaveable {
        mutableStateOf("")
    }
    var name by rememberSaveable {
        mutableStateOf("")
    }
    var last by rememberSaveable {
        mutableStateOf("")
    }
    var weight by rememberSaveable {
        mutableStateOf(80.0)
    }
    var units by rememberSaveable {
        mutableStateOf(Units.METRIC)
    }
    var profile by rememberSaveable {
        mutableStateOf<Uri?>(null)
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
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Label(stringResource(id = R.string.email))
            StandardTextField(value = email, onChange = {email = it}, modifier = Modifier.width(200.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Label(stringResource(id = R.string.password))
            StandardTextField(value = password,
                onChange = {password = it},
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.width(200.dp))
        }

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

        StandardButton(onClick = { onRegister(email, password, name, last, units.weight.convert(weight, Units.METRIC), profile)})
         {
            Text(stringResource(id = R.string.register))
        }
        Text(text = errorMessage, style = MaterialTheme.typography.labelMedium.copy(color = Color.Red))
    }
}
