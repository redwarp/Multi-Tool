package net.redwarp.app.multitool.tools

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_tools.*
import net.redwarp.app.multitool.R
import net.redwarp.app.multitool.compass.CompassActivity

class ToolsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tools)

        list.layoutManager = LinearLayoutManager(this)
        val toolsAdapter = ToolsAdapter(listOf(
                Tool("compass", R.string.compass)
        ))
        toolsAdapter.listener = { tool ->
            when (tool.id) {
                "compass" -> {
                    startActivity(Intent(this, CompassActivity::class.java))
                }
            }
        }
        list.adapter = toolsAdapter
    }
}