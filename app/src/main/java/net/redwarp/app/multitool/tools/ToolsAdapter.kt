package net.redwarp.app.multitool.tools

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.redwarp.app.multitool.R

class ToolsAdapter(val tools: List<Tool>) : RecyclerView.Adapter<ToolsAdapter.ViewHolder>() {
    var listener: ((Tool) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tool, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tools[position])
    }

    override fun getItemCount(): Int = tools.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.name)

        init {
            itemView.setOnClickListener {
                if (layoutPosition != RecyclerView.NO_POSITION) {
                    listener?.invoke(tools[layoutPosition])
                }
            }
        }

        fun bind(tool: Tool) {
            nameView.text = nameView.context.getString(tool.name)
        }
    }
}
