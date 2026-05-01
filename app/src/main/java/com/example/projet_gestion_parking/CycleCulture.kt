package com.example.projet_gestion_parking
import kotlinx.serialization.Serializable
@Serializable
data class CycleCulture(
    val id: Int? = null,             // L'ID est auto-généré par Supabase
    val variete: String,            // Ex: "Tomate", "Chou"
    val parcelle_num: String,       // Ex: "Parcelle A1"
    val date_semis: String,         // On stockera la date au format texte (ISO)
    val duree_croissance: Int,      // Nombre de jours (ex: 90)
    val image_url: String?  = null,  // L'adresse de l'image dans le Storage
    val etat: String = "Actif"      // Pour savoir si c'est en cours ou récolté
)

