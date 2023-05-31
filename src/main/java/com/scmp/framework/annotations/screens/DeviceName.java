package com.scmp.framework.annotations.screens;

public enum DeviceName {
	iPhoneX(375, 812),
	iPad(768, 1024),
	iPadPro(1024, 1366),
	GalaxyS5(360, 640),
	Pixel2XL(411, 823),
	OtherDevice(0, 0),
	DeskTopHD(1680, 1050);

	public int width = 0;
	public int height = 0;

	DeviceName(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
