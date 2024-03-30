package com.kelme.interfaces

import com.kelme.model.ContactModel

interface QuantityListner {

    fun onQuantitychanged(userlist:ArrayList<ContactModel>)
}