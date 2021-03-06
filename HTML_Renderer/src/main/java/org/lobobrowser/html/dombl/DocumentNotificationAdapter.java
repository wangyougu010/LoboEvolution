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
package org.lobobrowser.html.dombl;

import org.lobobrowser.html.domimpl.DOMNodeImpl;

/**
 * An abstract implementation of {@link DocumentNotificationListener} with blank
 * methods, provided for convenience.
 */
public abstract class DocumentNotificationAdapter implements DocumentNotificationListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.dombl.DocumentNotificationListener#allInvalidated()
	 */
	@Override
	public void allInvalidated() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.dombl.DocumentNotificationListener#
	 * externalScriptLoading (org.lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void externalScriptLoading(DOMNodeImpl node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.dombl.DocumentNotificationListener#invalidated(org.
	 * lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void invalidated(DOMNodeImpl node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.dombl.DocumentNotificationListener#lookInvalidated(
	 * org .lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void lookInvalidated(DOMNodeImpl node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.dombl.DocumentNotificationListener#nodeLoaded(org.
	 * lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void nodeLoaded(DOMNodeImpl node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.dombl.DocumentNotificationListener#
	 * positionInvalidated (org.lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void positionInvalidated(DOMNodeImpl node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.lobobrowser.html.dombl.DocumentNotificationListener#sizeInvalidated(
	 * org .lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void sizeInvalidated(DOMNodeImpl node) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lobobrowser.html.dombl.DocumentNotificationListener#
	 * structureInvalidated (org.lobobrowser.html.domimpl.DOMNodeImpl)
	 */
	@Override
	public void structureInvalidated(DOMNodeImpl node) {
	}
}
