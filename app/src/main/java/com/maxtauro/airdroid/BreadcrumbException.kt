package com.maxtauro.airdroid

class BreadcrumbException(private val msg: String): Exception() {

    companion object {
        private const val TAG = "BreadcrumbException"
    }
}