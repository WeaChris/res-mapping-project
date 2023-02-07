package com.example.sxediasipriject;

public class User {
    public String fullName, password,email, age, address, bloodType, emergency_contact, known_sickness, personal_phone;

    public User(){

    }
    public User( String password,String email){

        this.password = password;
        this.email = email;

    }
}
