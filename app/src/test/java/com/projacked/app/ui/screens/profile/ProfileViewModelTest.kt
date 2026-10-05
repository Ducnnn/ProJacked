package com.projacked.app.ui.screens.profile

import com.projacked.app.fakes.FakeAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileViewModelTest {

    @Test
    fun `logOut signs out`() {
        val auth = FakeAuthRepository(initialUserId = "uid-a")

        ProfileViewModel(auth).logOut()

        assertEquals(1, auth.signOutCount)
        assertNull(auth.currentUserId())
    }
}
