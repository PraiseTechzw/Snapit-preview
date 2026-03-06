package com.snaptool.service

import android.service.quicksettings.TileService
import android.content.Intent
import com.snaptool.ui.MainActivity

class SnapQuickTile : TileService() {
    override fun onClick() {
        super.onClick()
        // Open the app to Screen Record directly
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse("snaptool://screen_record")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivityAndCollapse(intent)
    }
}
