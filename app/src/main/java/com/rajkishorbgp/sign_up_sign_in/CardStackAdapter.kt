package com.rajkishorbgp.sign_up_sign_in

import Evento
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardStackAdapter(private val eventos: List<Evento>) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.imageView)
        val title = view.findViewById<TextView>(R.id.titleTextView)
        val organizer = view.findViewById<TextView>(R.id.organizerTextView)
        val date = view.findViewById<TextView>(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = eventos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = eventos[position]
        holder.title.text = evento.titulo
        holder.organizer.text = "Organiza: Anónimo"
        holder.date.text = "${evento.fecha} - ${evento.hora}"
        holder.image.setImageResource(R.drawable.ic_people)
    }
}
