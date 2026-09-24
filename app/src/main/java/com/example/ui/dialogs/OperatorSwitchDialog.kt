package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.BrandSlateDark
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OperatorSwitchDialog(
    currentOperator: String,
    onDismiss: () -> Unit,
    onOperatorChanged: (operatorName: String, role: UserRole) -> Unit
) {
    var newOperatorName by remember { mutableStateOf(currentOperator) }
    var selectedRole by remember { mutableStateOf(UserRole.STORE_OPERATOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSlateCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = BrandSkyLight)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Changement d'Opérateur", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Passage de relais sans réappairer le terminal", fontSize = 11.sp, color = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newOperatorName,
                    onValueChange = { newOperatorName = it },
                    label = { Text("Identifiant / Nom de l'opérateur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSkyLight,
                        unfocusedBorderColor = BrandSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Text("Rôle & Droits d'Accès :", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val roles = listOf(UserRole.STORE_OPERATOR, UserRole.STORE_MANAGER, UserRole.AUDITOR)
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandSkyBlue else BrandSlateDark)
                                .clickable { selectedRole = role }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (role) {
                                    UserRole.STORE_OPERATOR -> "Opérateur"
                                    UserRole.STORE_MANAGER -> "Manager"
                                    UserRole.AUDITOR -> "Auditeur"
                                    UserRole.ADMIN -> "Admin"
                                },
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newOperatorName.isNotBlank()) {
                        onOperatorChanged(newOperatorName.trim(), selectedRole)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_confirm_operator_switch")
            ) {
                Text("Valider la Prise de Poste", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Annuler", color = TextSecondary)
            }
        }
    )
}
