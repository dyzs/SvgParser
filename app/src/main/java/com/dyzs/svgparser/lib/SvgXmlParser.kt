package com.dyzs.svgparser.lib

import android.content.Context
import android.text.TextUtils
import org.w3c.dom.Element
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory

/**
 * author : cbk
 * date   : 2023/3/7
 * desc   :
 */
object SvgXmlParser {
    /**
     * InputStream inputStream = mContext.getResources().openRawResource(R.raw.china);
     * <p>
     * <p>
     * DocumentBuilderFactory facotory = DocumentBuilderFactory.newInstance();
     * <p>
     * try {
     * DocumentBuilder builder = facotory.newDocumentBuilder();
     * <p>
     * Document doc = builder.parse(inputStream);
     * <p>
     * Element rootElement = doc.getDocumentElement();
     * NodeList items = rootElement.getElementsByTagName("path");
     * <p>
     * for (int i = 0; i < items.getLength(); i++) {
     * Element element = (Element) items.item(i);
     * String pathData = element.getAttribute("android:pathData");
     * <p>
     * Path path = PathParser.createPathFromPathData(pathData);
     * ProvinceItem item = new ProvinceItem(path);
     * list.add(new ProvinceItem(path));
     * }
     * }catch (ParserConfigurationException e) {
     * e.printStackTrace();
     * } catch (SAXException e) {
     * e.printStackTrace();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     */

    fun parserXml2Bean(context: Context): SvgElementVector {
        val elementVector = SvgElementVector()
        var elementPath = SvgElementPath()
        val elementVectorList = mutableListOf<SvgElementPath>()
        try {
            val inputStream =
                context.resources.assets.open("svgxml/vector_drawable_the_peoples_republic_of_china.xml")
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(inputStream)
            val rootElement = doc.documentElement
            elementVector.setViewportWidth(rootElement.getAttribute("android:viewportWidth"))
            elementVector.setViewportHeight(rootElement.getAttribute("android:viewportHeight"))
            val items = rootElement.getElementsByTagName("path")
            for (i in 0 until items.length) {
                val element = items.item(i) as Element
                val name = element.getAttribute("android:name")
                val pathData = element.getAttribute("android:pathData")
                val fillAlpha = element.getAttribute("android:fillAlpha")
                val fillColor = element.getAttribute("android:fillColor")
                val fillType = element.getAttribute("android:fillType")
                val strokeWidth = element.getAttribute("android:strokeWidth")
                val strokeAlpha = element.getAttribute("android:strokeAlpha")
                val strokeColor = element.getAttribute("android:strokeColor")
                val strokeLineCap = element.getAttribute("android:strokeLineCap")
                val strokeLineJoin = element.getAttribute("android:strokeLineJoin")
                elementPath = SvgElementPath()
                elementPath.setName(
                    if (TextUtils.isEmpty(name)) UUID.randomUUID().toString() else name
                )
                elementPath.setPathData(pathData)
                elementPath.setFillAlpha(fillAlpha)
                elementPath.setFillColor(fillColor)
                elementPath.setFillType(fillType)
                elementPath.setStrokeWidth(strokeWidth)
                elementPath.setStrokeAlpha(strokeAlpha)
                elementPath.setStrokeColor(strokeColor)
                elementPath.setStrokeLineCap(strokeLineCap)
                elementPath.setStrokeLineJoin(strokeLineJoin)
                elementVectorList.add(elementPath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            elementVector.getSvgElementPathList().addAll(elementVectorList)
        }
        return elementVector
    }
}