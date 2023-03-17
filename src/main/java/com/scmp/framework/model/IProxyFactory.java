package com.scmp.framework.model;

import net.lightbody.bmp.BrowserMobProxy;

public interface IProxyFactory {
	BrowserMobProxy getProxy(String name);
}
