package com.example.obviouscontrol

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotifyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotifyFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_notify, container, false)

        // Find the notifybtn view
        val notifyButton = view.findViewById<Button>(R.id.notifybtn)


        // Set onClick listener for notifybtn
        notifyButton.setOnClickListener {
            val message = view.findViewById<EditText>(R.id.et_message).text.toString()

            sendNotification(message)
        }

        return view
    }




    private fun sendNotification(message: String) {
        // Get the message from et_message

        // Check if et_message is empty
        if (message.isEmpty()) {
            // Show a toast message indicating that the message is empty
            Toast.makeText(requireContext(), getString(R.string.msgcantbeempty), Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val workerId = sharedPreferences.getString("worker_id", "")

        val myMessage = "Worker $workerId $message"

        // Create a JSON object with the message
        val jsonObject = JSONObject().apply {
            put("message", myMessage)
        }

        // Make the API call using Volley
        val url = "http://192.168.0.151:8080/api/notifications"
        val queue = Volley.newRequestQueue(requireContext())

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                Log.d("NotifyFragment", "Response: $response")
                Toast.makeText(requireContext(), response.optString("result"), Toast.LENGTH_SHORT).show()
                val etMessage = view?.findViewById<EditText>(R.id.et_message)
                if (etMessage != null) {
                    etMessage.text.clear()
                }
            },
            Response.ErrorListener { error ->
                Log.e("NotifyFragment", "Error: ${error.message}", error)
                Toast.makeText(requireContext(), getString(R.string.erroroccured), Toast.LENGTH_SHORT).show()
            })

        queue.add(request)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NotifyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotifyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}