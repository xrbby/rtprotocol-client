package me.xrbby.client;

@FunctionalInterface
public interface EndpointListener {

	void onDataReceive(String data);
}