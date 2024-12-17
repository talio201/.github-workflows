import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListTextView: TextView
    private lateinit var scanButton: Button

    private val requestBluetoothPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startScan()
        } else {
            Toast.makeText(this, "Permissão de Bluetooth negada", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startScan()
        } else {
            Toast.makeText(this, "Permissão de Localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        deviceListTextView = findViewById(R.id.deviceList)
        scanButton = findViewById(R.id.buttonScan)

        // Verifica se o Bluetooth está disponível
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não é suportado neste dispositivo", Toast.LENGTH_LONG).show()
            return
        }

        // Verifica se o Bluetooth está ativado
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        // Solicitar permissões de localização e Bluetooth se necessário
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                scanButton.setOnClickListener { startScan() }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH)
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        } else {
            scanButton.setOnClickListener { startScan() }
        }
    }

    private fun startScan() {
        // Verifica se o Bluetooth está ativado
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Ative o Bluetooth primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpa o texto antes de iniciar a busca
        deviceListTextView.text = "Buscando dispositivos..."

        // Inicia a descoberta de dispositivos Bluetooth
        bluetoothAdapter.startDiscovery()

        // Cria o BroadcastReceiver para receber os dispositivos encontrados
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                val deviceName = device.name
                val deviceAddress = device.address // Endereço MAC do dispositivo
                deviceListTextView.append("\n$deviceName - $deviceAddress")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
