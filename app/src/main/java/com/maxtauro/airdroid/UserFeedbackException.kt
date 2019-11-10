package com.maxtauro.airdroid

class UserFeedbackException(private val msg: String): IllegalStateException(msg) {

    companion object {
        private const val TAG = "UserFeedbackException"
    }
}