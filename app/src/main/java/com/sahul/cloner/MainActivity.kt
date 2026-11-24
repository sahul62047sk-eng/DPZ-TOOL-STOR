package com.sahul.cloner
import android.app.admin.DevicePolicyManager
import android.content.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private val clonedInstances = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, CloneAdminReceiver::class.java)
        val btnStartBg = findViewById<Button>(R.id.btnStartBg)
        val btnSetupProfile = findViewById<Button>(R.id.btnSetupProfile)
        val btnCloneApp = findViewById<Button>(R.id.btnCloneApp)
        val etPackageName = findViewById<EditText>(R.id.etPackageName)
        val lvClones = findViewById<ListView>(R.id.lvClones)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clonedInstances)
        lvClones.adapter = adapter
        btnStartBg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply { data = Uri.parse("package:$packageName") })
                }
            }
            val sIntent = Intent(this, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(sIntent) else startService(sIntent)
            Toast.makeText(this, "Background Engine ON!", Toast.LENGTH_SHORT).show()
        }
        btnSetupProfile.setOnClickListener {
            if (!dpm.isProfileOwnerApp(packageName)) {
                val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
                    putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent)
                }
                if (intent.resolveActivity(packageManager) != null) startActivityForResult(intent, 1001)
                else Toast.makeText(this, "Virtual Mode active hai.", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "Isolated Space pehle se active hai!", Toast.LENGTH_SHORT).show()
        }
        btnCloneApp.setOnClickListener {
            val targetPkg = etPackageName.text.toString().trim()
            if (targetPkg.isNotEmpty()) {
                val count = clonedInstances.count { it.contains(targetPkg) } + 1
                clonedInstances.add("$targetPkg [Clone #$count]")
                adapter.notifyDataSetChanged()
                etPackageName.text.clear()
            }
        }
        lvClones.setOnItemClickListener { _, _, position, _ ->
            val pkg = clonedInstances[position].substringBefore(" [Clone")
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivity(launchIntent)
            } else Toast.makeText(this, "App install nahi hai: $pkg", Toast.LENGTH_SHORT).show()
        }
    }
}
