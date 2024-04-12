package com.dyzs.svgparser.lib

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.Path
import android.graphics.Path.FillType
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.text.TextUtils


/**
 * author : cbk
 * date   : 2023/4/12
 * desc   :
 */
object SvgUtils {

    fun enumPath(svgElementVector: SvgElementVector?): List<Path?> {
        if (svgElementVector?.getSvgElementPathList() == null) {
            return emptyList()
        }
        val paths: MutableList<Path?> = ArrayList()
        for (elementPath in svgElementVector.getSvgElementPathList()) {
            paths.add(elementPath.parserPath())
        }
        return paths
    }

    // 枚举RegionList
    fun enumPathRegion(paths: MutableList<Path?>): List<Region> {
        val regionList: MutableList<Region> = ArrayList()
        if (paths == null) {
            return regionList
        }
        for (path in paths) {
            regionList.add(createRegion(path!!))
        }
        return regionList
    }


    /**
     * 存在path为像素点的情况下，或者为直线的情况下，region rect为0
     * 导致直线rect无法被创建region
     *
     * @param path
     * @return
     */
    fun createRegion(path: Path): Region {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val region = Region()
        region.setPath(
            path,
            Region(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
        )
        return region
    }

    fun createRegionRect(path: Path): Rect {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val region = Region()
        region.setPath(
            path,
            Region(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
        )
        if (region.isEmpty) {
            val rect = Rect()
            bounds.round(rect)
            return rect
        }
        return region.bounds
    }

    /**
     * 穷举Svg显示区域
     * todo 待解决bug，存在path为像素点的情况下，或者为直线的情况下，创建region时rect 参数值为0
     */
    fun exhaustionSvgInRectByRegion(regionList: List<Region>?): Rect {
        if (regionList.isNullOrEmpty()) return Rect()
        val rect = Rect()
        val l = ArrayList<Int>()
        val t = ArrayList<Int>()
        val r = ArrayList<Int>()
        val b = ArrayList<Int>()
        for (region in regionList) {
            if (region.isEmpty) continue
            l.add(region.bounds.left)
            t.add(region.bounds.top)
            r.add(region.bounds.right)
            b.add(region.bounds.bottom)
        }
        l.sort()
        t.sort()
        r.sort()
        b.sort()
        if (l.size > 0) {
            rect.left = ArrayList(l)[0]
        }
        if (t.size > 0) {
            rect.top = ArrayList(t)[0]
        }
        if (r.size > 0) {
            rect.right = ArrayList(r)[r.size - 1]
        }
        if (b.size > 0) {
            rect.bottom = ArrayList(b)[b.size - 1]
        }
        return rect
    }


    /**
     * 穷举Svg显示区域
     */
    fun exhaustionSvgInRectByPaths(paths: List<Path?>?): Rect {
        if (paths.isNullOrEmpty()) return Rect()
        val rectList: MutableList<Rect> = ArrayList()
        for (path in paths) {
            rectList.add(createRegionRect(path!!))
        }
        val rect = Rect()
        val l = ArrayList<Int>()
        val t = ArrayList<Int>()
        val r = ArrayList<Int>()
        val b = ArrayList<Int>()
        for (region in rectList) {
            if (region.isEmpty) continue
            l.add(region.left)
            t.add(region.top)
            r.add(region.right)
            b.add(region.bottom)
        }
        l.sort()
        t.sort()
        r.sort()
        b.sort()
        if (l.size > 0) {
            rect.left = ArrayList(l)[0]
        }
        if (t.size > 0) {
            rect.top = ArrayList(t)[0]
        }
        if (r.size > 0) {
            rect.right = ArrayList(r)[r.size - 1]
        }
        if (b.size > 0) {
            rect.bottom = ArrayList(b)[b.size - 1]
        }
        return rect
    }

    fun matchFillType(fillType: String?): FillType {
        var pathFillType = FillType.EVEN_ODD
        if (TextUtils.isEmpty(fillType)) {
            return pathFillType
        }
        when (fillType) {
            "evenOdd" -> pathFillType = FillType.EVEN_ODD
            "winding" -> pathFillType = FillType.WINDING
            "inverseWinding" -> pathFillType = FillType.INVERSE_WINDING
            "inverseEvenOdd" -> pathFillType = FillType.INVERSE_EVEN_ODD
        }
        return pathFillType
    }

    /**
     * 设置圆角效果   BUTT：默认（无圆角） ROUND：圆角  SQUARE：方角
     * @param cap
     * @return
     */
    fun matchPaintCap(cap: String?): Cap {
        var ret = Cap.ROUND
        if (TextUtils.isEmpty(cap)) {
            return ret
        }
        when (cap) {
            "round" -> ret = Cap.ROUND
            "butt" -> ret = Cap.BUTT
            "square" -> ret = Cap.SQUARE
        }
        return ret
    }

    /**
     * 设置拐角风格   MITER：尖角    ROUND：圆角   BEVEL：折角
     * @param join
     * @return
     */
    fun matchPaintJoin(join: String?): Join {
        var ret = Join.MITER
        if (TextUtils.isEmpty(join)) {
            return ret
        }
        when (join) {
            "miter" -> ret = Join.MITER
            "bevel" -> ret = Join.BEVEL
            "round" -> ret = Join.ROUND
        }
        return ret
    }

    fun createElementPathPaint(elementPath: SvgElementPath) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.parseColor(elementPath.getStrokeColor())
        paint.strokeWidth = elementPath.getStrokeWidth()!!.toInt().toFloat()
        paint.strokeCap = matchPaintCap(elementPath.getStrokeLineCap())
    }

}