package com.example.projet_gestion_parking

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.projet_gestion_parking.viewModel.CultureViewModel
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2E7D32),
                    secondary = Color(0xFF8D6E63),
                    background = Color(0xFFF1F8E9)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EcranAgriSmart()
                }
            }
        }
    }
}

@Composable
fun EcranAgriSmart(monViewModel: CultureViewModel = viewModel()) {
    val lesCultures by monViewModel.cycles.collectAsState()
    val context = LocalContext.current
    
    var variete by remember { mutableStateOf("") }
    var parcelle by remember { mutableStateOf("") }
    var jours by remember { mutableStateOf("") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var photoPrete by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBytes = outputStream.toByteArray()
            photoPrete = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBytes = outputStream.toByteArray()
            photoPrete = true
        }
    }

    LaunchedEffect(Unit) {
        monViewModel.recupererCultures()
    }

    // Structure principale : Une Column fixe en haut pour le formulaire
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AgriSmart - Tableau de Bord", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)
        ) {
            Text(
                "Cultures actives : ${lesCultures.size}",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Formulaire compact
        OutlinedTextField(value = variete, onValueChange = { variete = it }, label = { Text("Variété") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = parcelle, onValueChange = { parcelle = it }, label = { Text("Parcelle") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = jours, onValueChange = { jours = it }, label = { Text("Jours") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) {
                Text("Photo")
            }
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                Text("Galerie")
            }
        }
        
        if (photoPrete) {
            Text("Photo prête ✅", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                monViewModel.ajouterCulture(variete, parcelle, jours.toIntOrNull() ?: 0, imageBytes)
                variete = ""; parcelle = ""; jours = ""; imageBytes = null; photoPrete = false
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Lancer le cycle")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // ZONE SCROLLABLE : La liste des cartes défile ici
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lesCultures) { culture ->
                CarteCulture(cycle = culture, viewModel = monViewModel)
            }
        }
    }
}

@Composable
fun CarteCulture(cycle: CycleCulture, viewModel: CultureViewModel) {
    val joursRestants = viewModel.calculerJoursRestants(cycle.date_semis, cycle.duree_croissance)
    val progression = remember(cycle) {
        val total = cycle.duree_croissance.toFloat()
        if (total <= 0) 0f else ((total - joursRestants) / total).coerceIn(0f, 1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            if (cycle.image_url != null) {
                AsyncImage(
                    model = cycle.image_url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cycle.variete, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32))
                    Text("P${cycle.parcelle_num}", style = MaterialTheme.typography.bodyMedium)
                }

                LinearProgressIndicator(
                    progress = { progression },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = Color(0xFF2E7D32)
                )

                Text(if (joursRestants > 0) "Récolte dans $joursRestants jours" else "Prêt !")

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.marquerCommeRecolte(cycle.id!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                ) {
                    Text("Marquer comme Récolté")
                }
            }
        }
    }
}
