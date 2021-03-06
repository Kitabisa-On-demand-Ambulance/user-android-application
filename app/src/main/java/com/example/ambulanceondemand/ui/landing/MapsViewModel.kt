package com.example.ambulanceondemand.ui.landing

import android.util.Log
import androidx.lifecycle.*
import com.example.ambulanceondemand.repository.ApiConfig
import com.example.ambulanceondemand.repository.driverrepository.ApiConfigDriver
import com.example.ambulanceondemand.ui.landing.ambulanceresponses.AmbulanceResponse
import com.example.ambulanceondemand.ui.landing.model.DirectionResponses
import com.example.ambulanceondemand.ui.landing.model.HospitalResponses
import com.example.ambulanceondemand.ui.landing.model.Location
import com.example.ambulanceondemand.ui.landing.model.ModelPreference
import com.example.ambulanceondemand.ui.verification.model.VerificationResponseX
import com.example.ambulanceondemand.util.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val pref: UserPreference): ViewModel() {

    private val _getDestination = MutableLiveData<HospitalResponses>()
    val getDestination: LiveData<HospitalResponses> = _getDestination

    private val _getRoute = MutableLiveData<DirectionResponses>()
    val getRoute: LiveData<DirectionResponses> = _getRoute

    private val _getAmbulances = MutableLiveData<VerificationResponseX>()
    val getAmbulances: LiveData<VerificationResponseX> = _getAmbulances

    private val _getNearAmbulance = MutableLiveData<AmbulanceResponse>()
    val getNearAmbulance: LiveData<AmbulanceResponse> = _getNearAmbulance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

//    val apiServices = RetrofitClient.apiServices(this)

    fun setDestination(location: String, radius: Int, type: String, key: String) {
        _isLoading.value = true
        val retrofit = ApiConfig.getRetrofitClientInstance()
        retrofit.getHospital(location, radius, type, key)
            .enqueue(object : Callback<HospitalResponses> {
                override fun onResponse(call: Call<HospitalResponses>, response: Response<HospitalResponses>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val hospitalName = responseBody?.results?.get(0)?.name
                        _getDestination.postValue(response.body())
                        Log.e(TAG, "Hospital berhasil $hospitalName")
                    }

                }

                override fun onFailure(call: Call<HospitalResponses>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("hospital error", "hospital error ${t.localizedMessage}")
                }
            })
    }

    fun setRoute(origin: String, destination: String, key: String) {
        _isLoading.value = true
        val retrofit = ApiConfig.getRetrofitClientInstance()
        retrofit.getDirection(origin, destination, key)
            .enqueue(object : Callback<DirectionResponses> {
                override fun onResponse(call: Call<DirectionResponses>, response: Response<DirectionResponses>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        _getRoute.postValue(response.body())
                        Log.e(TAG, "Rute berhasil ${response.message()}")
                    }

                }

                override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                    _isLoading.value = false
                    Log.e(TAG, "Rute gagal ${t.localizedMessage}")
                }
            })
    }

    fun setAmbulances(province : String) {
        _isLoading.value = true
        val retrofit = ApiConfigDriver.getRetrofitClientInstance()
        retrofit.getAmbulances(province)
            .enqueue(object : Callback<VerificationResponseX> {
                override fun onResponse(
                    call: Call<VerificationResponseX>,
                    response: Response<VerificationResponseX>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
//                        val responseBody = response.body()?.data?.get(0)?.namaDriver
                        _getAmbulances.postValue(response.body())
                        Log.d("berhasil cuy", "onResponse: ${response.body()?.data?.get(0)?.namaDriver}}")
//                        Log.e("<TAG>",  "Hospital berhasil $responseBody")
                    }
                }

                override fun onFailure(call: Call<VerificationResponseX>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("gagal woi", t.printStackTrace().toString())
                }
            })
    }

    fun setNearAmbulance(location : String, radius : Int ) {
        _isLoading.value = true
        val retrofit = ApiConfigDriver.getRetrofitClientInstance()
        retrofit.getNearAmbulances(location, radius)
            .enqueue(object : Callback<AmbulanceResponse> {
                override fun onResponse(
                    call: Call<AmbulanceResponse>,
                    response: Response<AmbulanceResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val found = response.body()?.found
                        if (found == 0){
                            setVerification(ModelPreference(false))
                            Log.d("Found", "onResponse: ${found}}")
                            Log.d("Found", "onResponse: ${response.body()?.ambulances?.get(0)?.kontakPicAmbulance}}")
                        }else{
                            _getNearAmbulance.postValue(response.body())
                        }

                    }
                }

                override fun onFailure(call: Call<AmbulanceResponse>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("gagal woi", t.printStackTrace().toString())
                }
            })
    }

    fun getVerification(): LiveData<ModelPreference>{
        return pref.getVerification().asLiveData()
    }

    fun setVerification(user: ModelPreference) {
        viewModelScope.launch {
            pref.setVerification(user)
        }
    }


    companion object {
        private const val TAG = "MapsViewModel"
    }

}