import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.obviouscontrol.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SensorDetailsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var sensorId: Int = 0
    private var sensorName: String = ""
    private var sensorDescription: String = ""
    private lateinit var lineChart: LineChart
    private lateinit var minValueEditText: EditText
    private lateinit var maxValueEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sensorId = it.getInt("sensorId", 0)
            sensorName = it.getString("sensorName", "")
            sensorDescription = it.getString("sensorDescription", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sensor_details, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        minValueEditText = view.findViewById(R.id.editTextMinValue)
        maxValueEditText = view.findViewById(R.id.editTextMaxValue)
        startDateEditText = view.findViewById(R.id.editTextDateStart)
        endDateEditText = view.findViewById(R.id.editTextDateEnd)
        submitButton = view.findViewById(R.id.btn_submit)

        val sensorNameTextView = view.findViewById<TextView>(R.id.sensorName)
        val sensorDescriptionTextView = view.findViewById<TextView>(R.id.sensorDescription)
        sensorNameTextView.text = sensorName
        sensorDescriptionTextView.text = sensorDescription

        // Configure the line chart properties
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        // Populate the line chart with data
        //populateLineChart()

        val minValue = minValueEditText.text.toString()
        val maxValue = maxValueEditText.text.toString()
        val startDate = startDateEditText.text.toString()
        val endDate = endDateEditText.text.toString()
        fetchData(sensorId, minValue, maxValue, startDate, endDate)

        // Get references to your EditText fields
        val editTextDateStart = view.findViewById<EditText>(R.id.editTextDateStart)
        val editTextDateEnd = view.findViewById<EditText>(R.id.editTextDateEnd)

// Set an OnClickListener to the EditText fields
        editTextDateStart.setOnClickListener {
            showDateTimePicker(editTextDateStart)
        }

        editTextDateEnd.setOnClickListener {
            showDateTimePicker(editTextDateEnd)
        }

        submitButton.setOnClickListener {
            val minValue = minValueEditText.text.toString()
            val maxValue = maxValueEditText.text.toString()
            val startDate = startDateEditText.text.toString()
            val endDate = endDateEditText.text.toString()
            fetchData(sensorId, minValue, maxValue, startDate, endDate)
        }

        return view
    }

    private fun retrieveTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    private fun retrieveHelmetFromSharedPreferences(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("helmet_id", null)
    }

    private fun showDateTimePicker(editText: EditText) {
        val currentDate = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                currentDate.set(Calendar.YEAR, year)
                currentDate.set(Calendar.MONTH, month)
                currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePicker = TimePickerDialog(
                    requireContext(),
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        currentDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        currentDate.set(Calendar.MINUTE, minute)

                        // Format the selected date and time
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val formattedDateTime = dateFormat.format(currentDate.time)

                        // Set the formatted date and time to the EditText field
                        editText.setText(formattedDateTime)
                    },
                    currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE),
                    false
                )

                timePicker.show()
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }


    private fun populateLineChart() {
        val entries = mutableListOf<Entry>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Add your data entries to the 'entries' list
        entries.add(Entry(dateFormat.parse("2023-05-01 10:00:00").time.toFloat(), 10f))
        entries.add(Entry(dateFormat.parse("2023-05-02 11:00:00").time.toFloat(), 20f))
        entries.add(Entry(dateFormat.parse("2023-05-03 12:00:00").time.toFloat(), 15f))
        entries.add(Entry(dateFormat.parse("2023-05-04 13:00:00").time.toFloat(), 25f))
        entries.add(Entry(dateFormat.parse("2023-05-05 14:00:00").time.toFloat(), 18f))
        entries.add(Entry(dateFormat.parse("2023-05-06 15:00:00").time.toFloat(), 30f))

        val dataSet = LineDataSet(entries, "Sensor Data")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)

        val lineData = LineData(dataSets)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun fetchData(
        sensorId: Int,
        minValue: String,
        maxValue: String,
        startDate: String,
        endDate: String
    ) {
        val token = retrieveTokenFromSharedPreferences()
        val helmetId = retrieveHelmetFromSharedPreferences()

        val queue = Volley.newRequestQueue(context)
        val url = "http://192.168.0.151:8080/api/readings/statistics" +
                "?sensor_id=$sensorId" +
                "&helmet_id=${helmetId ?: ""}" +
                "&min_value=$minValue" +
                "&max_value=$maxValue" +
                "&start_date=$startDate" +
                "&end_date=$endDate"

        val request = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                // Handle the response here
                Log.d("ServerResponse", response.toString())
                // Assuming 'response' contains the JSON response you provided

                try {
                    val responseData = response.getJSONArray("data")

                    val entries = mutableListOf<Entry>()

                    for (i in 0 until responseData.length()) {
                        val dataObj = responseData.getJSONObject(i)
                        val sensorValue = dataObj.getDouble("sensor_value").toFloat()
                        val createdAt = dataObj.getString("created_at")

                        // Parse the 'createdAt' string to a Date object using SimpleDateFormat
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                        val date = dateFormat.parse(createdAt)

                        // Create the Entry object and add it to the list
                        entries.add(Entry(date.time.toFloat(), sensorValue))
                    }

                    // Create the LineDataSet and configure its properties
                    val dataSet = LineDataSet(entries, "Sensor Data")
                    dataSet.color = Color.BLUE
                    dataSet.valueTextColor = Color.BLACK

                    val dataSets: ArrayList<ILineDataSet> = ArrayList()
                    dataSets.add(dataSet)

                    val lineData = LineData(dataSets)

                    // Set the LineData to your LineChart
                    lineChart.data = lineData
                    lineChart.invalidate()

                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

            },
            Response.ErrorListener { error ->
                // Handle error cases here
                Log.e("ServerResponse", error.toString())
                error.printStackTrace()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        queue.add(request)
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SensorDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
