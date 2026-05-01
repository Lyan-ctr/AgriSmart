package com.example.projet_gestion_parking
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object supabaseclient {
    val client = createSupabaseClient(
        supabaseUrl = "https://iwsvqjyhhpvastwcveab.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml3c3ZxanloaHB2YXN0d2N2ZWFiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcyNDg3NDksImV4cCI6MjA5MjgyNDc0OX0.JXuGNE7EONV78f1V-wV9Es348xhwwPMuAklQwDi5rug"
    ) {
        install(Postgrest)
        install(Storage)
    }
}
