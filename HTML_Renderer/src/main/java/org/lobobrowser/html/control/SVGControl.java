/*
    GNU GENERAL LICENSE
    Copyright (C) 2006 The Lobo Project. Copyright (C) 2014 - 2017 Lobo Evolution

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    verion 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General License for more details.

    You should have received a copy of the GNU General Public
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    

    Contact info: lobochief@users.sourceforge.net; ivan.difrancesco@yahoo.it
 */

package org.lobobrowser.html.control;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lobobrowser.html.info.SVGInfo;
import org.lobobrowser.html.style.AbstractCSS2Properties;
import org.lobobrowser.html.svgimpl.SVGAnimateColorElementImpl;
import org.lobobrowser.html.svgimpl.SVGAnimateElementImpl;
import org.lobobrowser.html.svgimpl.SVGAnimateTransformElementImpl;
import org.lobobrowser.html.svgimpl.SVGAnimationImpl;
import org.lobobrowser.html.svgimpl.SVGCircleElementImpl;
import org.lobobrowser.html.svgimpl.SVGDefsElementImpl;
import org.lobobrowser.html.svgimpl.SVGEllipseElementImpl;
import org.lobobrowser.html.svgimpl.SVGGElementImpl;
import org.lobobrowser.html.svgimpl.SVGLineElementImpl;
import org.lobobrowser.html.svgimpl.SVGPathElementImpl;
import org.lobobrowser.html.svgimpl.SVGPolygonElementImpl;
import org.lobobrowser.html.svgimpl.SVGPolylineElementImpl;
import org.lobobrowser.html.svgimpl.SVGRectElementImpl;
import org.lobobrowser.html.svgimpl.SVGSVGElementImpl;
import org.lobobrowser.html.svgimpl.SVGTextElementImpl;
import org.lobobrowser.html.svgimpl.SVGUseElementImpl;
import org.lobobrowser.html.svgimpl.SVGUtility;
import org.lobobrowser.w3c.svg.SVGLengthList;
import org.lobobrowser.w3c.svg.SVGPathSegList;
import org.lobobrowser.w3c.svg.SVGPointList;
import org.lobobrowser.w3c.svg.SVGTransformList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGControl extends SVGBasicControl {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(SVGControl.class.getName());

	private SVGSVGElementImpl modelNode;

	private ArrayList<SVGInfo> svgList = new ArrayList<SVGInfo>();

	public SVGControl(SVGSVGElementImpl modelNode) {
		super(modelNode);
		this.modelNode = modelNode;
		NodeList childNodes = modelNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node n = childNodes.item(i);

			if (n instanceof SVGGElementImpl) {
				SVGGElementImpl svgGroup = (SVGGElementImpl) n;
				SVGTransformList tl = svgGroup.getTransform().getBaseVal();
				AbstractCSS2Properties style = svgGroup.getSVGStyle();
				setSvgiGroup(new SVGInfo(style, tl));
				svgGroup.setSvg(modelNode);
				NodeList gChildNodes = n.getChildNodes();
				for (int g = 0; g < gChildNodes.getLength(); g++) {
					Node n1 = gChildNodes.item(g);
					svgChildren(n1);
				}
			} else if (n instanceof SVGDefsElementImpl) {
				SVGDefsElementImpl defs = (SVGDefsElementImpl) n;
				SVGTransformList tl = defs.getTransform().getBaseVal();
				setSvgiGroup(new SVGInfo(null, tl));
			} else if (n instanceof SVGAnimateElementImpl) {
				setAnimate((SVGAnimateElementImpl) n);
			} else if (n instanceof SVGAnimateColorElementImpl) {
				setAnimate((SVGAnimateColorElementImpl) n);
			} else if (n instanceof SVGAnimateTransformElementImpl) {
				setAnimate((SVGAnimateTransformElementImpl) n);
			} else {
				svgChildren(n);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		try {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			SVGInfo group = getSvgiGroup();
			transform(g2d, new SVGInfo(), group);

			for (int i = 0; i < svgList.size(); i++) {
				SVGInfo svgi = svgList.get(i);
				switch (svgi.getMethod()) {
				case CIRCLE:
					circle(g2d, svgi, i);
					break;
				case RECT:
					rectangle(g2d, svgi, i);
					break;
				case ELLIPSE:
					ellipse(g2d, svgi, i);
					break;
				case LINE:
					line(g2d, svgi, i);
					break;
				case POLYGON:
					polygon(g2d, svgi, i);
					break;
				case POLYLINE:
					polyline(g2d, svgi, i);
					break;
				case PATH:
					path(g2d, svgi, i);
					break;
				case USE:
					use(g2d, svgi, modelNode);
					break;
				case TEXT:
					text(g2d, svgi, i);
					break;
				default:
					break;
				}
			}

		} catch (Exception ex) {
			logger.error("Error", ex);
		}
	}

	private void svgChildren(Node n) {

		if (n instanceof SVGCircleElementImpl) {
			SVGCircleElementImpl svgcircle = (SVGCircleElementImpl) n;
			String id = svgcircle.getId();
			float x = svgcircle.getCx().getBaseVal().getValue();
			float y = svgcircle.getCy().getBaseVal().getValue();
			float r = svgcircle.getR().getBaseVal().getValue();
			String clippath = svgcircle.getClipPath();
			SVGTransformList tl = svgcircle.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgcircle.getSVGStyle();
			SVGAnimationImpl animateElement = svgcircle.getAnimateElement();
			svgList.add(new SVGInfo(id, CIRCLE, x, y, r, style, false, clippath, tl, animateElement));
		}

		if (n instanceof SVGRectElementImpl) {
			SVGRectElementImpl svgrect = (SVGRectElementImpl) n;
			String id = svgrect.getId();
			float x = svgrect.getX().getBaseVal().getValue();
			float y = svgrect.getY().getBaseVal().getValue();
			float height = svgrect.getHeight().getBaseVal().getValue();
			float width = svgrect.getWidth().getBaseVal().getValue();
			float rx = svgrect.getRx().getBaseVal().getValue();
			float ry = svgrect.getRy().getBaseVal().getValue();
			String clippath = svgrect.getClipPath();
			SVGTransformList tl = svgrect.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgrect.getSVGStyle();
			SVGAnimationImpl animateElement = svgrect.getAnimateElement();
			svgList.add(new SVGInfo(id, RECT, x, y, height, width, rx, ry, style, false, clippath, tl, animateElement));
		}

		if (n instanceof SVGEllipseElementImpl) {
			SVGEllipseElementImpl svgellipse = (SVGEllipseElementImpl) n;
			String id = svgellipse.getId();
			float x = svgellipse.getCx().getBaseVal().getValue();
			float y = svgellipse.getCy().getBaseVal().getValue();
			float rx = svgellipse.getRx().getBaseVal().getValue();
			float ry = svgellipse.getRy().getBaseVal().getValue();
			String clippath = svgellipse.getClipPath();
			SVGTransformList tl = svgellipse.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgellipse.getSVGStyle();
			SVGAnimationImpl animateElement = svgellipse.getAnimateElement();
			svgList.add(new SVGInfo(id, ELLIPSE, x, y, rx, ry, style, false, clippath, tl, animateElement));
		}

		if (n instanceof SVGLineElementImpl) {
			SVGLineElementImpl svgline = (SVGLineElementImpl) n;
			String id = svgline.getId();
			float x1 = svgline.getX1().getBaseVal().getValue();
			float x2 = svgline.getX2().getBaseVal().getValue();
			float y1 = svgline.getY1().getBaseVal().getValue();
			float y2 = svgline.getY2().getBaseVal().getValue();
			String clippath = svgline.getClipPath();
			SVGTransformList tl = svgline.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgline.getSVGStyle();
			SVGAnimationImpl animateElement = svgline.getAnimateElement();
			svgList.add(new SVGInfo(id, LINE, x1, y1, x2, y2, style, clippath, false, tl, animateElement));
		}

		if (n instanceof SVGPolylineElementImpl) {
			SVGPolylineElementImpl svgpolyline = (SVGPolylineElementImpl) n;
			String id = svgpolyline.getId();
			SVGPointList points = svgpolyline.getPoints();
			String clippath = svgpolyline.getClipPath();
			SVGTransformList tl = svgpolyline.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgpolyline.getSVGStyle();
			SVGAnimationImpl animateElement = svgpolyline.getAnimateElement();
			svgList.add(new SVGInfo(id, POLYLINE, points, style, false, clippath, tl, animateElement));
		}

		if (n instanceof SVGPolygonElementImpl) {
			SVGPolygonElementImpl svgpolygon = (SVGPolygonElementImpl) n;
			String id = svgpolygon.getId();
			SVGPointList points = svgpolygon.getPoints();
			String clippath = svgpolygon.getClipPath();
			SVGTransformList tl = svgpolygon.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgpolygon.getSVGStyle();
			SVGAnimationImpl animateElement = svgpolygon.getAnimateElement();
			svgList.add(new SVGInfo(id, POLYGON, points, style, false, clippath, tl, animateElement));
		}

		if (n instanceof SVGPathElementImpl) {
			SVGPathElementImpl svgpath = (SVGPathElementImpl) n;
			String id = svgpath.getId();
			SVGPathSegList points = svgpath.getPathSegList();
			String clippath = svgpath.getClipPath();
			SVGTransformList tl = svgpath.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgpath.getSVGStyle();
			SVGAnimationImpl animateElement = svgpath.getAnimateElement();
			svgList.add(new SVGInfo(id, PATH, points, style, false, clippath, tl, animateElement));
		}

		if (n instanceof SVGGElementImpl) {
			SVGGElementImpl svgGroup = (SVGGElementImpl) n;
			SVGTransformList tl = svgGroup.getTransform().getBaseVal();
			AbstractCSS2Properties style = svgGroup.getSVGStyle();
			setSvgiGroup(new SVGInfo(style, tl));
			svgGroup.setSvg(modelNode);
			NodeList gChildNodes = svgGroup.getChildNodes();
			for (int g = 0; g < gChildNodes.getLength(); g++) {
				Node n1 = gChildNodes.item(g);
				svgChildren(n1);
			}
		}

		if (n instanceof SVGUseElementImpl) {
			SVGUseElementImpl use = (SVGUseElementImpl) n;
			float x = use.getX().getBaseVal().getValue();
			float y = use.getY().getBaseVal().getValue();
			String href = use.getHref().getBaseVal();
			String clippath = use.getClipPath();
			SVGTransformList tl = use.getTransform().getBaseVal();
			AbstractCSS2Properties style = use.getSVGStyle();
			svgList.add(new SVGInfo(USE, x, y, href, style, clippath, false, tl));
		}

		if (n instanceof SVGTextElementImpl) {
			SVGTextElementImpl text = (SVGTextElementImpl) n;
			String id = text.getId();
			float x = text.getX().getBaseVal().getValue();
			float y = text.getY().getBaseVal().getValue();
			Font font = SVGUtility.getFontValue(text.getFontFamily(), text.getFontSize());
			String clippath = text.getClipPath();
			String txt = SVGUtility.getText(n);
			String txtAnchor = text.getTextAnchor();
			SVGLengthList dyList = text.getDy().getBaseVal();
			SVGLengthList dxList = text.getDx().getBaseVal();
			SVGTransformList tl = text.getTransform().getBaseVal();
			AbstractCSS2Properties style = text.getSVGStyle();
			SVGAnimationImpl animateElement = text.getAnimateElement();
			svgList.add(new SVGInfo(id, TEXT, x, y, font, txt, txtAnchor, dyList, dxList, style, clippath, false, tl, animateElement));
		}
	}
}
