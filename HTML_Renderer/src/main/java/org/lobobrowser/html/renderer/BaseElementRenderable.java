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

package org.lobobrowser.html.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.batik.transcoder.TranscoderException;
import org.lobobrowser.html.dombl.ModelNode;
import org.lobobrowser.html.dombl.SVGRasterizer;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.lobobrowser.html.info.BackgroundInfo;
import org.lobobrowser.html.info.BorderInfo;
import org.lobobrowser.html.renderstate.RenderState;
import org.lobobrowser.html.style.AbstractCSS2Properties;
import org.lobobrowser.html.style.CSSValuesProperties;
import org.lobobrowser.html.style.HtmlInsets;
import org.lobobrowser.html.style.HtmlValues;
import org.lobobrowser.http.UserAgentContext;
import org.lobobrowser.util.SSLCertificate;
import org.lobobrowser.util.Strings;
import org.lobobrowser.util.gui.GUITasks;
import org.w3c.dom.css.CSS2Properties;

/**
 * The Class BaseElementRenderable.
 */
public abstract class BaseElementRenderable extends BaseRCollection
		implements RElement, RenderableContainer, ImageObserver, CSSValuesProperties {

	/** The Constant INVALID_SIZE. */
	protected static final Integer INVALID_SIZE = new Integer(Integer.MIN_VALUE);

	/**
	 * A collection of all GUI components added by descendents.
	 */
	private Collection<Component> guiComponents = null;

	/**
	 * A list of absolute positioned or float parent-child pairs.
	 */
	protected Collection<DelayedPair> delayedPairs = null;

	/**
	 * Background color which may be different to that from RenderState in the
	 * case of a Document node.
	 */
	protected Color backgroundColor;

	/** The background image. */
	protected volatile Image backgroundImage;

	/** The z index. */
	protected int zIndex;

	/** The border top color. */
	protected Color borderTopColor;

	/** The border left color. */
	protected Color borderLeftColor;

	/** The border bottom color. */
	protected Color borderBottomColor;

	/** The border right color. */
	protected Color borderRightColor;

	/** The border insets. */
	protected Insets borderInsets;

	/** The margin insets. */
	protected Insets marginInsets;

	/** The padding insets. */
	protected Insets paddingInsets;

	/** The border info. */
	protected BorderInfo borderInfo;

	/** The last background image uri. */
	protected URL lastBackgroundImageUri;

	/** The default margin insets. */
	protected Insets defaultMarginInsets = null;

	/** The default padding insets. */
	protected Insets defaultPaddingInsets = null;

	/** The overflow x. */
	protected int overflowX;

	/** The overflow y. */
	protected int overflowY;

	/** The user agent context. */
	protected final UserAgentContext userAgentContext;

	/**
	 * Instantiates a new base element renderable.
	 *
	 * @param container
	 *            the container
	 * @param modelNode
	 *            the model node
	 * @param ucontext
	 *            the ucontext
	 */
	public BaseElementRenderable(RenderableContainer container, ModelNode modelNode, UserAgentContext ucontext) {
		super(container, modelNode);
		this.userAgentContext = ucontext;
	}

	/**
	 * Sets the default padding insets.
	 *
	 * @param insets
	 *            the new default padding insets
	 */
	public void setDefaultPaddingInsets(Insets insets) {
		this.defaultPaddingInsets = insets;
	}

	/**
	 * Sets the default margin insets.
	 *
	 * @param insets
	 *            the new default margin insets
	 */
	public void setDefaultMarginInsets(Insets insets) {
		this.defaultMarginInsets = insets;
	}

	/**
	 * Gets the alignment x.
	 *
	 * @return the alignment x
	 */
	public float getAlignmentX() {
		return 0.0f;
	}

	/**
	 * Gets the alignment y.
	 *
	 * @return the alignment y
	 */
	public float getAlignmentY() {
		return 0.0f;
	}

	/** The layout deep can be invalidated. */
	protected boolean layoutDeepCanBeInvalidated = false;

	/**
	 * Invalidates this Renderable and all descendents. This is only used in
	 * special cases, such as when a new style sheet is added.
	 */
	@Override
	public final void invalidateLayoutDeep() {
		if (this.layoutDeepCanBeInvalidated) {
			this.layoutDeepCanBeInvalidated = false;
			this.invalidateLayoutLocal();
			Iterator<?> i = this.getRenderables();
			if (i != null) {
				while (i.hasNext()) {
					Object r = i.next();
					if (r instanceof RCollection) {
						((RCollection) r).invalidateLayoutDeep();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.BaseBoundableRenderable#
	 * invalidateLayoutLocal()
	 */
	@Override
	protected void invalidateLayoutLocal() {
		RenderState rs = this.modelNode.getRenderState();
		if (rs != null) {
			rs.invalidate();
		}
		this.overflowX = RenderState.OVERFLOW_NONE;
		this.overflowY = RenderState.OVERFLOW_NONE;
		this.declaredWidth = INVALID_SIZE;
		this.declaredHeight = INVALID_SIZE;
		this.lastAvailHeightForDeclared = -1;
		this.lastAvailWidthForDeclared = -1;
	}

	/** The declared width. */
	private Integer declaredWidth = INVALID_SIZE;

	/** The declared height. */
	private Integer declaredHeight = INVALID_SIZE;

	/** The last avail width for declared. */
	private int lastAvailWidthForDeclared = -1;

	/** The last avail height for declared. */
	private int lastAvailHeightForDeclared = -1;

	/**
	 * Gets the declared width.
	 *
	 * @param renderState
	 *            the render state
	 * @param actualAvailWidth
	 *            the actual avail width
	 * @return the declared width
	 */
	protected Integer getDeclaredWidth(RenderState renderState, int actualAvailWidth) {
		Integer dw = this.declaredWidth;
		if (dw == INVALID_SIZE || actualAvailWidth != this.lastAvailWidthForDeclared) {
			this.lastAvailWidthForDeclared = actualAvailWidth;
			int dwInt = this.getDeclaredWidthImpl(renderState, actualAvailWidth);
			dw = dwInt == -1 ? null : new Integer(dwInt);
			this.declaredWidth = dw;
		}
		return dw;
	}

	/**
	 * Checks for declared width.
	 *
	 * @return true, if successful
	 */
	public final boolean hasDeclaredWidth() {
		Integer dw = this.declaredWidth;
		if (dw == INVALID_SIZE) {
			Object rootNode = this.modelNode;
			if (rootNode instanceof HTMLElementImpl) {
				HTMLElementImpl element = (HTMLElementImpl) rootNode;
				CSS2Properties props = element.getCurrentStyle();
				if (props == null) {
					return false;
				}
				return !Strings.isBlank(props.getWidth());
			}
			return false;
		}
		return dw != null;
	}

	/**
	 * Gets the declared width impl.
	 *
	 * @param renderState
	 *            the render state
	 * @param availWidth
	 *            the avail width
	 * @return the declared width impl
	 */
	private final int getDeclaredWidthImpl(RenderState renderState, int availWidth) {
		Object rootNode = this.modelNode;
		if (rootNode instanceof HTMLElementImpl) {
			HTMLElementImpl element = (HTMLElementImpl) rootNode;
			CSS2Properties props = element.getCurrentStyle();
			if (props == null) {
				return -1;
			}
			String widthText = props.getWidth();

			if (INHERIT.equalsIgnoreCase(widthText)) {
				widthText = element.getParentStyle().getWidth();
			} else if (INITIAL.equalsIgnoreCase(widthText)) {
				widthText = "100%";
			}

			int width = -1;

			if (widthText != null) {
				width = HtmlValues.getPixelSize(widthText, renderState, -1, availWidth);
			}

			if (props.getMaxWidth() != null) {
				int maxWidth = HtmlValues.getPixelSize(props.getMaxWidth(), renderState, -1, availWidth);

				if (width == -1 || width > maxWidth) {
					width = maxWidth;
				}
			}

			if (props.getMinWidth() != null) {
				int minWidth = HtmlValues.getPixelSize(props.getMinWidth(), element.getRenderState(), 0, availWidth);

				if (width == 0 || width < minWidth) {
					width = minWidth;
				}
			}

			return width;
		} else {
			return -1;
		}
	}

	/**
	 * Gets the declared height.
	 *
	 * @param renderState
	 *            the render state
	 * @param actualAvailHeight
	 *            the actual avail height
	 * @return the declared height
	 */
	protected Integer getDeclaredHeight(RenderState renderState, int actualAvailHeight) {
		Integer dh = this.declaredHeight;
		if (dh == INVALID_SIZE || actualAvailHeight != this.lastAvailHeightForDeclared) {
			this.lastAvailHeightForDeclared = actualAvailHeight;
			int dhInt = this.getDeclaredHeightImpl(renderState, actualAvailHeight);
			dh = dhInt == -1 ? null : new Integer(dhInt);
			this.declaredHeight = dh;
		}
		return dh;
	}

	/**
	 * Gets the declared height impl.
	 *
	 * @param renderState
	 *            the render state
	 * @param availHeight
	 *            the avail height
	 * @return the declared height impl
	 */
	protected int getDeclaredHeightImpl(RenderState renderState, int availHeight) {
		Object rootNode = this.modelNode;
		if (rootNode instanceof HTMLElementImpl) {
			HTMLElementImpl element = (HTMLElementImpl) rootNode;
			CSS2Properties props = element.getCurrentStyle();
			if (props == null) {
				return -1;
			}
			String heightText = props.getHeight();

			if (INHERIT.equalsIgnoreCase(heightText)) {
				heightText = element.getParentStyle().getHeight();
			} else if (INITIAL.equalsIgnoreCase(heightText)) {
				heightText = "100%";
			}

			int height = -1;

			if (heightText != null) {
				height = HtmlValues.getPixelSize(heightText, element.getRenderState(), -1, availHeight);
			}

			if (props.getMaxHeight() != null) {
				int maxHeight = HtmlValues.getPixelSize(props.getMaxHeight(), element.getRenderState(), -1,
						availHeight);

				if (height == 0 || height > maxHeight) {
					height = maxHeight;
				}
			}

			if (props.getMinHeight() != null) {
				int minHeight = HtmlValues.getPixelSize(props.getMinHeight(), element.getRenderState(), -1,
						availHeight);

				if (height == 0 || height < minHeight) {
					height = minHeight;
				}
			}

			return height;
		} else {
			return -1;
		}
	}

	/**
	 * All overriders should call super implementation.
	 *
	 * @param g
	 *            the g
	 */
	@Override
	public void paint(Graphics g) {
	}

	/**
	 * Lays out children, and deals with "valid" state. Override doLayout method
	 * instead of this one.
	 *
	 * @param availWidth
	 *            the avail width
	 * @param availHeight
	 *            the avail height
	 * @param sizeOnly
	 *            the size only
	 */
	@Override
	public final void layout(int availWidth, int availHeight, boolean sizeOnly) {
		// Must call doLayout regardless of validity state.
		try {
			this.doLayout(availWidth, availHeight, sizeOnly);
		} finally {
			this.layoutUpTreeCanBeInvalidated = true;
			this.layoutDeepCanBeInvalidated = true;
		}
	}

	/**
	 * Do layout.
	 *
	 * @param availWidth
	 *            the avail width
	 * @param availHeight
	 *            the avail height
	 * @param sizeOnly
	 *            the size only
	 */
	protected abstract void doLayout(int availWidth, int availHeight, boolean sizeOnly);

	/**
	 * Send gui components to parent.
	 */
	protected final void sendGUIComponentsToParent() {
		// Ensures that parent has all the components
		// below this renderer node. (Parent expected to have removed them).
		Collection<Component> gc = this.guiComponents;
		if (gc != null) {
			RenderableContainer rc = this.container;
			Iterator<Component> i = gc.iterator();
			while (i.hasNext()) {
				rc.addComponent(i.next());
			}
		}
	}

	/**
	 * Clear gui components.
	 */
	protected final void clearGUIComponents() {
		Collection<Component> gc = this.guiComponents;
		if (gc != null) {
			gc.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.render.RenderableContainer#add(java.awt.Component)
	 */
	@Override
	public Component addComponent(Component component) {
		// Expected to be called in GUI thread.
		// Adds only in local collection.
		// Does not remove from parent.
		// Sending components to parent is done
		// by sendGUIComponentsToParent().
		Collection<Component> gc = this.guiComponents;
		if (gc == null) {
			gc = new HashSet<Component>(1);
			this.guiComponents = gc;
		}
		gc.add(component);
		return component;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.renderer.RenderableContainer#updateAllWidgetBounds()
	 */
	@Override
	public void updateAllWidgetBounds() {
		this.container.updateAllWidgetBounds();
	}

	/**
	 * Updates widget bounds below this node only. Should not be called during
	 * general rendering.
	 */
	public void updateWidgetBounds() {
		java.awt.Point guiPoint = this.getGUIPoint(0, 0);
		this.updateWidgetBounds(guiPoint.x, guiPoint.y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.dombl.UINode#getBoundsRelativeToBlock()
	 */
	@Override
	public Rectangle getBoundsRelativeToBlock() {
		RCollection parent = this;
		int x = 0, y = 0;
		while (parent != null) {
			x += parent.getX();
			y += parent.getY();
			parent = parent.getParent();
			if (parent instanceof RElement) {
				break;
			}
		}
		return new Rectangle(x, y, this.getWidth(), this.getHeight());
	}

	/**
	 * Clear style.
	 *
	 * @param isRootBlock
	 *            the is root block
	 */
	protected void clearStyle(boolean isRootBlock) {
		this.borderInfo = null;
		this.borderInsets = null;
		this.borderTopColor = null;
		this.borderLeftColor = null;
		this.borderBottomColor = null;
		this.borderRightColor = null;
		this.zIndex = 0;
		this.backgroundColor = null;
		this.backgroundImage = null;
		this.lastBackgroundImageUri = null;
		this.overflowX = RenderState.OVERFLOW_VISIBLE;
		this.overflowY = RenderState.OVERFLOW_VISIBLE;
		if (isRootBlock) {
			// The margin of the root block behaves like extra padding.
			Insets insets1 = this.defaultMarginInsets;
			Insets insets2 = this.defaultPaddingInsets;
			Insets finalInsets = insets1 == null ? null
					: new Insets(insets1.top, insets1.left, insets1.bottom, insets1.right);
			if (insets2 != null) {
				if (finalInsets == null) {
					finalInsets = new Insets(insets2.top, insets2.left, insets2.bottom, insets2.right);
				} else {
					finalInsets.top += insets2.top;
					finalInsets.bottom += insets2.bottom;
					finalInsets.left += insets2.left;
					finalInsets.right += insets2.right;
				}
			}
			this.marginInsets = null;
			this.paddingInsets = finalInsets;
		} else {
			this.marginInsets = this.defaultMarginInsets;
			this.paddingInsets = this.defaultPaddingInsets;
		}
	}

	/**
	 * Apply style.
	 *
	 * @param availWidth
	 *            the avail width
	 * @param availHeight
	 *            the avail height
	 */
	protected void applyStyle(int availWidth, int availHeight) {
		// TODO: Can be optimized if there's no style?
		// TODO: There's part of this that doesn't depend on availWidth
		// and availHeight, so it doesn't need to be redone on
		// every resize.
		// Note: Overridden by tables and lists.

		Object rootNode = this.modelNode;
		HTMLElementImpl rootElement;
		boolean isRootBlock;
		if (rootNode instanceof HTMLDocumentImpl) {
			isRootBlock = true;
			HTMLDocumentImpl doc = (HTMLDocumentImpl) rootNode;
			// Need to get BODY tag, for bgcolor, etc.
			rootElement = (HTMLElementImpl) doc.getBody();
		} else {
			isRootBlock = false;
			rootElement = (HTMLElementImpl) rootNode;
		}
		if (rootElement == null) {
			this.clearStyle(isRootBlock);
			return;
		}
		RenderState rs = rootElement.getRenderState();
		if (rs == null) {
			throw new IllegalStateException(
					"Element without render state: " + rootElement + "; parent=" + rootElement.getParentNode());
		}
		BackgroundInfo binfo = rs.getBackgroundInfo();
		this.backgroundColor = binfo == null ? null : binfo.getBackgroundColor();
		URL backgroundImageUri = binfo == null ? null : binfo.getBackgroundImage();
		if (backgroundImageUri == null) {
			this.backgroundImage = null;
			this.lastBackgroundImageUri = null;
		} else if (!backgroundImageUri.equals(this.lastBackgroundImageUri)) {
			this.lastBackgroundImageUri = backgroundImageUri;
			this.loadBackgroundImage(backgroundImageUri);
		}
		AbstractCSS2Properties props = rootElement.getCurrentStyle();
		if (props == null) {
			this.clearStyle(isRootBlock);
		} else {
			BorderInfo borderInfo = rs.getBorderInfo();
			this.borderInfo = borderInfo;
			HtmlInsets binsets = borderInfo == null ? null : borderInfo.getInsets();
			HtmlInsets minsets = rs.getMarginInsets();
			HtmlInsets pinsets = rs.getPaddingInsets();
			Insets defaultMarginInsets = this.defaultMarginInsets;
			int dmleft = 0, dmright = 0, dmtop = 0, dmbottom = 0;
			if (defaultMarginInsets != null) {
				dmleft = defaultMarginInsets.left;
				dmright = defaultMarginInsets.right;
				dmtop = defaultMarginInsets.top;
				dmbottom = defaultMarginInsets.bottom;
			}
			Insets defaultPaddingInsets = this.defaultPaddingInsets;
			int dpleft = 0, dpright = 0, dptop = 0, dpbottom = 0;
			if (defaultPaddingInsets != null) {
				dpleft = defaultPaddingInsets.left;
				dpright = defaultPaddingInsets.right;
				dptop = defaultPaddingInsets.top;
				dpbottom = defaultPaddingInsets.bottom;
			}
			Insets borderInsets = binsets == null ? null
					: binsets.getAWTInsets(0, 0, 0, 0, availWidth, availHeight, 0, 0);
			if (borderInsets == null) {
				borderInsets = RBlockViewport.ZERO_INSETS;
			}
			Insets paddingInsets = pinsets == null ? defaultPaddingInsets
					: pinsets.getAWTInsets(dptop, dpleft, dpbottom, dpright, availWidth, availHeight, 0, 0);
			if (paddingInsets == null) {
				paddingInsets = RBlockViewport.ZERO_INSETS;
			}
			Insets tentativeMarginInsets = minsets == null ? defaultMarginInsets
					: minsets.getAWTInsets(dmtop, dmleft, dmbottom, dmright, availWidth, availHeight, 0, 0);
			if (tentativeMarginInsets == null) {
				tentativeMarginInsets = RBlockViewport.ZERO_INSETS;
			}
			int actualAvailWidth = availWidth - paddingInsets.left - paddingInsets.right - borderInsets.left
					- borderInsets.right - tentativeMarginInsets.left - tentativeMarginInsets.right;
			int actualAvailHeight = availHeight - paddingInsets.top - paddingInsets.bottom - borderInsets.top
					- borderInsets.bottom - tentativeMarginInsets.top - tentativeMarginInsets.bottom;
			Integer declaredWidth = this.getDeclaredWidth(rs, actualAvailWidth);
			Integer declaredHeight = this.getDeclaredHeight(rs, actualAvailHeight);
			int autoMarginX = 0, autoMarginY = 0;
			if (declaredWidth != null) {
				autoMarginX = (availWidth - declaredWidth.intValue()
						- (borderInsets == null ? 0 : borderInsets.left - borderInsets.right)
						- (paddingInsets == null ? 0 : paddingInsets.left - paddingInsets.right)) / 2;
			}
			if (declaredHeight != null) {
				autoMarginY = (availHeight - declaredHeight.intValue()
						- (borderInsets == null ? 0 : borderInsets.top - borderInsets.bottom)
						- (paddingInsets == null ? 0 : paddingInsets.top - paddingInsets.bottom)) / 2;
			}
			this.borderInsets = borderInsets;
			if (isRootBlock) {
				// In the root block, the margin behaves like an extra padding.
				Insets regularMarginInsets = autoMarginX == 0 && autoMarginY == 0 ? tentativeMarginInsets
						: minsets == null ? defaultMarginInsets
								: minsets.getAWTInsets(dmtop, dmleft, dmbottom, dmright, availWidth, availHeight,
										autoMarginX, autoMarginY);
				if (regularMarginInsets == null) {
					regularMarginInsets = RBlockViewport.ZERO_INSETS;
				}
				this.marginInsets = null;
				this.paddingInsets = new Insets(paddingInsets.top + regularMarginInsets.top,
						paddingInsets.left + regularMarginInsets.left,
						paddingInsets.bottom + regularMarginInsets.bottom,
						paddingInsets.right + regularMarginInsets.right);
			} else {
				this.paddingInsets = paddingInsets;
				this.marginInsets = autoMarginX == 0 && autoMarginY == 0 ? tentativeMarginInsets
						: minsets == null ? defaultMarginInsets
								: minsets.getAWTInsets(dmtop, dmleft, dmbottom, dmright, availWidth, availHeight,
										autoMarginX, autoMarginY);
			}
			if (borderInfo != null) {
				this.borderTopColor = borderInfo.getTopColor();
				this.borderLeftColor = borderInfo.getLeftColor();
				this.borderBottomColor = borderInfo.getBottomColor();
				this.borderRightColor = borderInfo.getRightColor();
			} else {
				this.borderTopColor = null;
				this.borderLeftColor = null;
				this.borderBottomColor = null;
				this.borderRightColor = null;
			}
			String zIndex = props.getZIndex();
			if (zIndex != null) {
				this.zIndex = HtmlValues.getPixelSize(zIndex, null, 0);
			} else {
				this.zIndex = 0;
			}
			this.overflowX = rs.getOverflowX();
			this.overflowY = rs.getOverflowY();
		}

		// Check if background image needs to be loaded
	}

	/**
	 * Load background image.
	 *
	 * @param imageURL
	 *            the image url
	 */
	protected void loadBackgroundImage(final URL imageURL) {
		Image image = null;
		String url = imageURL.toString();
		try {
			SSLCertificate.setCertificate();

			URLConnection con = imageURL.openConnection();
			con.setRequestProperty("User-Agent", UserAgentContext.DEFAULT_USER_AGENT);

			if (url.endsWith(".svg")) {
				SVGRasterizer r = new SVGRasterizer(imageURL);
				image = r.bufferedImageToImage();
			} else if (url.startsWith("https")) {
				BufferedImage bi = ImageIO.read(con.getInputStream());
				if (bi != null) {
					image = Toolkit.getDefaultToolkit().createImage(bi.getSource());
				}
			} else if (url.endsWith(".gif")) {
				try {
					image = new ImageIcon(imageURL).getImage();
				} catch (Exception e) {
					image = ImageIO.read(con.getInputStream());
				}
			} else if (url.endsWith(".bmp")) {
				image = ImageIO.read(con.getInputStream());
			} else {
				image = ImageIO.read(con.getInputStream());
			}

			BaseElementRenderable.this.backgroundImage = image;

			int w = -1;
			int h = -1;
			if (image != null) {
				w = image.getWidth(BaseElementRenderable.this);
				h = image.getHeight(BaseElementRenderable.this);
			}

			if (w != -1 && h != -1) {
				BaseElementRenderable.this.repaint();
			}
		} catch (FileNotFoundException | IIOException ex) {
			logger.error("loadBackgroundImage(): Image not found " + url);
		} catch (IOException | TranscoderException thrown) {
			logger.error("loadBackgroundImage()", thrown);
		} catch (Exception e) {
			logger.error("loadBackgroundImage()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.BaseRenderable#getZIndex()
	 */
	@Override
	public int getZIndex() {
		return this.zIndex;
	}

	/**
	 * Gets the border top color.
	 *
	 * @return the border top color
	 */
	private Color getBorderTopColor() {
		Color c = this.borderTopColor;
		return c == null ? Color.black : c;
	}

	/**
	 * Gets the border left color.
	 *
	 * @return the border left color
	 */
	private Color getBorderLeftColor() {
		Color c = this.borderLeftColor;
		return c == null ? Color.black : c;
	}

	/**
	 * Gets the border bottom color.
	 *
	 * @return the border bottom color
	 */
	private Color getBorderBottomColor() {
		Color c = this.borderBottomColor;
		return c == null ? Color.black : c;
	}

	/**
	 * Gets the border right color.
	 *
	 * @return the border right color
	 */
	private Color getBorderRightColor() {
		Color c = this.borderRightColor;
		return c == null ? Color.black : c;
	}

	/**
	 * Gets the width element.
	 *
	 * @return the width element
	 */
	protected int getWidthElement() {
		return this.width;
	}

	/**
	 * Gets the height element.
	 *
	 * @return the height element
	 */
	protected int getHeightElement() {
		return this.height;
	}

	/**
	 * Gets the start x.
	 *
	 * @return the start x
	 */
	protected int getStartX() {
		return 0;
	}

	/**
	 * Gets the start y.
	 *
	 * @return the start y
	 */
	protected int getStartY() {
		return 0;
	}

	/**
	 * Pre paint.
	 *
	 * @param g
	 *            the g
	 */
	protected void prePaint(java.awt.Graphics g) {
		int startWidth = getWidthElement();
		int startHeight = getHeightElement();
		int totalWidth = startWidth;
		int totalHeight = startHeight;
		int startX = getStartX();
		int startY = getStartY();
		ModelNode node = this.modelNode;
		RenderState rs = node.getRenderState();
		Insets marginInsets = this.marginInsets;
		if (marginInsets != null) {
			totalWidth -= marginInsets.left + marginInsets.right;
			totalHeight -= marginInsets.top + marginInsets.bottom;
			startX += marginInsets.left;
			startY += marginInsets.top;
		}
		Insets borderInsets = this.borderInsets;
		if (borderInsets != null) {
			int btop = borderInsets.top;
			int bleft = borderInsets.left;
			int bright = borderInsets.right;
			int bbottom = borderInsets.bottom;

			int newTotalWidth = totalWidth - (bleft + bright);
			int newTotalHeight = totalHeight - (btop + bbottom);
			int newStartX = startX + bleft;
			int newStartY = startY + btop;
			Rectangle clientRegion = new Rectangle(newStartX, newStartY, newTotalWidth, newTotalHeight);

			// Paint borders if the clip bounds are not contained
			// by the content area.
			Rectangle clipBounds = g.getClipBounds();
			if (!clientRegion.contains(clipBounds)) {
				BorderInfo borderInfo = this.borderInfo;
				if (btop > 0) {
					g.setColor(this.getBorderTopColor());
					int borderStyle = borderInfo == null ? HtmlValues.BORDER_STYLE_SOLID : borderInfo.getTopStyle();
					for (int i = 0; i < btop; i++) {
						int leftOffset = i * bleft / btop;
						int rightOffset = i * bright / btop;
						if (borderStyle == HtmlValues.BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, startX + leftOffset, startY + i,
									startX + totalWidth - rightOffset - 1, startY + i, 10 + btop, 6);
						} else {
							g.drawLine(startX + leftOffset, startY + i, startX + totalWidth - rightOffset - 1,
									startY + i);
						}
					}
				}
				if (bright > 0) {
					int borderStyle = borderInfo == null ? HtmlValues.BORDER_STYLE_SOLID : borderInfo.getRightStyle();
					g.setColor(this.getBorderRightColor());
					int lastX = startX + totalWidth - 1;
					for (int i = 0; i < bright; i++) {
						int topOffset = i * btop / bright;
						int bottomOffset = i * bbottom / bright;
						if (borderStyle == HtmlValues.BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, lastX - i, startY + topOffset, lastX - i,
									startY + totalHeight - bottomOffset - 1, 10 + bright, 6);
						} else {
							g.drawLine(lastX - i, startY + topOffset, lastX - i,
									startY + totalHeight - bottomOffset - 1);
						}
					}
				}
				if (bbottom > 0) {
					int borderStyle = borderInfo == null ? HtmlValues.BORDER_STYLE_SOLID : borderInfo.getBottomStyle();
					g.setColor(this.getBorderBottomColor());
					int lastY = startY + totalHeight - 1;
					for (int i = 0; i < bbottom; i++) {
						int leftOffset = i * bleft / bbottom;
						int rightOffset = i * bright / bbottom;
						if (borderStyle == HtmlValues.BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, startX + leftOffset, lastY - i,
									startX + totalWidth - rightOffset - 1, lastY - i, 10 + bbottom, 6);
						} else {
							g.drawLine(startX + leftOffset, lastY - i, startX + totalWidth - rightOffset - 1,
									lastY - i);
						}
					}
				}
				if (bleft > 0) {
					int borderStyle = borderInfo == null ? HtmlValues.BORDER_STYLE_SOLID : borderInfo.getLeftStyle();
					g.setColor(this.getBorderLeftColor());
					for (int i = 0; i < bleft; i++) {
						int topOffset = i * btop / bleft;
						int bottomOffset = i * bbottom / bleft;
						if (borderStyle == HtmlValues.BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, startX + i, startY + topOffset, startX + i,
									startY + totalHeight - bottomOffset - 1, 10 + bleft, 6);
						} else {
							g.drawLine(startX + i, startY + topOffset, startX + i,
									startY + totalHeight - bottomOffset - 1);
						}
					}
				}
			}

			// Adjust client area border
			totalWidth = newTotalWidth;
			totalHeight = newTotalHeight;
			startX = newStartX;
			startY = newStartY;

		}
		// Using clientG (clipped below) beyond this point.
		Graphics clientG = g.create(startX, startY, totalWidth, totalHeight);
		try {
			Rectangle bkgBounds = null;
			if (node != null) {
				Color bkg = this.backgroundColor;
				if (bkg != null && bkg.getAlpha() > 0) {
					clientG.setColor(bkg);
					bkgBounds = clientG.getClipBounds();
					clientG.fillRect(bkgBounds.x, bkgBounds.y, bkgBounds.width, bkgBounds.height);
				}
				BackgroundInfo binfo = rs == null ? null : rs.getBackgroundInfo();
				Image image = this.backgroundImage;
				if (image != null) {
					if (bkgBounds == null) {
						bkgBounds = clientG.getClipBounds();
					}
					int w = image.getWidth(this);
					int h = image.getHeight(this);
					if (w != -1 && h != -1) {
						switch (binfo == null ? BackgroundInfo.BR_REPEAT : binfo.backgroundRepeat) {
						case BackgroundInfo.BR_NO_REPEAT: {
							int imageX;
							if (binfo.isBackgroundXPositionAbsolute()) {
								imageX = binfo.getBackgroundXPosition();
							} else {
								imageX = binfo.getBackgroundXPosition() * (totalWidth - w) / 100;
							}
							int imageY;
							if (binfo.isBackgroundYPositionAbsolute()) {
								imageY = binfo.getBackgroundYPosition();
							} else {
								imageY = binfo.getBackgroundYPosition() * (totalHeight - h) / 100;
							}
							clientG.drawImage(image, imageX, imageY, w, h, this);
							break;
						}
						case BackgroundInfo.BR_REPEAT_X: {
							int imageY;
							if (binfo.isBackgroundYPositionAbsolute()) {
								imageY = binfo.getBackgroundYPosition();
							} else {
								imageY = binfo.getBackgroundYPosition() * (totalHeight - h) / 100;
							}
							// Modulate starting x.
							int x = bkgBounds.x / w * w;
							int topX = bkgBounds.x + bkgBounds.width;
							for (; x < topX; x += w) {
								clientG.drawImage(image, x, imageY, w, h, this);
							}
							break;
						}
						case BackgroundInfo.BR_REPEAT_Y: {
							int imageX;
							if (binfo.isBackgroundXPositionAbsolute()) {
								imageX = binfo.getBackgroundXPosition();
							} else {
								imageX = binfo.getBackgroundXPosition() * (totalWidth - w) / 100;
							}
							// Modulate starting y.
							int y = bkgBounds.y / h * h;
							int topY = bkgBounds.y + bkgBounds.height;
							for (; y < topY; y += h) {
								clientG.drawImage(image, imageX, y, w, h, this);
							}
							break;
						}
						default: {
							// Modulate starting x and y.
							int baseX = bkgBounds.x / w * w;
							int baseY = bkgBounds.y / h * h;
							int topX = bkgBounds.x + bkgBounds.width;
							int topY = bkgBounds.y + bkgBounds.height;
							// Replacing this:
							for (int x = baseX; x < topX; x += w) {
								for (int y = baseY; y < topY; y += h) {
									clientG.drawImage(image, x, y, w, h, this);
								}
							}
							break;
						}
						}
					}
				}
			}
		} finally {
			clientG.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int,
	 * int, int, int)
	 */
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
		// This is so that a loading image doesn't cause
		// too many repaint events.
		if ((infoflags & ImageObserver.ALLBITS) != 0 || (infoflags & ImageObserver.FRAMEBITS) != 0) {
			this.repaint();
		}
		return true;
	}

	/** The Constant SCROLL_BAR_THICKNESS. */
	protected static final int SCROLL_BAR_THICKNESS = 16;

	/**
	 * Gets insets of content area. It includes margin, borders and scrollbars,
	 * but not padding.
	 *
	 * @param hscroll
	 *            the hscroll
	 * @param vscroll
	 *            the vscroll
	 * @return the insets
	 */
	@Override
	public Insets getInsets(boolean hscroll, boolean vscroll) {
		Insets mi = this.marginInsets;
		Insets bi = this.borderInsets;
		Insets pi = this.paddingInsets;
		int top = 0;
		int bottom = 0;
		int left = 0;
		int right = 0;
		if (mi != null) {
			top += mi.top;
			left += mi.left;
			bottom += mi.bottom;
			right += mi.right;
		}
		if (bi != null) {
			top += bi.top;
			left += bi.left;
			bottom += bi.bottom;
			right += bi.right;
		}
		if (pi != null) {
			top += pi.top;
			left += pi.left;
			bottom += pi.bottom;
			right += pi.right;
		}
		if (hscroll) {
			bottom += SCROLL_BAR_THICKNESS;
		}
		if (vscroll) {
			right += SCROLL_BAR_THICKNESS;
		}
		return new Insets(top, left, bottom, right);
	}

	/**
	 * Gets the border insets.
	 *
	 * @return the border insets
	 */

	public Insets getBorderInsets() {
		return this.borderInsets == null ? RBlockViewport.ZERO_INSETS : this.borderInsets;
	}

	/**
	 * Send delayed pairs to parent.
	 */
	protected final void sendDelayedPairsToParent() {
		// Ensures that parent has all the components
		// below this renderer node. (Parent expected to have removed them).
		Collection<DelayedPair> gc = this.delayedPairs;
		if (gc != null) {
			RenderableContainer rc = this.container;
			Iterator<DelayedPair> i = gc.iterator();
			while (i.hasNext()) {
				DelayedPair pair = i.next();
				if (pair.containingBlock != this) {
					rc.addDelayedPair(pair);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.renderer.RenderableContainer#clearDelayedPairs()
	 */
	@Override
	public final void clearDelayedPairs() {
		Collection<DelayedPair> gc = this.delayedPairs;
		if (gc != null) {
			gc.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RenderableContainer#getDelayedPairs()
	 */
	@Override
	public final Collection<DelayedPair> getDelayedPairs() {
		return this.delayedPairs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.render.RenderableContainer#add(java.awt.Component)
	 */
	@Override
	public void addDelayedPair(DelayedPair pair) {
		// Expected to be called in GUI thread.
		// Adds only in local collection.
		// Does not remove from parent.
		// Sending components to parent is done
		// by sendDelayedPairsToParent().
		Collection<DelayedPair> gc = this.delayedPairs;
		if (gc == null) {
			// Sequence is important.
			// TODO: But possibly added multiple
			// times in table layout?
			gc = new LinkedList<DelayedPair>();
			this.delayedPairs = gc;
		}
		gc.add(pair);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.renderer.RenderableContainer#getParentContainer()
	 */
	@Override
	public RenderableContainer getParentContainer() {
		return this.container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.renderer.BoundableRenderable#isContainedByNode()
	 */
	@Override
	public boolean isContainedByNode() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RElement#getCollapsibleMarginBottom()
	 */
	@Override
	public int getCollapsibleMarginBottom() {
		int cm;
		Insets paddingInsets = this.paddingInsets;
		if (paddingInsets != null && paddingInsets.bottom > 0) {
			cm = 0;
		} else {
			Insets borderInsets = this.borderInsets;
			if (borderInsets != null && borderInsets.bottom > 0) {
				cm = 0;
			} else {
				cm = this.getMarginBottom();
			}
		}
		if (this.isMarginBoundary()) {
			RenderState rs = this.modelNode.getRenderState();
			if (rs != null) {
				FontMetrics fm = rs.getFontMetrics();
				int fontHeight = fm.getHeight();
				if (fontHeight > cm) {
					cm = fontHeight;
				}
			}
		}
		return cm;
	}

	/**
	 * Checks if is margin boundary.
	 *
	 * @return true, if is margin boundary
	 */
	protected boolean isMarginBoundary() {
		return this.overflowY != RenderState.OVERFLOW_VISIBLE && this.overflowX != RenderState.OVERFLOW_NONE
				|| this.modelNode instanceof HTMLDocumentImpl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RElement#getCollapsibleMarginTop()
	 */
	@Override
	public int getCollapsibleMarginTop() {
		int cm;
		Insets paddingInsets = this.paddingInsets;
		if (paddingInsets != null && paddingInsets.top > 0) {
			cm = 0;
		} else {
			Insets borderInsets = this.borderInsets;
			if (borderInsets != null && borderInsets.top > 0) {
				cm = 0;
			} else {
				cm = this.getMarginTop();
			}
		}
		if (this.isMarginBoundary()) {
			RenderState rs = this.modelNode.getRenderState();
			if (rs != null) {
				FontMetrics fm = rs.getFontMetrics();
				int fontHeight = fm.getHeight();
				if (fontHeight > cm) {
					cm = fontHeight;
				}
			}
		}
		return cm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RElement#getMarginBottom()
	 */
	@Override
	public int getMarginBottom() {
		Insets marginInsets = this.marginInsets;
		return marginInsets == null ? 0 : marginInsets.bottom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RElement#getMarginLeft()
	 */
	@Override
	public int getMarginLeft() {
		Insets marginInsets = this.marginInsets;
		return marginInsets == null ? 0 : marginInsets.left;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RElement#getMarginRight()
	 */
	@Override
	public int getMarginRight() {
		Insets marginInsets = this.marginInsets;
		return marginInsets == null ? 0 : marginInsets.right;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.renderer.RElement#getMarginTop()
	 */
	@Override
	public int getMarginTop() {
		Insets marginInsets = this.marginInsets;
		return marginInsets == null ? 0 : marginInsets.top;
	}
}
