package com.dyzs.svgparser.lib
/**
 * author : cbk
 * date   : 2023/3/7
 * desc   :
 * xmlns:android="http://schemas.android.com/apk/res/android"
 */
class SvgElementVector {

    private var viewportWidth = "0"

    private var viewportHeight = "0"

    private var width = "0dp"

    private var height = "0dp"

    private var svgElementPathList: MutableList<SvgElementPath> = ArrayList()

    fun getViewportWidth(): String {
        return viewportWidth
    }

    fun setViewportWidth(viewportWidth: String) {
        this.viewportWidth = viewportWidth
    }

    fun getViewportHeight(): String {
        return viewportHeight
    }

    fun setViewportHeight(viewportHeight: String) {
        this.viewportHeight = viewportHeight
    }

    fun getWidth(): String {
        return width
    }

    fun setWidth(width: String) {
        this.width = width
    }

    fun getHeight(): String {
        return height
    }

    fun setHeight(height: String) {
        this.height = height
    }

    fun getSvgElementPathList(): MutableList<SvgElementPath> {
        return svgElementPathList
    }

    fun setSvgElementPathList(svgElementPathList: MutableList<SvgElementPath>) {
        this.svgElementPathList = svgElementPathList
    }
}