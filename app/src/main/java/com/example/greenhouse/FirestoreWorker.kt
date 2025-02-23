import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val userId = " jyFQkNn2sLMmNRbGTX5RKs2t3Qz1 "

        db.collection("users").document(userId).collection("greenhouses")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: "Greenhouse"
                    val temp = document.getDouble("temperature") ?: 0.0
                    val humidity = document.getDouble("humidity") ?: 0.0
                    val acidity = document.getDouble("acidity") ?: 0.0

                    if (temp < 10 || temp > 35) {
                        sendNotification("⚠️ Temperature Alert in $name!", "Current: ${temp}°C")
                    }
                    if (humidity < 30 || humidity > 80) {
                        sendNotification("⚠️ Humidity Alert in $name!", "Current: ${humidity}%")
                    }
                    if (acidity < 5.5 || acidity > 7.5) {
                        sendNotification("⚠️ Acidity Alert in $name!", "Current: $acidity")
                    }
                }
            }
            .addOnFailureListener { e -> Log.e("FirestoreWorker", "Error checking Firestore", e) }

        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(title: String, message: String) {
        val channelId = "greenhouse_alerts"
        val notificationId = System.currentTimeMillis().toInt()

        val notification = android.app.Notification.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(notificationId, notification)
    }
}
