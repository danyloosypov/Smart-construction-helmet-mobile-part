package com.example.obviouscontrol

import SensorDetailsFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.obviouscontrol.data.Sensor
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SensorsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SensorsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sensors, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val workerId = sharedPreferences.getString("worker_id", "")
        val tvHelmetStatus = view.findViewById<TextView>(R.id.tvHelmetStatus)
        // Make the API call using Volley
        val url = "http://192.168.0.151:8080/api/helmets/worker/$workerId"
        val queue = Volley.newRequestQueue(requireContext())

        val request = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener { response ->
                val correctedResponse = checkAndCorrectResponse(response.toString())
                val data = correctedResponse.optJSONObject("data")
                val helmetId = data.optString("id")
                /*val data = response.optJSONObject("data")
                val helmetId = data.optString("id")*/

                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("helmet_id", helmetId)
                    .apply()

                val helmetName = data.optString("name") ?: getString(R.string.sensorsnamenotdefined)
                val helmetDescription = data.optString("description") ?: getString(R.string.sensorsdescnotdefined)
                val formattedDescription = if (helmetDescription === null || helmetDescription.isEmpty() || helmetDescription === "null") {
                    getString(R.string.sensorsdescnotdefined)
                } else {
                    helmetDescription
                }
                val formattedName = if (helmetName === null || helmetName.isEmpty() || helmetName === "null") {
                    getString(R.string.sensorsnamenotdefined)
                } else {
                    helmetName
                }
                // Set the values to TextViews
                val tvHelmetName = view?.findViewById<TextView>(R.id.helmetName)
                var tvHelmetDescription = view?.findViewById<TextView>(R.id.helmetDescription)
                tvHelmetStatus?.visibility = View.GONE
                tvHelmetName?.text = formattedName
                tvHelmetDescription?.text = formattedDescription

                getSensors()
                Log.d("SensorsFragment", "Response: $response")
            },
            Response.ErrorListener { error ->
                if (error is ParseError && error.cause is JSONException) {
                    tvHelmetStatus.text = getString(R.string.donthavehelmet)
                    Toast.makeText(requireContext(), getString(R.string.donthavehelmet), Toast.LENGTH_SHORT).show()

                } else {
                    tvHelmetStatus.text = getString(R.string.erroroccured)
                    Toast.makeText(requireContext(), getString(R.string.erroroccured), Toast.LENGTH_SHORT).show()
                }
                val helmetNameTitle = view?.findViewById<TextView>(R.id.helmetNameTitle)
                val helmetDescriptionTitle = view?.findViewById<TextView>(R.id.helmetDescriptionTitle)
                val sensorsTitle = view?.findViewById<TextView>(R.id.sensorsTitle)
                sensorsTitle?.visibility = View.GONE
                helmetNameTitle?.visibility = View.GONE
                helmetDescriptionTitle?.visibility = View.GONE
                Log.e("SensorsFragment", "error: $error")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", "")
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        queue.add(request)


        return view
    }

    private fun checkAndCorrectResponse(response: String): JSONObject {
        val openingBraceCount = response.count { it == '{' }
        val closingBraceCount = response.count { it == '}' }

        if (openingBraceCount != closingBraceCount) {
            val correctedResponse = response.trim() + "}"
            try {
                return JSONObject(correctedResponse)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        try {
            return JSONObject(response)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return JSONObject() // Return an empty JSON object if the response is invalid
    }




    private fun getSensors() {
        val token = retrieveTokenFromSharedPreferences() // Retrieve the token from SharedPreferences

        val url = "http://192.168.0.151:8080/api/sensors/all"
        val queue = Volley.newRequestQueue(requireContext())

        val sensorList = mutableListOf<Sensor>()
        val adapter = SensorAdapter(sensorList)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        val request = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                Log.d("SensorsFragment", "Response: $response")
                //val jsonObject = JSONObject(response)
                val correctedResponse = checkAndCorrectResponse(response.toString())
                val data = correctedResponse.getJSONArray("data")
                //val data = jsonObject.getJSONArray("data")

                for (i in 0 until data.length()) {
                    val sensorObj = data.getJSONObject(i)
                    val id = sensorObj.getInt("id")
                    val name = sensorObj.getString("name")
                    val description = sensorObj.getString("description")
                    val sensor = Sensor(id, name, description)
                    sensorList.add(sensor)
                    adapter.notifyDataSetChanged()
                }

                adapter.onItemClick = { sensor ->
                    val sensorDetailsFragment = SensorDetailsFragment()
                    val bundle = Bundle()
                    bundle.putInt("sensorId", sensor.id)
                    bundle.putString("sensorName", sensor.name)
                    bundle.putString("sensorDescription", sensor.description)

                    sensorDetailsFragment.arguments = bundle

                    val fragmentManager = requireActivity().supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.frameLayout, sensorDetailsFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }


            },
            Response.ErrorListener { error ->
                // Handle the error
                val errorMessage = getString(R.string.erroroccured) + ": ${error.message}"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token" // Set the token in the request header
                return headers
            }
        }

        queue.add(request)
    }



    private fun retrieveTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SensorsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SensorsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}