package org.lobobrowser.html.w3c;

public interface HTMLMarqueeElement extends HTMLElement {
	// HTMLMarqueeElement
	public String getBehavior();

	public void setBehavior(String behavior);

	public String getBgColor();

	public void setBgColor(String bgColor);

	public String getDirection();

	public void setDirection(String direction);

	public String getHeight();

	public void setHeight(String height);

	public int getHspace();

	public void setHspace(int hspace);

	public int getLoop();

	public void setLoop(int loop);

	public int getScrollAmount();

	public void setScrollAmount(int scrollAmount);

	public int getScrollDelay();

	public void setScrollDelay(int scrollDelay);

	public String getTrueSpeed();

	public void setTrueSpeed(String trueSpeed);

	public int getVspace();

	public void setVspace(int vspace);

	public String getWidth();

	public void setWidth(String width);

	public void start();

	public void stop();
}
