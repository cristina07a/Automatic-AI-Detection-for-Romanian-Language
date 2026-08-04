package com.example.android_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel: ViewModel()
{

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val currentUser get() = auth.currentUser


    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    init
    {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if(auth.currentUser == null)
        {
            _authState.value = AuthState.Unauthenticated
        }
        else
        {
            _authState.value = AuthState.Authenticated
        }


    }

    fun login(email:String, password: String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Fill all fields!")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")

                }

            }
    }

    fun signup(email:String, password: String, password2: String){

        if(email.isEmpty() || password.isEmpty() || password2.isEmpty()){
            _authState.value = AuthState.Error("Fill all fields!")
            return
        }

        if(password != password2){
            _authState.value = AuthState.Error("Passwords must coincide")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")

                }

            }
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()

    data class Error(val message: String) : AuthState()

}