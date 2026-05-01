package com.example.projet_gestion_parking.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet_gestion_parking.CycleCulture
import com.example.projet_gestion_parking.supabaseclient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CultureViewModel : ViewModel() {

    private val _cycles = MutableStateFlow<List<CycleCulture>>(emptyList())
    val cycles = _cycles.asStateFlow()

    fun ajouterCulture(variete: String, parcelle: String, jours: Int, photoBytes: ByteArray? = null) {
        viewModelScope.launch {
            try {
                var urlPhoto: String? = null
                if (photoBytes != null) {
                    val nomFichier = "photo_${System.currentTimeMillis()}.jpg"
                    val bucket = supabaseclient.client.storage.from("cultures")
                    bucket.upload(nomFichier, photoBytes)
                    urlPhoto = bucket.publicUrl(nomFichier)
                }

                val nouveau = CycleCulture(
                    variete = variete,
                    parcelle_num = parcelle,
                    date_semis = LocalDate.now().toString(),
                    duree_croissance = jours,
                    image_url = urlPhoto,
                    etat = "Actif"
                )
                
                supabaseclient.client.from("cycles_culture").insert(nouveau)
                recupererCultures()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun recupererCultures() {
        viewModelScope.launch {
            try {
                // Suppression du filtre pour tout afficher et valider la connexion
                val liste = supabaseclient.client.from("cycles_culture")
                    .select()
                    .decodeList<CycleCulture>()
                _cycles.value = liste
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun marquerCommeRecolte(id: Int) {
        viewModelScope.launch {
            try {
                supabaseclient.client.from("cycles_culture").update(
                    {
                        set("etat", "Recolte")
                    }
                ) {
                    filter { eq("id", id) }
                }
                recupererCultures()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun supprimerCulture(id: Int) {
        viewModelScope.launch {
            try {
                supabaseclient.client.from("cycles_culture").delete {
                    filter { eq("id", id) }
                }
                recupererCultures()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun calculerJoursRestants(dateSemis: String, duree: Int): Long {
        return try {
            val debut = LocalDate.parse(dateSemis)
            val finPrevue = debut.plusDays(duree.toLong())
            val aujourdhui = LocalDate.now()
            val reste = ChronoUnit.DAYS.between(aujourdhui, finPrevue)
            if (reste < 0) 0 else reste
        } catch (e: Exception) {
            0L
        }
    }
}
