package com.snaptool.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.snaptool.R
import com.snaptool.ui.MainActivity

class SnapWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Intent for Screenshot button
        val screenshotIntent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("snaptool://screenshot")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val screenshotPendingIntent = PendingIntent.getActivity(
            context, 0, screenshotIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_camera, screenshotPendingIntent)

        // Intent for Record button
        val recordIntent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("snaptool://screen_record")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val recordPendingIntent = PendingIntent.getActivity(
            context, 1, recordIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_record, recordPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
