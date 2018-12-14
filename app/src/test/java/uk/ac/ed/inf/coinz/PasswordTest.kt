package uk.ac.ed.inf.coinz

import org.junit.Assert
import org.junit.Test


class PasswordTest {
    private val valid_password = "test123" // length >= 6
    private val invalid_password = "test" // length < 6
    @Test
    fun password_isOK() {
        Assert.assertEquals(true, validPwd(valid_password))
    }
    @Test
    fun password_notOK() {
        Assert.assertEquals(false, validPwd(invalid_password))
    }

    fun validPwd(pwd: String): Boolean {
        return pwd.length>=6
    }
}